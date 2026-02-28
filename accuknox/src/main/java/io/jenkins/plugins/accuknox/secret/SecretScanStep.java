package io.jenkins.plugins.accuknox.secret;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.FormValidation;
import io.jenkins.plugins.accuknox.shared.AccuKnoxBaseStep;
import io.jenkins.plugins.accuknox.shared.AccuKnoxFormValidation;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import java.util.Set;

/**
 * Pipeline step: accuknoxSecretScan
 *
 * Detects leaked secrets accidentally committed to source control.
 */
public class SecretScanStep extends AccuKnoxBaseStep {

    private static final long serialVersionUID = 1L;

    public static final String DOCKER_IMAGE =
            "public.ecr.aws/k9v9d5v2/accuknox-aspm-secret-scanner:latest";

    /**
     * When true, scanner also inspects git commit history.
     * Default: false.
     */
    private boolean scanHistory = false;

    @DataBoundConstructor
    public SecretScanStep(String token, String label, String endpoint) {
        super(token, label, endpoint);
    }

    @Override
    public String getDockerImage() {
        return DOCKER_IMAGE;
    }

    @Override
    public String getScanTypeName() {
        return "SECRET";
    }

    public boolean isScanHistory() {
        return scanHistory;
    }

    @DataBoundSetter
    public void setScanHistory(boolean scanHistory) {
        this.scanHistory = scanHistory;
    }

    @Override
    public StepExecution start(StepContext context) {
        return new SecretScanExecution(this, context);
    }

    // ── Descriptor ───────────────────────────────────────────────

    @Extension
    public static final class DescriptorImpl extends StepDescriptor {

        @Override
        public String getFunctionName() {
            return "accuknoxSecretScan";
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return "AccuKnox Secret Scan";
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Set.of(
                    Run.class,
                    FilePath.class,
                    Launcher.class,
                    TaskListener.class,
                    EnvVars.class
            );
        }

        public FormValidation doCheckToken(@QueryParameter String value) {
            return AccuKnoxFormValidation.checkToken(value);
        }

        public FormValidation doCheckLabel(@QueryParameter String value) {
            return AccuKnoxFormValidation.checkLabel(value);
        }

        public FormValidation doCheckEndpoint(@QueryParameter String value) {
            return AccuKnoxFormValidation.checkEndpoint(value);
        }
    }
}
