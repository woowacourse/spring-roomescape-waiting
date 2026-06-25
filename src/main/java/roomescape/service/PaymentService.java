package roomescape.service;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

import roomescape.domain.Payment;
import roomescape.domain.PaymentOrder;
import roomescape.domain.PaymentStatus;
import roomescape.domain.repository.PaymentOrderRepository;
import roomescape.domain.repository.PaymentRepository;
import roomescape.domain.repository.ReservationSlotRepository;
import roomescape.domain.vo.PaymentConfirmation;
import roomescape.domain.vo.PaymentResult;
import roomescape.dto.PaymentConfirmRequest;
import roomescape.dto.PaymentFailRequest;
import roomescape.exception.CustomException;
import roomescape.exception.ErrorCode;
import roomescape.infrastructure.toss.TossPaymentException;
import roomescape.service.port.PaymentGateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class PaymentService {
    private static final Logger log =
            LoggerFactory.getLogger(PaymentService.class);

    private final PaymentOrderRepository paymentOrderRepository;
    private final PaymentRepository paymentRepository;
    private final ReservationSlotRepository reservationSlotRepository;
    private final PaymentGateway paymentGateway;

    public PaymentService(
            PaymentOrderRepository paymentOrderRepository,
            PaymentRepository paymentRepository,
            ReservationSlotRepository reservationSlotRepository,
            PaymentGateway paymentGateway
    ) {
        this.paymentOrderRepository = paymentOrderRepository;
        this.paymentRepository = paymentRepository;
        this.reservationSlotRepository = reservationSlotRepository;
        this.paymentGateway = paymentGateway;
    }

    @Transactional
    public void confirm(PaymentConfirmRequest request) {
        PaymentOrder paymentOrder = paymentOrderRepository.getByOrderId(request.orderId());
        if (!paymentOrder.getAmount().equals(request.amount())) {
            log.warn("Payment amount mismatch. expected={}, actual={}", paymentOrder.getAmount(), request.amount());
            throw new CustomException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        PaymentResult result = confirmPayment(request, paymentOrder);
        if (!paymentRepository.existsByPaymentOrderId(paymentOrder.getId())) {
            paymentRepository.save(Payment.create(paymentOrder.getId(), result.paymentKey(), result.amount()));
        }
        paymentOrderRepository.updateStatus(paymentOrder.getId(), PaymentStatus.CONFIRMED);
        reservationSlotRepository.confirmPayment(paymentOrder.getReservationId());
    }

    private PaymentResult confirmPayment(PaymentConfirmRequest request, PaymentOrder paymentOrder) {
        try {
            return paymentGateway.confirm(
                    new PaymentConfirmation(
                            request.paymentKey(),
                            paymentOrder.getOrderId(),
                            request.amount(),
                            paymentOrder.getIdempotencyKey()
                    )
            );
        } catch (ResourceAccessException e) {
            if (isReadTimeout(e)) {
                paymentOrderRepository.updateStatus(paymentOrder.getId(), PaymentStatus.UNKNOWN);
                throw new CustomException(ErrorCode.PAYMENT_CONFIRM_RESULT_UNKNOWN);
            }
            if (hasCause(e, ConnectException.class) || hasCause(e, SocketTimeoutException.class)) {
                paymentOrderRepository.updateStatus(paymentOrder.getId(), PaymentStatus.FAILED);
                throw new CustomException(ErrorCode.PAYMENT_GATEWAY_CONNECTION_FAILED);
            }
            throw e;
        } catch (RestClientException e) {
            if (isReadTimeout(e)) {
                paymentOrderRepository.updateStatus(paymentOrder.getId(), PaymentStatus.UNKNOWN);
                throw new CustomException(ErrorCode.PAYMENT_CONFIRM_RESULT_UNKNOWN);
            }
            throw e;
        } catch (TossPaymentException e) {
            paymentOrderRepository.updateStatus(paymentOrder.getId(), PaymentStatus.FAILED);
            throw e;
        }
    }

    private boolean hasCause(Throwable throwable, Class<? extends Throwable> causeType) {
        Throwable current = throwable;
        while (current != null) {
            if (causeType.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private boolean isReadTimeout(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof SocketTimeoutException && current.getMessage() != null
                    && current.getMessage().toLowerCase().contains("read")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    @Transactional
    public void fail(PaymentFailRequest request) {
        if (!StringUtils.hasText(request.orderId())) {
            log.info("Payment failed without orderId. code={}, message={}", request.code(), request.message());
            return;
        }

        log.info("Payment failed. orderId={}, code={}, message={}", request.orderId(), request.code(), request.message());
        PaymentOrder paymentOrder = paymentOrderRepository.getByOrderId(request.orderId());
        paymentOrderRepository.updateStatus(paymentOrder.getId(), PaymentStatus.FAILED);
    }
}
