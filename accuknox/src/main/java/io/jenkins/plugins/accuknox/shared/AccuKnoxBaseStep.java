package io.jenkins.plugins.accuknox.shared;

import org.jenkinsci.plugins.workflow.steps.Step;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.Serializable;

/**
 * Abstract base for all AccuKnox scanner pipeline steps.
 *
 * <p>Every scan type (SAST, DAST, Container, IaC, Secret) shares three
 * mandatory parameters and two optional parameters defined here.
 * Concrete sub-classes only need to declare their own extra fields and
 * implement {@link #getDockerImage()} / {@link #getScanTypeName()}.</p>
 */
public abstract class AccuKnoxBaseStep extends Step implements Serializable {

    private static final long serialVersionUID = 1L;

    // ── Mandatory ────────────────────────────────────────────────────────────

    /** AccuKnox ASPM API token. Always use a Jenkins Secret Text credential. */
    private final String token;

    /** Label that tags scan results inside the AccuKnox ASPM console. */
    private final String label;

    /** Base URL of the AccuKnox ASPM instance, e.g. https://cspm.demo.accuknox.com */
    private final String endpoint;

    // ── Optional (set via @DataBoundSetter) ───────────────────────────────────

    /**
     * When {@code true} the step will NOT fail the build on findings —
     * useful for informational / advisory runs.
     * Default: {@code false} (fail the build).
     */
    private boolean softFail = false;

    /**
     * Workspace-relative path the scanner should analyse.
     * Default: {@code .} (entire workspace).
     */
    private String scanPath = ".";

    // ── Constructor ───────────────────────────────────────────────────────────

    protected AccuKnoxBaseStep(String token, String label, String endpoint) {
        this.token    = token;
        this.label    = label;
        this.endpoint = endpoint;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String  getToken()    { return token; }
    public String  getLabel()    { return label; }
    public String  getEndpoint() { return endpoint; }
    public boolean isSoftFail()  { return softFail; }
    public String  getScanPath() { return scanPath; }

    // ── Setters ───────────────────────────────────────────────────────────────

    @DataBoundSetter
    public void setSoftFail(boolean softFail) { this.softFail = softFail; }

    @DataBoundSetter
    public void setScanPath(String scanPath)  { this.scanPath = scanPath; }

    // ── Abstract ──────────────────────────────────────────────────────────────

    /** Fully-qualified Docker image reference for this scan type. */
    public abstract String getDockerImage();

    /** Human-readable scan type label used in build logs (e.g. "SAST"). */
    public abstract String getScanTypeName();
}
