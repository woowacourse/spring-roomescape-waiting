package roomescape.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationPaymentDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.ThemeDao;
import roomescape.dao.exception.DataConflictException;
import roomescape.domain.PaymentStatus;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationPayment;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.payment.OrderIdGenerator;
import roomescape.payment.PaymentFailure;
import roomescape.payment.PaymentAmountMismatchException;
import roomescape.payment.PaymentConfirmation;
import roomescape.payment.PaymentConfirmUnknownException;
import roomescape.payment.PaymentConnectionException;
import roomescape.payment.PaymentGateway;
import roomescape.payment.PaymentOrderNotFoundException;
import roomescape.payment.PaymentResult;
import roomescape.payment.toss.TossPaymentException;
import roomescape.service.exception.ReservationConflictException;
import roomescape.service.exception.ReservationTimeNotFoundException;
import roomescape.service.exception.ThemeNotFoundException;

@Service
@Transactional(readOnly = true)
public class ReservationPaymentService {

    private final ReservationDao reservationDao;
    private final ReservationPaymentDao reservationPaymentDao;
    private final ReservationTimeDao reservationTimeDao;
    private final ThemeDao themeDao;
    private final OrderIdGenerator orderIdGenerator;
    private final PaymentGateway paymentGateway;
    private final Clock clock;
    private final long reservationAmount;

    public ReservationPaymentService(
            ReservationDao reservationDao,
            ReservationPaymentDao reservationPaymentDao,
            ReservationTimeDao reservationTimeDao,
            ThemeDao themeDao,
            OrderIdGenerator orderIdGenerator,
            PaymentGateway paymentGateway,
            Clock clock,
            @Value("${payment.reservation.amount}") long reservationAmount
    ) {
        this.reservationDao = reservationDao;
        this.reservationPaymentDao = reservationPaymentDao;
        this.reservationTimeDao = reservationTimeDao;
        this.themeDao = themeDao;
        this.orderIdGenerator = orderIdGenerator;
        this.paymentGateway = paymentGateway;
        this.clock = clock;
        this.reservationAmount = reservationAmount;
    }

    @Transactional
    public ReservationPayment prepare(String name, LocalDate date, long timeId, long themeId) {
        ReservationTime time = validateReservationTime(timeId);
        Theme theme = validateTheme(themeId);
        validateAvailableSlot(date, timeId, themeId);
        Reservation reservation = new Reservation(name, date, LocalDateTime.now(clock), time, theme);
        return savePayment(new ReservationPayment(orderIdGenerator.generate(), reservationAmount, reservation));
    }

    @Transactional
    public Reservation confirm(String paymentKey, String orderId, long amount) {
        ReservationPayment payment = reservationPaymentDao.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentOrderNotFoundException("존재하지 않는 주문입니다."));
        if (payment.getAmount() != amount) {
            throw new PaymentAmountMismatchException(payment.getAmount(), amount);
        }
        if (payment.getPaymentStatus() == PaymentStatus.CONFIRMED) {
            return payment.getReservation();
        }

        PaymentResult result = confirmPayment(paymentKey, orderId, amount, payment);
        reservationPaymentDao.markConfirmed(orderId, result.paymentKey());
        return saveReservation(payment.getReservation());
    }

    private PaymentResult confirmPayment(String paymentKey, String orderId, long amount, ReservationPayment payment) {
        try {
            return paymentGateway.confirm(new PaymentConfirmation(
                    paymentKey,
                    orderId,
                    amount,
                    payment.getIdempotencyKey()
            ));
        } catch (PaymentConfirmUnknownException e) {
            markConfirmUnknown(orderId, e);
            throw e;
        } catch (PaymentConnectionException e) {
            throw e;
        } catch (TossPaymentException e) {
            markFailed(orderId, e);
            throw e;
        }
    }

    @Transactional
    public PaymentFailure fail(String code, String message, String orderId) {
        if (hasText(orderId)) {
            reservationPaymentDao.deleteByOrderId(orderId);
        }
        return new PaymentFailure(code, message, orderId);
    }

    public List<ReservationPayment> findPaymentsByName(String name) {
        return reservationPaymentDao.findAllByName(name);
    }

    private ReservationTime validateReservationTime(long timeId) {
        return reservationTimeDao.findById(timeId)
                .orElseThrow(() -> new ReservationTimeNotFoundException("존재하지 않는 예약 시간입니다."));
    }

    private Theme validateTheme(long themeId) {
        return themeDao.findById(themeId)
                .orElseThrow(() -> new ThemeNotFoundException("존재하지 않는 테마입니다."));
    }

    private void validateAvailableSlot(LocalDate date, long timeId, long themeId) {
        if (reservationDao.existsByDateAndTimeIdAndThemeId(date, timeId, themeId)
                || reservationPaymentDao.existsByDateAndTimeIdAndThemeId(date, timeId, themeId)) {
            throw new ReservationConflictException("이미 예약된 시간입니다.");
        }
    }

    private ReservationPayment savePayment(ReservationPayment payment) {
        try {
            return reservationPaymentDao.save(payment);
        } catch (DataConflictException e) {
            throw new ReservationConflictException("이미 예약된 시간입니다.");
        }
    }

    private Reservation saveReservation(Reservation reservation) {
        try {
            return reservationDao.save(reservation);
        } catch (DataConflictException e) {
            throw new ReservationConflictException("이미 예약된 시간입니다.");
        }
    }

    private void markConfirmUnknown(String orderId, PaymentConfirmUnknownException e) {
        reservationPaymentDao.markConfirmUnknown(orderId, "CONFIRM_TIMEOUT", e.getMessage());
    }

    private void markFailed(String orderId, TossPaymentException e) {
        reservationPaymentDao.markFailed(orderId, e.getCode(), e.getMessage());
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
