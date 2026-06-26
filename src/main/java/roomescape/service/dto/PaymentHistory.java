package roomescape.service.dto;

import java.time.LocalDate;
import roomescape.domain.PaymentOrder;
import roomescape.domain.PaymentOrderStatus;
import roomescape.domain.Session;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;

public record PaymentHistory(
        String orderId,
        PaymentOrderStatus status,
        Long amount,
        String paymentKey,
        LocalDate date,
        TimeSlot timeSlot,
        Theme theme
) {

    public static PaymentHistory of(PaymentOrder order, Session session) {
        return new PaymentHistory(
                order.orderId(),
                order.status(),
                order.amount(),
                order.paymentKey(),
                session == null ? null : session.getDate(),
                session == null ? null : session.getTimeSlot(),
                session == null ? null : session.getTheme()
        );
    }
}
