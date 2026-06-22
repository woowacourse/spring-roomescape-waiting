package roomescape.payment.application.service;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.payment.application.dto.PaymentOrderCommand;
import roomescape.payment.application.dto.PaymentOrderResult;
import roomescape.payment.application.exception.PaymentErrorCode;
import roomescape.payment.application.exception.PaymentException;
import roomescape.payment.config.PaymentProperties;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentGateway;
import roomescape.payment.domain.PaymentOrder;
import roomescape.payment.domain.PaymentOrderRepository;
import roomescape.payment.domain.PaymentResult;
import roomescape.reservation.application.dto.ReservationCreateCommand;
import roomescape.reservation.application.validator.ReservationValidator;
import roomescape.reservationtime.application.dto.ReservationTimeQueryResult;
import roomescape.reservationtime.application.service.ReservationTimeService;
import roomescape.theme.application.dto.ThemeQueryResult;
import roomescape.theme.application.service.ThemeService;

@RequiredArgsConstructor
@Transactional
@Service
public class PaymentService {

    private static final String ORDER_PREFIX = "ROOM_";

    private final PaymentOrderRepository paymentOrderRepository;
    private final PaymentGateway paymentGateway;
    private final ThemeService themeService;
    private final ReservationTimeService timeService;
    private final ReservationValidator reservationValidator;
    private final PaymentProperties paymentProperties;

    public PaymentOrderResult createOrder(PaymentOrderCommand command, LocalDateTime currentDateTime) {
        ReservationTimeQueryResult time = timeService.findById(command.timeId());
        reservationValidator.validateCreateDateTime(command.date(), time.startAt(), currentDateTime);
        ThemeQueryResult theme = themeService.findById(command.themeId());
        reservationValidator.validateWaitingRequest(new ReservationCreateCommand(
                command.name(), command.date(), command.themeId(), command.timeId()));

        String orderId = ORDER_PREFIX + UUID.randomUUID().toString().replace("-", "");
        long amount = paymentProperties.reservationAmount();
        paymentOrderRepository.savePending(
                command.name(), command.date(), theme.id(), time.id(), orderId, amount);

        return new PaymentOrderResult(
                orderId,
                amount,
                theme.name() + " 방탈출 예약",
                paymentProperties.toss().clientKey()
        );
    }

    public PaymentResult confirm(PaymentConfirmation confirmation) {
        PaymentOrder order = paymentOrderRepository.findByOrderId(confirmation.orderId())
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.ORDER_NOT_FOUND));

        if (!order.hasSameAmount(confirmation.amount())) {
            throw new PaymentException(PaymentErrorCode.AMOUNT_MISMATCH);
        }

        PaymentResult result = paymentGateway.confirm(confirmation);
        paymentOrderRepository.confirm(order.orderId(), result.paymentKey());
        return result;
    }

    public void fail(String code, String orderId) {
        if (orderId != null && !orderId.isBlank()) {
            paymentOrderRepository.deletePending(orderId);
        }
    }
}
