package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.exception.BusinessException;
import roomescape.domain.exception.ErrorCode;
import roomescape.domain.theme.Theme;
import roomescape.domain.user.User;

@DisplayName("예약")
class ReservationTest {

    @Test
    @DisplayName("생성하면 대기 상태로 시작한다")
    void create() {
        // given
        User user = User.of(1L, "홍길동");
        ReservationSlot slot = ReservationSlot.of(
                10L,
                LocalDate.of(2030, 1, 1),
                ReservationTime.of(20L, LocalTime.of(13, 0)),
                Theme.of(30L, "도심 탈출", "도심 탈출 설명", "/themes/chase")
        );

        // when
        Reservation reservation = Reservation.create(user, slot, LocalDateTime.of(2030, 1, 1, 10, 0));

        // then
        assertThat(reservation.getId()).isNull();
        assertThat(reservation.getUser()).isEqualTo(user);
        assertThat(reservation.getSlot()).isEqualTo(slot);
        assertThat(reservation.getWaitingNumber()).isNull();
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.WAITING);
    }

    @Test
    @DisplayName("조회 결과를 그대로 담는다")
    void of() {
        // given
        User user = User.of(1L, "홍길동");
        ReservationSlot slot = ReservationSlot.of(
                10L,
                LocalDate.of(2030, 1, 1),
                ReservationTime.of(20L, LocalTime.of(13, 0)),
                Theme.of(30L, "도심 탈출", "도심 탈출 설명", "/themes/chase")
        );

        // when
        Reservation reservation = Reservation.of(
                100L,
                user,
                slot,
                1,
                ReservationStatus.CONFIRMED,
                LocalDateTime.of(2030, 1, 1, 10, 0)
        );

        // then
        assertThat(reservation.getId()).isEqualTo(100L);
        assertThat(reservation.getUser()).isEqualTo(user);
        assertThat(reservation.getSlot()).isEqualTo(slot);
        assertThat(reservation.getWaitingNumber()).isEqualTo(1);
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    @DisplayName("예약을 확정 상태로 바꿀 수 있다")
    void updateConfirmed() {
        // given
        Reservation reservation = Reservation.of(
                100L,
                User.of(1L, "홍길동"),
                ReservationSlot.of(
                        10L,
                        LocalDate.of(2030, 1, 1),
                        ReservationTime.of(20L, LocalTime.of(13, 0)),
                        Theme.of(30L, "도심 탈출", "도심 탈출 설명", "/themes/chase")
                ),
                3,
                ReservationStatus.WAITING,
                LocalDateTime.of(2030, 1, 1, 10, 0)
        );

        // when
        Reservation updated = reservation.updateConfirmed();

        // then
        assertThat(updated.getId()).isEqualTo(100L);
        assertThat(updated.getWaitingNumber()).isEqualTo(0);
        assertThat(updated.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(updated.getUser()).isEqualTo(reservation.getUser());
        assertThat(updated.getSlot()).isEqualTo(reservation.getSlot());
    }

    @Test
    @DisplayName("예약을 대기 상태로 바꿀 수 있다")
    void updateWaiting() {
        // given
        Reservation reservation = Reservation.of(
                100L,
                User.of(1L, "홍길동"),
                ReservationSlot.of(
                        10L,
                        LocalDate.of(2030, 1, 1),
                        ReservationTime.of(20L, LocalTime.of(13, 0)),
                        Theme.of(30L, "도심 탈출", "도심 탈출 설명", "/themes/chase")
                ),
                0,
                ReservationStatus.CONFIRMED,
                LocalDateTime.of(2030, 1, 1, 10, 0)
        );

        // when
        Reservation updated = reservation.updateWaiting(2);

        // then
        assertThat(updated.getId()).isEqualTo(100L);
        assertThat(updated.getWaitingNumber()).isEqualTo(2);
        assertThat(updated.getStatus()).isEqualTo(ReservationStatus.WAITING);
        assertThat(updated.getUser()).isEqualTo(reservation.getUser());
        assertThat(updated.getSlot()).isEqualTo(reservation.getSlot());
    }

    @Test
    @DisplayName("취소 가능 여부를 확인할 수 있다")
    void validateCancellable() {
        // given
        Reservation reservation = Reservation.of(
                100L,
                User.of(1L, "홍길동"),
                ReservationSlot.of(
                        10L,
                        LocalDate.of(2030, 1, 1),
                        ReservationTime.of(20L, LocalTime.of(13, 0)),
                        Theme.of(30L, "도심 탈출", "도심 탈출 설명", "/themes/chase")
                ),
                0,
                ReservationStatus.CONFIRMED,
                LocalDateTime.of(2030, 1, 1, 10, 0)
        );

        // when & then
        reservation.validateCancellable(LocalDateTime.of(2030, 1, 1, 12, 0));
    }

    @Test
    @DisplayName("과거 슬롯의 예약은 취소할 수 없다")
    void validateCancellableWhenPast() {
        // given
        Reservation reservation = Reservation.of(
                100L,
                User.of(1L, "홍길동"),
                ReservationSlot.of(
                        10L,
                        LocalDate.of(2030, 1, 1),
                        ReservationTime.of(20L, LocalTime.of(13, 0)),
                        Theme.of(30L, "도심 탈출", "도심 탈출 설명", "/themes/chase")
                ),
                0,
                ReservationStatus.CONFIRMED,
                LocalDateTime.of(2030, 1, 1, 10, 0)
        );

        // when & then
        assertThatThrownBy(() -> reservation.validateCancellable(LocalDateTime.of(2030, 1, 1, 14, 0)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESERVATION_SLOT_IN_PAST);
    }
}
