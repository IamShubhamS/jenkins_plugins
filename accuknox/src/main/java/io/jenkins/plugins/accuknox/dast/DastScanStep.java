package io.jenkins.plugins.accuknox.dast;

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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

/**
 * Pipeline step: accuknoxDastScan
 *
 * Performs Dynamic Application Security Testing (DAST) against a live target URL.
 */
public class DastScanStep extends AccuKnoxBaseStep {

    private static final long serialVersionUID = 1L;

    public static final String DOCKER_IMAGE =
            "public.ecr.aws/k9v9d5v2/accuknox-aspm-dast-scanner:latest";

    /** Live URL the DAST scanner will probe. */
    private String targetUrl = "";

    @DataBoundConstructor
    public DastScanStep(String token, String label, String endpoint) {
        super(token, label, endpoint);
    }

    @Override
    public String getDockerImage() {
        return DOCKER_IMAGE;
    }

    @Override
    public String getScanTypeName() {
        return "DAST";
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    @DataBoundSetter
    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    @Override
    public StepExecution start(StepContext context) {
        return new DastScanExecution(this, context);
    }

    // ── Descriptor ───────────────────────────────────────────────

    @Extension
    public static final class DescriptorImpl extends StepDescriptor {

        @Override
        public String getFunctionName() {
            return "accuknoxDastScan";
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return "AccuKnox DAST Scan";
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

        public FormValidation doCheckTargetUrl(@QueryParameter String value) {
            if (value == null || value.trim().isEmpty()) {
                return FormValidation.error("targetUrl is required for DAST scans.");
            }

            try {
                URI uri = new URI(value);

                if (uri.getScheme() == null ||
                        (!uri.getScheme().equalsIgnoreCase("http")
                                && !uri.getScheme().equalsIgnoreCase("https"))) {
                    return FormValidation.error("targetUrl must start with http:// or https://");
                }

                if (uri.getHost() == null) {
                    return FormValidation.error("Invalid targetUrl (host missing).");
                }

            } catch (URISyntaxException e) {
                return FormValidation.error("Invalid targetUrl format.");
            }

            return FormValidation.ok();
        }
    }
}
