package roomescape.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import roomescape.DatabaseInitializer;
import roomescape.common.exception.RoomEscapeException;
import roomescape.common.exception.code.PaymentErrorCode;
import roomescape.dao.*;
import roomescape.domain.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

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

    @Autowired
    private DatabaseInitializer databaseInitializer;

    @MockitoBean
    private PaymentGateway paymentGateway;

    @BeforeEach
    void setUp() {
        databaseInitializer.clear();
    }

    @Test
    void 저장된_주문이_없으면_예외가_발생한다() {
        assertThatThrownBy(() -> paymentService.confirm("payment-key", "unknown-order-id", 10_000L))
                .isInstanceOfSatisfying(RoomEscapeException.class, exception ->
                        assertThat(exception.getErrorCode())).isEqualTo(PaymentErrorCode.ORDER_NOT_FOUND);

        verify(paymentGateway, never()).confirm(any());
    }

    @Test
    void 요청_금액이_저장된_금액과_다르면_승인_호출_전에_차단한다() {
        Reservation reservation = saveReservation();
        paymentOrderDao.insert(
                PaymentOrder.createPendingWithoutId(
                        "order-1",
                        reservation.getId(),
                        10_000L,
                        LocalDateTime.of(2026, 6, 18, 10, 0)
                )
        );

        assertThatThrownBy(() -> paymentService.confirm("payment-key", "order-1", 9_000L))
                .isInstanceOfSatisfying(RoomEscapeException.class, exception ->
                        assertThat(exception.getErrorCode())).isEqualTo(PaymentErrorCode.AMOUNT_MISMATCH);

        verify(paymentGateway, never()).confirm(any());
    }

    @Test
    void 요청_금액이_저장된_금액과_같으면_결제_승인을_호출한다() {
        Reservation reservation = saveReservation();
        paymentOrderDao.insert(
                PaymentOrder.createPendingWithoutId(
                        "order-1",
                        reservation.getId(),
                        10_000L,
                        LocalDateTime.of(2026, 6, 18, 10, 0)
                )
        );

        given(paymentGateway.confirm(new PaymentConfirmation("payment-key", "order-1", 10_000L)))
                .willReturn(new PaymentResult("payment-key", "order-1", 10_000L));

        PaymentResult result = paymentService.confirm("payment-key", "order-1", 10_000L);

        assertThat(result)
                .extracting(
                        PaymentResult::paymentKey,
                        PaymentResult::orderId,
                        PaymentResult::approvedAmount
                )
                .containsExactly("payment-key", "order-1", 10_000L);

        verify(paymentGateway).confirm(new PaymentConfirmation("payment-key", "order-1", 10_000L));
    }

    private Reservation saveReservation() {
        ReservationTime time = reservationTimeDao.insert(
                ReservationTime.createWithoutId(LocalTime.of(10, 0))
        );
        Theme theme = themeDao.insert(
                Theme.createWithoutId("방탈출1", "설명", "https://thumb.com")
        );
        ReservationSlot slot = reservationSlotDao.findOrCreate(
                new ReservationSlot(LocalDate.of(2026, 6, 20), time, theme)
        );

        return reservationDao.insert(
                Reservation.createPendingWithoutId("브라운", slot)
        );
    }
}
