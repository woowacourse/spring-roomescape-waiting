package roomescape.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.domain.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@JdbcTest
@Import({PaymentOrderDao.class, ReservationDao.class, ReservationSlotDao.class, ReservationTimeDao.class, ThemeDao.class})
public class PaymentOrderDaoTest {
    @Autowired
    private PaymentOrderDao paymentOrderDao;

    @Autowired
    private ReservationDao reservationDao;

    @Autowired
    private ReservationSlotDao reservationSlotDao;

    @Autowired
    private ReservationTimeDao reservationTimeDao;

    @Autowired
    private ThemeDao themeDao;

    @Test
    void 결제_주문을_생성한다() {
        Reservation reservation = saveReservation();
        LocalDateTime createdAt = LocalDateTime.of(2026, 6, 17, 10, 10);
        PaymentOrder paymentOrder = PaymentOrder.createPendingWithoutId(
                "order_123",
                reservation.getId(),
                10_000L,
                createdAt
        );

        PaymentOrder saved = paymentOrderDao.insert(paymentOrder);

        assertAll(
                () -> assertThat(saved.getId()).isNotNull(),
                () -> assertThat(saved.getOrderId()).isEqualTo("order_123"),
                () -> assertThat(saved.getReservationId()).isEqualTo(reservation.getId()),
                () -> assertThat(saved.getAmount()).isEqualTo(10_000L),
                () -> assertThat(saved.getStatus()).isEqualTo(PaymentOrderStatus.PENDING),
                () -> assertThat(saved.getCreatedAt()).isEqualTo(createdAt)
        );
    }

    @Test
    void 주문_아이디로_결제_주문을_조회한다() {
        Reservation reservation = saveReservation();
        LocalDateTime createdAt = LocalDateTime.of(2026, 6, 17, 10, 10);

        PaymentOrder saved = paymentOrderDao.insert(
                PaymentOrder.createPendingWithoutId(
                        "order_123",
                        reservation.getId(),
                        10_000L,
                        LocalDateTime.of(2026, 6, 17, 10, 0)
                )
        );

        Optional<PaymentOrder> found = paymentOrderDao.selectByOrderId("order_123");

        assertThat(found).isPresent();
        assertThat(found.get())
                .extracting(
                        PaymentOrder::getId,
                        PaymentOrder::getOrderId,
                        PaymentOrder::getReservationId,
                        PaymentOrder::getAmount,
                        PaymentOrder::getStatus,
                        PaymentOrder::getCreatedAt
                )
                .containsExactly(
                        saved.getId(),
                        saved.getOrderId(),
                        saved.getReservationId(),
                        saved.getAmount(),
                        saved.getStatus(),
                        saved.getCreatedAt()
                );
    }

    @Test
    void 존재하지_않는_주문_아이디로_조회하면_빈_값을_반환한다() {
        Optional<PaymentOrder> found = paymentOrderDao.selectByOrderId("not_exist_order");

        assertThat(found).isEmpty();
    }

    private Reservation saveReservation() {
        ReservationTime time = reservationTimeDao.insert(
                ReservationTime.createWithoutId(LocalTime.of(10, 0))
        );
        Theme theme = themeDao.insert(
                Theme.createWithoutId("방탈출1", "설명", "https://thumb.com")
        );
        ReservationSlot slot = reservationSlotDao.findOrCreate(
                new ReservationSlot(LocalDate.of(2026, 7, 20), time, theme)
        );

        return reservationDao.insert(
                Reservation.createConfirmedWithoutId("브라운", slot)
        );
    }
}
