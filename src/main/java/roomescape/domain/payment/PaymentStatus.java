package roomescape.domain.payment;

import java.util.Arrays;
import java.util.List;

public enum PaymentStatus {
    APPROVED(List.of("DONE")),
    CANCELED(List.of("CANCELED", "PARTIAL_CANCELED")),
    ABORTED(List.of("DEFAULT"))
    ;

    private final List<String> status;

    PaymentStatus(List<String> status) {
        this.status = status;
    }

    public static PaymentStatus getStatus(String status) {
        return Arrays.stream(PaymentStatus.values())
                .filter(paymentStatus -> paymentStatus.status.stream()
                        .anyMatch(s -> s.equals(status)))
                .findFirst()
                .orElse(PaymentStatus.ABORTED);
    }
}
