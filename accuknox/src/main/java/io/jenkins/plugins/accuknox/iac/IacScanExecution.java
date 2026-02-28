package io.jenkins.plugins.accuknox.iac;

import hudson.EnvVars;
import io.jenkins.plugins.accuknox.shared.AccuKnoxBaseExecution;
import org.jenkinsci.plugins.workflow.steps.StepContext;

import java.util.List;

/**
 * Execution for {@link IacScanStep}.
 *
 * Injects IAC_TYPE so the scanner can apply the correct ruleset.
 */
public class IacScanExecution extends AccuKnoxBaseExecution {

    private static final long serialVersionUID = 1L;

    private final IacScanStep iacStep;

    public IacScanExecution(IacScanStep step, StepContext context) {
        super(step, context);
        this.iacStep = step;
    }

    @Override
    protected List<String> extraDockerEnvArgs(EnvVars envVars) {
        String raw = iacStep.getIacType();
        String resolved = (envVars != null) ? envVars.expand(raw) : raw;

        return List.of(
                "-e", "IAC_TYPE=" + resolved
        );
    }
}
