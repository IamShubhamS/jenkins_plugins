package io.jenkins.plugins.accuknox.shared;

import hudson.util.FormValidation;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Shared form validation utilities for all AccuKnox scanner steps.
 *
 * Each Descriptor's doCheck* method should delegate here
 * to avoid duplicated validation logic.
 */
public final class AccuKnoxFormValidation {

    private AccuKnoxFormValidation() {
        // utility class
    }

    // ── TOKEN ─────────────────────────────────────────

    public static FormValidation checkToken(String value) {
        if (isBlank(value)) {
            return FormValidation.error("AccuKnox API token is required.");
        }
        if (value.length() < 10) {
            return FormValidation.warning("Token looks unusually short. Please verify.");
        }
        return FormValidation.ok();
    }

    // ── LABEL ─────────────────────────────────────────

    public static FormValidation checkLabel(String value) {
        if (isBlank(value)) {
            return FormValidation.error("Label is required.");
        }
        if (value.length() > 100) {
            return FormValidation.warning("Label is quite long. Consider shortening.");
        }
        return FormValidation.ok();
    }

    // ── ENDPOINT ──────────────────────────────────────

    public static FormValidation checkEndpoint(String value) {
        if (isBlank(value)) {
            return FormValidation.error("Endpoint URL is required.");
        }

        try {
            URI uri = new URI(value);

            if (uri.getScheme() == null ||
                (!uri.getScheme().equalsIgnoreCase("http")
                 && !uri.getScheme().equalsIgnoreCase("https"))) {

                return FormValidation.error("Endpoint must start with http:// or https://");
            }

            if (uri.getHost() == null) {
                return FormValidation.error("Invalid endpoint URL.");
            }

        } catch (URISyntaxException e) {
            return FormValidation.error("Invalid endpoint URL format.");
        }

        return FormValidation.ok();
    }

    // ── INTERNAL ──────────────────────────────────────

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
