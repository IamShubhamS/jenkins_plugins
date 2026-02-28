package io.jenkins.plugins.accuknox.sast;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.FormValidation;
import io.jenkins.plugins.accuknox.shared.AccuKnoxBaseExecution;
import io.jenkins.plugins.accuknox.shared.AccuKnoxBaseStep;
import io.jenkins.plugins.accuknox.shared.AccuKnoxFormValidation;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.util.Set;

/**
 * Pipeline step: accuknoxSastScan
 *
 * Performs Static Application Security Testing (SAST) by running the
 * AccuKnox ASPM scanner image against the Jenkins workspace.
 */
public class SastScanStep extends AccuKnoxBaseStep {

    private static final long serialVersionUID = 1L;

    public static final String DOCKER_IMAGE =
            "public.ecr.aws/k9v9d5v2/accuknox-aspm-scanner:v0.13.4";

    @DataBoundConstructor
    public SastScanStep(String token, String label, String endpoint) {
        super(token, label, endpoint);
    }

    @Override
    public String getDockerImage() {
        return DOCKER_IMAGE;
    }

    @Override
    public String getScanTypeName() {
        return "SAST";
    }

    @Override
    public StepExecution start(StepContext context) {
        return new AccuKnoxBaseExecution(this, context);
    }

    // ── Descriptor ───────────────────────────────────────────────

    @Extension
    public static final class DescriptorImpl extends StepDescriptor {

        @Override
        public String getFunctionName() {
            return "accuknoxSastScan";
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return "AccuKnox SAST Scan";
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

