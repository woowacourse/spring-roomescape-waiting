package roomescape.reservationwait;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import roomescape.reservationtime.ReservationTime;
import roomescape.reservationwait.exception.PastReservationWaitNotAllowedException;
import roomescape.reservationwait.exception.SelfReservationWaitNotAllowedException;
import roomescape.reservation.Reservation;

public class ReservationWaitTest {

    private static final LocalDateTime SAMPLE_CREATED_AT = LocalDateTime.of(2026, 5, 26, 0, 0);
    private static final ReservationTime SAMPLE_TIME = new ReservationTime(1L, LocalTime.of(10, 0));

    @Test
    void reservationId가_null이면_예약대기를_생성할_수_없다() {
        assertThatThrownBy(() -> new ReservationWait(
                1L,
                null,
                1L,
                SAMPLE_CREATED_AT
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약 ID는 null일 수 없습니다.");
    }

    @Test
    void memberId가_null이면_예약대기를_생성할_수_없다() {
        assertThatThrownBy(() -> new ReservationWait(
                1L,
                1L,
                null,
                SAMPLE_CREATED_AT
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("회원 ID는 null일 수 없습니다.");
    }

    @Test
    void create는_과거_예약이면_PastReservationWaitNotAllowedException() {
        // given: 과거 예약 + 다른 사용자의 대기 시도
        long ownerId = 1L;
        long requesterId = 2L;
        Reservation pastReservation = new Reservation(
                10L, ownerId, LocalDate.now().minusDays(1), SAMPLE_TIME, 1L, 1L);

        // when & then: 과거 예약 대기 불가
        assertThatThrownBy(() -> ReservationWait.create(pastReservation, requesterId))
                .isInstanceOf(PastReservationWaitNotAllowedException.class);
    }

    @Test
    void create는_본인_예약이면_SelfReservationWaitNotAllowedException() {
        // given: 본인이 본인 예약에 대기 시도
        long ownerId = 1L;
        Reservation reservation = new Reservation(
                10L, ownerId, LocalDate.now().plusDays(1), SAMPLE_TIME, 1L, 1L);

        // when & then: 자기 자신 대기 불가
        assertThatThrownBy(() -> ReservationWait.create(reservation, ownerId))
                .isInstanceOf(SelfReservationWaitNotAllowedException.class);
    }

    @Test
    void create는_정상이면_id가_null인_ReservationWait를_반환한다() {
        // given: 미래 예약 + 다른 사용자
        long ownerId = 1L;
        long requesterId = 2L;
        Reservation reservation = new Reservation(
                10L, ownerId, LocalDate.now().plusDays(1), SAMPLE_TIME, 1L, 1L);

        // when: 정적 팩토리로 대기 생성
        ReservationWait wait = ReservationWait.create(reservation, requesterId);

        // then: id 는 null (영속 전), reservationId / memberId 반영
        assertThat(wait.getId()).isNull();
        assertThat(wait.getReservationId()).isEqualTo(10L);
        assertThat(wait.getMemberId()).isEqualTo(requesterId);
    }
}
