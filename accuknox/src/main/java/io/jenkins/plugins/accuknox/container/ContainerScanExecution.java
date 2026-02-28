package io.jenkins.plugins.accuknox.container;

import hudson.EnvVars;
import io.jenkins.plugins.accuknox.shared.AccuKnoxBaseExecution;
import org.jenkinsci.plugins.workflow.steps.StepContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Execution for {@link ContainerScanStep}.
 *
 * Adds:
 *  - docker socket mount so scanner can inspect/pull images
 *  - IMAGE_NAME env var
 *
 * Note: Unlike SAST, container scan targets an image, so we do not pass scanPath.
 */
public class ContainerScanExecution extends AccuKnoxBaseExecution {

    private static final long serialVersionUID = 1L;

    private final ContainerScanStep containerStep;

    public ContainerScanExecution(ContainerScanStep step, StepContext context) {
        super(step, context);
        this.containerStep = step;
    }

    @Override
    protected List<String> buildDockerCommand(
            String wsPath,
            String token,
            String label,
            String endpoint,
            String scanPath,
            EnvVars envVars
    ) {
        String rawImageName = containerStep.getImageName();
        String imageName = (envVars != null) ? envVars.expand(rawImageName) : rawImageName;

        List<String> cmd = new ArrayList<>();
        cmd.add("docker");
        cmd.add("run");
        cmd.add("--rm");

        // Mount workspace (optional but harmless; keeps consistency with base)
        cmd.add("-v");
        cmd.add(wsPath + ":/scan");

        // Mount docker socket so scanner can inspect/pull images
        cmd.add("-v");
        cmd.add("/var/run/docker.sock:/var/run/docker.sock");

        cmd.add("-w");
        cmd.add("/scan");

        cmd.add("-e");
        cmd.add("TOKEN=" + token);

        cmd.add("-e");
        cmd.add("LABEL=" + label);

        cmd.add("-e");
        cmd.add("ENDPOINT=" + endpoint);

        cmd.add("-e");
        cmd.add("IMAGE_NAME=" + imageName);

        // hook for any future extra args
        cmd.addAll(extraDockerEnvArgs(envVars));

        cmd.add(containerStep.getDockerImage());

        // IMPORTANT: no scanPath argument for container scan
        return cmd;
    }
}
