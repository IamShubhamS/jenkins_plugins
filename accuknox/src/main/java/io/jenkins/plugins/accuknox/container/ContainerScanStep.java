package io.jenkins.plugins.accuknox.container;

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
 * Pipeline step: accuknoxContainerScan
 *
 * Scans a Docker container image for known CVEs.
 *
 * Note: Container scanner commonly needs docker socket access on the agent.
 */
public class ContainerScanStep extends AccuKnoxBaseStep {

    private static final long serialVersionUID = 1L;

    public static final String DOCKER_IMAGE =
            "public.ecr.aws/k9v9d5v2/accuknox-aspm-container-scanner:latest";

    /** Container image to analyse, e.g. nginx:1.25 or myrepo/app:latest */
    private String imageName = "";

    @DataBoundConstructor
    public ContainerScanStep(String token, String label, String endpoint) {
        super(token, label, endpoint);
    }

    @Override
    public String getDockerImage() {
        return DOCKER_IMAGE;
    }

    @Override
    public String getScanTypeName() {
        return "CONTAINER";
    }

    public String getImageName() {
        return imageName;
    }

    @DataBoundSetter
    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    @Override
    public StepExecution start(StepContext context) {
        return new ContainerScanExecution(this, context);
    }

    // ── Descriptor ───────────────────────────────────────────────

    @Extension
    public static final class DescriptorImpl extends StepDescriptor {

        @Override
        public String getFunctionName() {
            return "accuknoxContainerScan";
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return "AccuKnox Container Scan";
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

        public FormValidation doCheckImageName(@QueryParameter String value) {
            if (value == null || value.trim().isEmpty()) {
                return FormValidation.error("imageName is required (e.g. nginx:latest or myrepo/app:1.2.3).");
            }
            return FormValidation.ok();
        }
    }
}
