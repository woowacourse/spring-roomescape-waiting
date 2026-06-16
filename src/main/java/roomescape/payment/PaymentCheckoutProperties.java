package roomescape.payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class PaymentCheckoutProperties {

    private static final String PLACEHOLDER_CLIENT_KEY = "test_ck_placeholder";
    private static final String NOT_SET = "<not-set>";

    private final String clientKey;
    private final String source;
    private final String diagnostics;

    @Autowired
    public PaymentCheckoutProperties(Environment environment) {
        Candidate selected = firstConfigured(
                candidate("payment.client-key", environment.getProperty("payment.client-key")),
                candidate("payment.clientKey", environment.getProperty("payment.clientKey")),
                candidate("payment.toss.client-key", environment.getProperty("payment.toss.client-key")),
                candidate("payment.toss.clientKey", environment.getProperty("payment.toss.clientKey")),
                candidate("toss.client-key", environment.getProperty("toss.client-key")),
                candidate("toss.clientKey", environment.getProperty("toss.clientKey")),
                candidate("TOSS_CLIENT_KEY", environment.getProperty("TOSS_CLIENT_KEY")),
                candidate("TOSS_PAYMENTS_CLIENT_KEY", environment.getProperty("TOSS_PAYMENTS_CLIENT_KEY")),
                candidate("System.getenv(TOSS_CLIENT_KEY)", System.getenv("TOSS_CLIENT_KEY")),
                candidate("System.getenv(TOSS_PAYMENTS_CLIENT_KEY)", System.getenv("TOSS_PAYMENTS_CLIENT_KEY"))
        );
        this.clientKey = selected.value();
        this.source = selected.name();
        this.diagnostics = selected.diagnostics();
    }

    public PaymentCheckoutProperties(String clientKey) {
        this.clientKey = clientKey;
        this.source = "test";
        this.diagnostics = "test=" + mask(clientKey);
    }

    public String getClientKey() {
        return clientKey;
    }

    public boolean isConfigured() {
        return clientKey != null
                && !clientKey.isBlank()
                && !PLACEHOLDER_CLIENT_KEY.equals(clientKey)
                && clientKey.startsWith("test_ck_");
    }

    public String diagnostics() {
        return "selected=" + source + ", candidates=[" + diagnostics + "]";
    }

    private Candidate firstConfigured(Candidate... candidates) {
        StringBuilder diagnosticsBuilder = new StringBuilder();
        for (Candidate candidate : candidates) {
            if (!diagnosticsBuilder.isEmpty()) {
                diagnosticsBuilder.append(", ");
            }
            diagnosticsBuilder.append(candidate.name()).append("=").append(mask(candidate.value()));

            if (candidate.value() != null
                    && !candidate.value().isBlank()
                    && !PLACEHOLDER_CLIENT_KEY.equals(candidate.value())) {
                return new Candidate(candidate.name(), candidate.value(), diagnosticsBuilder.toString());
            }
        }
        return new Candidate("default", PLACEHOLDER_CLIENT_KEY, diagnosticsBuilder.toString());
    }

    private Candidate candidate(String name, String value) {
        return new Candidate(name, value, "");
    }

    private String mask(String value) {
        if (value == null || value.isBlank()) {
            return NOT_SET;
        }
        if (PLACEHOLDER_CLIENT_KEY.equals(value)) {
            return PLACEHOLDER_CLIENT_KEY;
        }
        if (value.length() <= 12) {
            return value.charAt(0) + "***";
        }
        return value.substring(0, 8) + "***" + value.substring(value.length() - 4);
    }

    private record Candidate(
            String name,
            String value,
            String diagnostics
    ) {
    }
}
