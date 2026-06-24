package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import roomescape.domain.vo.ReservationDeletion;
import roomescape.domain.vo.ReservationSlotInfo;
import roomescape.exception.CustomException;
import roomescape.exception.ErrorCode;

class ReservationSlotTest {

    private static final LocalTime RESERVATION_TIME = LocalTime.of(15, 0);

    @Test
    @DisplayName("첫 예약은 결제 대기 상태로 예약한다")
    void 첫_예약_결제_대기() {
        LocalDateTime now = LocalDateTime.now();
        ReservationSlot reservationSlot = createReservationSlot(now, List.of());

        Reservation reservation = reservationSlot.reserve("브라운", now);

        assertThat(reservation.getName()).isEqualTo("브라운");
        assertThat(reservation.getReservationSlotId()).isEqualTo(1L);
        assertThat(reservation.getStatus()).isEqualTo(Status.PAYMENT_PENDING);
        assertThat(reservation.getUpdateAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("이미 결제 대기 예약이 있으면 대기 상태로 예약한다")
    void 결제_대기_예약_존재시_대기() {
        LocalDateTime now = LocalDateTime.now();
        ReservationSlot reservationSlot = createReservationSlot(now, new ArrayList<>(List.of(
                createReservation(1L, "브라운", Status.PAYMENT_PENDING, now)
        )));

        Reservation reservation = reservationSlot.reserve("도니", now.plusMinutes(1));

        assertThat(reservation.getStatus()).isEqualTo(Status.WAITING);
    }

    @Test
    @DisplayName("결제 대기 예약의 대기 순서는 0이다")
    void 결제_대기_예약_순서() {
        LocalDateTime now = LocalDateTime.now();
        Reservation reservation = createReservation(1L, "브라운", Status.PAYMENT_PENDING, now);
        ReservationSlot reservationSlot = createReservationSlot(now, new ArrayList<>(List.of(reservation)));

        int order = reservationSlot.calculateOrder(reservation);

        assertThat(order).isZero();
    }

    @Test
    @DisplayName("대기 예약의 순서를 계산한다")
    void 대기_예약_순서() {
        LocalDateTime now = LocalDateTime.now();
        Reservation firstWaiting = createReservation(2L, "도니", Status.WAITING, now.plusMinutes(1));
        Reservation secondWaiting = createReservation(3L, "모디", Status.WAITING, now.plusMinutes(2));
        ReservationSlot reservationSlot = createReservationSlot(now, new ArrayList<>(List.of(
                createReservation(1L, "브라운", Status.PAYMENT_PENDING, now),
                firstWaiting,
                secondWaiting
        )));

        int order = reservationSlot.calculateOrder(secondWaiting);

        assertThat(order).isEqualTo(2);
    }

    @Test
    @DisplayName("같은 이름으로 같은 슬롯에 예약하면 예외가 발생한다")
    void 같은_이름_예약_예외() {
        LocalDateTime now = LocalDateTime.now();
        ReservationSlot reservationSlot = createReservationSlot(now, new ArrayList<>(List.of(
                createReservation(1L, "브라운", Status.PAYMENT_PENDING, now)
        )));

        assertThatThrownBy(() -> reservationSlot.reserve("브라운", now.plusMinutes(1)))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.ALREADY_EXISTS_RESERVATION.getMessage());
    }

    @Test
    @DisplayName("지난 예약 슬롯에 예약하면 예외가 발생한다")
    void 지난_슬롯_예약_예외() {
        LocalDateTime now = LocalDateTime.now();
        ReservationSlot reservationSlot = createReservationSlot(now.minusDays(2), List.of());

        assertThatThrownBy(() -> reservationSlot.reserve("브라운", now))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("확정 예약을 삭제하면 예약이 취소된다")
    void 확정_예약_삭제() {
        LocalDateTime now = LocalDateTime.now();
        Reservation reservation = createReservation(1L, "브라운", Status.RESERVED, now);
        ReservationSlot reservationSlot = createReservationSlot(now, new ArrayList<>(List.of(reservation)));

        ReservationDeletion deletedReservation = reservationSlot.deleteReservation(1L, "브라운", now.plusMinutes(1));

        assertThat(deletedReservation.deletedReservation().getStatus()).isEqualTo(Status.CANCELED);
    }

    @Test
    @DisplayName("확정 예약을 삭제하면 첫 번째 대기 예약이 결제 대기로 승격된다")
    void 확정_삭제시_대기_승격() {
        LocalDateTime now = LocalDateTime.now();
        Reservation firstWaiting = createReservation(2L, "도니", Status.WAITING, now.plusMinutes(1));
        Reservation secondWaiting = createReservation(3L, "모디", Status.WAITING, now.plusMinutes(2));
        ReservationSlot reservationSlot = createReservationSlot(now, new ArrayList<>(List.of(
                createReservation(1L, "브라운", Status.RESERVED, now),
                firstWaiting,
                secondWaiting
        )));

        reservationSlot.deleteReservation(1L, "브라운", now.plusMinutes(3));

        assertThat(firstWaiting.getStatus()).isEqualTo(Status.PAYMENT_PENDING);
        assertThat(secondWaiting.getStatus()).isEqualTo(Status.WAITING);
    }

    @Test
    @DisplayName("결제 대기 예약을 삭제하면 첫 번째 대기 예약이 결제 대기로 승격된다")
    void 결제_대기_삭제시_대기_승격() {
        LocalDateTime now = LocalDateTime.now();
        Reservation firstWaiting = createReservation(2L, "도니", Status.WAITING, now.plusMinutes(1));
        Reservation secondWaiting = createReservation(3L, "모디", Status.WAITING, now.plusMinutes(2));
        ReservationSlot reservationSlot = createReservationSlot(now, new ArrayList<>(List.of(
                createReservation(1L, "브라운", Status.PAYMENT_PENDING, now),
                firstWaiting,
                secondWaiting
        )));

        reservationSlot.deleteReservation(1L, "브라운", now.plusMinutes(3));

        assertThat(firstWaiting.getStatus()).isEqualTo(Status.PAYMENT_PENDING);
        assertThat(secondWaiting.getStatus()).isEqualTo(Status.WAITING);
    }

    private ReservationSlot createReservationSlot(LocalDateTime now, List<Reservation> reservations) {
        return new ReservationSlot(
                new ReservationSlotInfo(1L, now.plusDays(1).toLocalDate(), new Time(1L, RESERVATION_TIME), createTheme()),
                new ArrayList<>(reservations)
        );
    }

    private Reservation createReservation(Long id, String name, Status status, LocalDateTime updateAt) {
        return new Reservation(id, name, 1L, status, updateAt);
    }

    private Theme createTheme() {
        return new Theme(
                1L,
                "공포의 저택",
                "버려진 저택에서 탈출하라",
                "https://example.com/image.png",
                10000L
        );
    }
}
