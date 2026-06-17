package roomescape.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationPaymentDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.ThemeDao;
import roomescape.dao.exception.DataConflictException;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationPayment;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.payment.OrderIdGenerator;
import roomescape.payment.PaymentAmountMismatchException;
import roomescape.payment.PaymentConfirmation;
import roomescape.payment.PaymentGateway;
import roomescape.payment.PaymentOrderNotFoundException;
import roomescape.payment.PaymentResult;
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
        if (reservationDao.existsByDateAndTimeIdAndThemeId(date, timeId, themeId)
                || reservationPaymentDao.existsByDateAndTimeIdAndThemeId(date, timeId, themeId)) {
            throw new ReservationConflictException("이미 예약된 시간입니다.");
        }
        Reservation reservation = new Reservation(name, date, LocalDateTime.now(clock), time, theme);
        ReservationPayment payment = new ReservationPayment(orderIdGenerator.generate(), reservationAmount, reservation);
        try {
            return reservationPaymentDao.save(payment);
        } catch (DataConflictException e) {
            throw new ReservationConflictException("이미 예약된 시간입니다.");
        }
    }

    @Transactional
    public Reservation confirm(String paymentKey, String orderId, long amount) {
        ReservationPayment payment = reservationPaymentDao.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentOrderNotFoundException("존재하지 않는 주문입니다."));
        if (payment.getAmount() != amount) {
            throw new PaymentAmountMismatchException(payment.getAmount(), amount);
        }

        PaymentResult result = paymentGateway.confirm(new PaymentConfirmation(paymentKey, orderId, amount));
        reservationPaymentDao.updatePaymentKey(orderId, result.paymentKey());
        try {
            return reservationDao.save(payment.getReservation());
        } catch (DataConflictException e) {
            throw new ReservationConflictException("이미 예약된 시간입니다.");
        }
    }

    private ReservationTime validateReservationTime(long timeId) {
        return reservationTimeDao.findById(timeId)
                .orElseThrow(() -> new ReservationTimeNotFoundException("존재하지 않는 예약 시간입니다."));
    }

    private Theme validateTheme(long themeId) {
        return themeDao.findById(themeId)
                .orElseThrow(() -> new ThemeNotFoundException("존재하지 않는 테마입니다."));
    }
}
