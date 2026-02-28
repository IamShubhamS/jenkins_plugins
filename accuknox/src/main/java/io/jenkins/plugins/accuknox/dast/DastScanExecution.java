package io.jenkins.plugins.accuknox.dast;

import hudson.EnvVars;
import io.jenkins.plugins.accuknox.shared.AccuKnoxBaseExecution;
import org.jenkinsci.plugins.workflow.steps.StepContext;

import java.util.List;

/**
 * Execution for {@link DastScanStep}.
 *
 * Injects TARGET_URL as an additional Docker environment variable
 * so the DAST scanner knows which live endpoint to probe.
 */
public class DastScanExecution extends AccuKnoxBaseExecution {

    private static final long serialVersionUID = 1L;

    private final DastScanStep dastStep;

    public DastScanExecution(DastScanStep step, StepContext context) {
        super(step, context);
        this.dastStep = step;
    }

    @Override
    protected List<String> extraDockerEnvArgs(EnvVars envVars) {
        String raw = dastStep.getTargetUrl();
        String resolved = (envVars != null) ? envVars.expand(raw) : raw;

        return List.of(
                "-e", "TARGET_URL=" + resolved
        );
    }
}
