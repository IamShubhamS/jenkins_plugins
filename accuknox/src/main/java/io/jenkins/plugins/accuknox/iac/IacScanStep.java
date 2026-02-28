package io.jenkins.plugins.accuknox.iac;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
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
 * Pipeline step: accuknoxIacScan
 *
 * Scans Infrastructure-as-Code files for security misconfigurations.
 */
public class IacScanStep extends AccuKnoxBaseStep {

    private static final long serialVersionUID = 1L;

    public static final String DOCKER_IMAGE =
            "public.ecr.aws/k9v9d5v2/accuknox-aspm-iac-scanner:latest";

    /**
     * IaC framework hint for the scanner.
     * Values: auto | terraform | helm | k8s | cloudformation | dockerfile
     */
    private String iacType = "auto";

    @DataBoundConstructor
    public IacScanStep(String token, String label, String endpoint) {
        super(token, label, endpoint);
    }

    @Override
    public String getDockerImage() {
        return DOCKER_IMAGE;
    }

    @Override
    public String getScanTypeName() {
        return "IAC";
    }

    public String getIacType() {
        return iacType;
    }

    @DataBoundSetter
    public void setIacType(String iacType) {
        this.iacType = iacType;
    }

    @Override
    public StepExecution start(StepContext context) {
        return new IacScanExecution(this, context);
    }

    // ── Descriptor ───────────────────────────────────────────────

    @Extension
    public static final class DescriptorImpl extends StepDescriptor {

        @Override
        public String getFunctionName() {
            return "accuknoxIacScan";
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return "AccuKnox IaC Scan";
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

        public FormValidation doCheckIacType(@QueryParameter String value) {
            if (value == null || value.trim().isEmpty()) {
                return FormValidation.error("iacType is required (use 'auto' if unsure).");
            }
            String v = value.trim().toLowerCase();
            switch (v) {
                case "auto":
                case "terraform":
                case "helm":
                case "k8s":
                case "cloudformation":
                case "dockerfile":
                    return FormValidation.ok();
                default:
                    return FormValidation.error("Invalid iacType. Use auto/terraform/helm/k8s/cloudformation/dockerfile.");
            }
        }

        /** Populates the IaC type dropdown in the config UI. */
        public ListBoxModel doFillIacTypeItems() {
            ListBoxModel m = new ListBoxModel();
            m.add("auto — detect automatically", "auto");
            m.add("Terraform", "terraform");
            m.add("Helm", "helm");
            m.add("Kubernetes manifests", "k8s");
            m.add("AWS CloudFormation", "cloudformation");
            m.add("Dockerfile", "dockerfile");
            return m;
        }
    }
}
