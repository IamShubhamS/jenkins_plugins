package io.jenkins.plugins.accuknox.shared;

import hudson.util.Secret;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.Serializable;

/**
 * Abstract base for all AccuKnox scanner pipeline steps.
 *
 * Every scan type (SAST, DAST, Container, IaC, Secret) shares three
 * mandatory parameters and two optional parameters defined here.
 */
public abstract class AccuKnoxBaseStep extends Step implements Serializable {

    private static final long serialVersionUID = 1L;

    // ── Mandatory ─────────────────────────────────────────

    /** AccuKnox ASPM API token (stored securely). */
    private final Secret token;

    /** Label that tags scan results inside the AccuKnox ASPM console. */
    private final String label;

    /** Base URL of the AccuKnox ASPM instance. */
    private final String endpoint;

    // ── Optional ──────────────────────────────────────────

    private boolean softFail = false;
    private String scanPath = ".";

    // ── Constructor ───────────────────────────────────────

    @DataBoundConstructor
    protected AccuKnoxBaseStep(String token, String label, String endpoint) {
        this.token = Secret.fromString(token);
        this.label = label;
        this.endpoint = endpoint;
    }

    // ── Getters ───────────────────────────────────────────

    public String getToken() {
        return token.getPlainText();
    }

    public String getLabel() {
        return label;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public boolean isSoftFail() {
        return softFail;
    }

    public String getScanPath() {
        return scanPath;
    }

    // ── Setters ───────────────────────────────────────────

    @DataBoundSetter
    public void setSoftFail(boolean softFail) {
        this.softFail = softFail;
    }

    @DataBoundSetter
    public void setScanPath(String scanPath) {
        this.scanPath = scanPath;
    }

    // ── Abstract ──────────────────────────────────────────

    public abstract String getDockerImage();

    public abstract String getScanTypeName();
}
