package roomescape.time.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import roomescape.reservation.exception.PastReservationException;

class ReservationTimeTest {

    private static final LocalDateTime FUTURE_START = LocalDateTime.now().plusDays(1).withNano(0);
    private static final LocalDateTime FUTURE_END = FUTURE_START.plusHours(2);
    private static final LocalDateTime PAST_START = LocalDateTime.now().minusDays(1).withNano(0);
    private static final LocalDateTime PAST_END = PAST_START.plusHours(2);

    @DisplayName("시작/종료 시간이 유효하면 객체가 생성된다.")
    @Test
    void 객체_생성_테스트() {
        // when
        ReservationTime time = new ReservationTime(1L, FUTURE_START, FUTURE_END);

        // then
        assertThat(time.getId()).isEqualTo(1L);
        assertThat(time.getStartAt()).isEqualTo(FUTURE_START);
        assertThat(time.getEndAt()).isEqualTo(FUTURE_END);
    }

    @DisplayName("시작 시간이 null이면 IllegalArgumentException이 발생한다.")
    @Test
    void 시작시간_null이면_예외() {
        // when & then
        assertThatThrownBy(() -> new ReservationTime(null, FUTURE_END))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("시작 시간과 종료 시간은 비어있을 수 없습니다.");
    }

    @DisplayName("종료 시간이 null이면 IllegalArgumentException이 발생한다.")
    @Test
    void 종료시간_null이면_예외() {
        // when & then
        assertThatThrownBy(() -> new ReservationTime(FUTURE_START, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("시작 시간과 종료 시간은 비어있을 수 없습니다.");
    }

    @DisplayName("종료 시간이 시작 시간 이전이면 IllegalArgumentException이 발생한다.")
    @Test
    void 종료시간이_시작시간보다_이전이면_예외() {
        // when & then
        assertThatThrownBy(() -> new ReservationTime(FUTURE_END, FUTURE_START))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("종료 시간은 시작 시간 이후여야 합니다.");
    }

    @DisplayName("종료 시간이 시작 시간과 같으면 IllegalArgumentException이 발생한다.")
    @Test
    void 종료시간이_시작시간과_같으면_예외() {
        // when & then
        assertThatThrownBy(() -> new ReservationTime(FUTURE_START, FUTURE_START))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("종료 시간은 시작 시간 이후여야 합니다.");
    }

    @DisplayName("startAt으로부터 날짜를 반환한다.")
    @Test
    void getDate_날짜_반환_테스트() {
        // given
        LocalDateTime start = LocalDateTime.of(2030, 6, 1, 10, 0);
        ReservationTime time = new ReservationTime(1L, start, start.plusHours(2));

        // when
        LocalDate date = time.getDate();

        // then
        assertThat(date).isEqualTo(LocalDate.of(2030, 6, 1));
    }

    @DisplayName("미래 시간은 예약 가능하다.")
    @Test
    void validateReservableSchedule_미래이면_정상() {
        // given
        ReservationTime time = new ReservationTime(1L, FUTURE_START, FUTURE_END);

        // when & then
        assertThatCode(time::validateReservableSchedule)
                .doesNotThrowAnyException();
    }

    @DisplayName("과거 시간에 예약을 시도하면 PastReservationException이 발생한다.")
    @Test
    void validateReservableSchedule_과거이면_예외() {
        // given
        ReservationTime time = new ReservationTime(1L, PAST_START, PAST_END);

        // when & then
        assertThatThrownBy(time::validateReservableSchedule)
                .isInstanceOf(PastReservationException.class);
    }

    @DisplayName("미래 시간은 변경 가능하다.")
    @Test
    void validateUpdatableReservation_미래이면_정상() {
        // given
        ReservationTime time = new ReservationTime(1L, FUTURE_START, FUTURE_END);

        // when & then
        assertThatCode(time::validateUpdatableReservation)
                .doesNotThrowAnyException();
    }

    @DisplayName("과거 시간을 변경하려고 하면 PastReservationException이 발생한다.")
    @Test
    void validateUpdatableReservation_과거이면_예외() {
        // given
        ReservationTime time = new ReservationTime(1L, PAST_START, PAST_END);

        // when & then
        assertThatThrownBy(time::validateUpdatableReservation)
                .isInstanceOf(PastReservationException.class);
    }

    @DisplayName("미래 시간은 취소 가능하다.")
    @Test
    void validateNotPastForCancel_미래이면_정상() {
        // given
        ReservationTime time = new ReservationTime(1L, FUTURE_START, FUTURE_END);

        // when & then
        assertThatCode(time::validateNotPastForCancel)
                .doesNotThrowAnyException();
    }

    @DisplayName("과거 시간을 취소하려고 하면 PastReservationException이 발생한다.")
    @Test
    void validateNotPastForCancel_과거이면_예외() {
        // given
        ReservationTime time = new ReservationTime(1L, PAST_START, PAST_END);

        // when & then
        assertThatThrownBy(time::validateNotPastForCancel)
                .isInstanceOf(PastReservationException.class);
    }
}
