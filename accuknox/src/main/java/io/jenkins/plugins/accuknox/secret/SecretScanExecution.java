package io.jenkins.plugins.accuknox.secret;

import hudson.EnvVars;
import io.jenkins.plugins.accuknox.shared.AccuKnoxBaseExecution;
import org.jenkinsci.plugins.workflow.steps.StepContext;

import java.util.List;

/**
 * Execution for {@link SecretScanStep}.
 *
 * Injects SCAN_HISTORY so the scanner knows
 * whether to inspect git commit history.
 */
public class SecretScanExecution extends AccuKnoxBaseExecution {

    private static final long serialVersionUID = 1L;

    private final SecretScanStep secretStep;

    public SecretScanExecution(SecretScanStep step, StepContext context) {
        super(step, context);
        this.secretStep = step;
    }

    @Override
    protected List<String> extraDockerEnvArgs(EnvVars envVars) {
        return List.of(
                "-e", "SCAN_HISTORY=" + secretStep.isScanHistory()
        );
    }
}
