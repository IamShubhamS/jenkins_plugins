package io.jenkins.plugins.accuknox.shared;

import hudson.AbortException;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic execution used by scan types that need no extra env vars.
 *
 * Workflow:
 *  1) Resolves user params against EnvVars
 *  2) Builds a docker run command mounting workspace -> /scan
 *  3) Streams output to build log (token redacted)
 *  4) On non-zero exit: fails unless softFail=true
 *
 * Scan types that need extra env vars override {@link #extraDockerEnvArgs(EnvVars)}.
 */
public class AccuKnoxBaseExecution extends SynchronousNonBlockingStepExecution<Void> {

    private static final long serialVersionUID = 1L;

    protected final AccuKnoxBaseStep step;

    public AccuKnoxBaseExecution(AccuKnoxBaseStep step, StepContext context) {
        super(context);
        this.step = step;
    }

    @Override
    protected Void run() throws Exception {

        StepContext ctx = getContext();

        TaskListener listener = ctx.get(TaskListener.class);
        FilePath workspace = ctx.get(FilePath.class);
        Launcher launcher = ctx.get(Launcher.class);
        EnvVars envVars = ctx.get(EnvVars.class);

        if (listener == null) {
            throw new AbortException("[AccuKnox] TaskListener not available in StepContext.");
        }
        if (workspace == null) {
            throw new AbortException("[AccuKnox] Workspace (FilePath) not available. Ensure agent has a workspace.");
        }
        if (launcher == null) {
            throw new AbortException("[AccuKnox] Launcher not available in StepContext.");
        }
        if (envVars == null) {
            envVars = new EnvVars();
        }

        PrintStream log = listener.getLogger();

        // Expand any $VAR or ${VAR} references in the user-supplied values
        String token = envVars.expand(step.getToken());
        String label = envVars.expand(step.getLabel());
        String endpoint = envVars.expand(step.getEndpoint());
        String scanPath = envVars.expand(step.getScanPath());

        // Workspace path on the agent
        String wsPath = workspace.getRemote();

        printBanner(log, label, endpoint, scanPath);

        List<String> cmd = buildDockerCommand(wsPath, token, label, endpoint, scanPath, envVars);

        log.println("[AccuKnox] $ " + redact(String.join(" ", cmd), token));

        int exit = launcher.launch()
                .cmds(cmd)
                .pwd(workspace)
                .stdout(listener)
                .stderr(listener.getLogger())
                .join();

        log.println("[AccuKnox] ─────────────────────────────────────────────");
        log.println("[AccuKnox] Exit code: " + exit);

        handleExit(exit, log);
        return null;
    }

    /**
     * Builds a safe docker command (no cmdAsSingleString quoting issues).
     *
     * Mounts workspace -> /scan.
     * If scanPath is "." or empty -> runs on /scan.
     * Else runs on /scan/<scanPath>.
     */
    protected List<String> buildDockerCommand(
            String wsPath,
            String token,
            String label,
            String endpoint,
            String scanPath,
            EnvVars envVars
    ) {

        String targetPathInContainer;
        if (scanPath == null || scanPath.trim().isEmpty() || ".".equals(scanPath.trim())) {
            targetPathInContainer = "/scan";
        } else {
            // normalize: do not allow absolute paths to escape /scan
            String cleaned = scanPath.replace("\\", "/");
            while (cleaned.startsWith("/")) cleaned = cleaned.substring(1);
            targetPathInContainer = "/scan/" + cleaned;
        }

        List<String> cmd = new ArrayList<>();
        cmd.add("docker");
        cmd.add("run");
        cmd.add("--rm");

        cmd.add("-v");
        cmd.add(wsPath + ":/scan");

        cmd.add("-w");
        cmd.add("/scan");

        cmd.add("-e");
        cmd.add("TOKEN=" + token);

        cmd.add("-e");
        cmd.add("LABEL=" + label);

        cmd.add("-e");
        cmd.add("ENDPOINT=" + endpoint);

        // hook for extra env vars
        cmd.addAll(extraDockerEnvArgs(envVars));

        cmd.add(step.getDockerImage());

        // pass target path as arg to scanner
        cmd.add(targetPathInContainer);

        return cmd;
    }

    /**
     * Hook for subclasses to append extra docker arguments like:
     *   -e KEY=VALUE
     *   --network host
     * etc.
     */
    protected List<String> extraDockerEnvArgs(EnvVars envVars) {
        return List.of();
    }

    protected void handleExit(int exit, PrintStream log) throws AbortException {
        if (exit == 0) {
            log.println("[AccuKnox] ✔ " + step.getScanTypeName() + " scan passed — no blocking findings.");
            return;
        }
        if (step.isSoftFail()) {
            log.println("[AccuKnox] ⚠  Findings detected (softFail=true) — build continues. Review ASPM console.");
        } else {
            throw new AbortException(
                    "[AccuKnox] " + step.getScanTypeName() + " scan FAILED (exit " + exit + "). " +
                    "Resolve findings or set softFail=true to continue despite vulnerabilities."
            );
        }
    }

    private void printBanner(PrintStream log, String label, String endpoint, String scanPath) {
        log.println("[AccuKnox] ═════════════════════════════════════════════");
        log.println("[AccuKnox]  AccuKnox ASPM — " + step.getScanTypeName());
        log.println("[AccuKnox] ═════════════════════════════════════════════");
        log.println("[AccuKnox] Image    : " + step.getDockerImage());
        log.println("[AccuKnox] Label    : " + label);
        log.println("[AccuKnox] Endpoint : " + endpoint);
        log.println("[AccuKnox] ScanPath : " + scanPath);
        log.println("[AccuKnox] SoftFail : " + step.isSoftFail());
        log.println("[AccuKnox] ─────────────────────────────────────────────");
    }

    /** Replace raw token value with *** so it never appears in build logs. */
    private String redact(String cmd, String token) {
        if (token == null || token.isEmpty()) return cmd;
        return cmd.replace(token, "***");
    }
}
