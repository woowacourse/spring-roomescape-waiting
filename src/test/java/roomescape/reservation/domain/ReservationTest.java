package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.reservation.domain.exception.ReservationCancellationException;
import roomescape.reservation.domain.exception.ReservationModificationException;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

class ReservationTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 8, 10, 30);
    private static final ReservationTime PAST_TIME = ReservationTime.of(1L, LocalTime.of(10, 0));
    private static final ReservationTime FUTURE_TIME = ReservationTime.of(2L, LocalTime.of(11, 0));
    private static final Theme THEME = Theme.of(1L, "링", "공포 테마", "http:~");

    @Test
    void 현재_이전_시간으로_예약을_생성할_수_없다() {
        assertThatThrownBy(() -> Reservation.create(
                "브라운",
                LocalDate.of(2026, 5, 8),
                PAST_TIME,
                THEME,
                NOW
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 현재_이후_시간으로_예약을_생성한다() {
        Reservation reservation = Reservation.create(
                "브라운",
                LocalDate.of(2026, 5, 8),
                FUTURE_TIME,
                THEME,
                NOW
        );

        assertThat(reservation.getCustomerName()).isEqualTo("브라운");
    }

    @Test
    void 과거_예약도_복원할_수_있다() {
        Reservation reservation = Reservation.of(
                1L,
                "브라운",
                LocalDate.of(2026, 5, 8),
                PAST_TIME,
                THEME
        );

        assertThat(reservation.getDate()).isEqualTo(LocalDate.of(2026, 5, 8));
    }

    @Test
    void 예약_일정을_변경한다() {
        Reservation reservation = Reservation.of(
                1L,
                "브라운",
                LocalDate.of(2026, 5, 8),
                FUTURE_TIME,
                THEME
        );
        ReservationTime newTime = ReservationTime.of(3L, LocalTime.of(12, 0));

        Reservation changed = reservation.changeSchedule(
                LocalDate.of(2026, 5, 9),
                newTime,
                NOW
        );

        assertThat(changed.getId()).isEqualTo(1L);
        assertThat(changed.getCustomerName()).isEqualTo("브라운");
        assertThat(changed.getDate()).isEqualTo(LocalDate.of(2026, 5, 9));
        assertThat(changed.getTime()).isEqualTo(newTime);
        assertThat(changed.getTheme()).isEqualTo(THEME);
    }

    @Test
    void 과거_시간으로_예약_일정을_변경할_수_없다() {
        Reservation reservation = Reservation.of(
                1L,
                "브라운",
                LocalDate.of(2026, 5, 9),
                FUTURE_TIME,
                THEME
        );

        assertThatThrownBy(() -> reservation.changeSchedule(
                LocalDate.of(2026, 5, 8),
                PAST_TIME,
                NOW
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 예약일_당일에는_예약_시작_전이어도_사용자가_예약을_취소할_수_없다() {
        Reservation reservation = Reservation.of(
                1L,
                "브라운",
                LocalDate.of(2026, 5, 8),
                FUTURE_TIME,
                THEME
        );

        assertThatThrownBy(() -> reservation.validateCancelableByCustomer(LocalDate.of(2026, 5, 8)))
                .isInstanceOf(ReservationCancellationException.class);
    }

    @Test
    void 예약일_하루_전에는_사용자가_예약을_취소할_수_있다() {
        Reservation reservation = Reservation.of(
                1L,
                "브라운",
                LocalDate.of(2026, 5, 9),
                FUTURE_TIME,
                THEME
        );

        assertThatCode(() -> reservation.validateCancelableByCustomer(LocalDate.of(2026, 5, 8)))
                .doesNotThrowAnyException();
    }

    @Test
    void 예약일_당일에는_예약_시작_전이어도_사용자가_예약을_변경할_수_없다() {
        Reservation reservation = Reservation.of(
                1L,
                "브라운",
                LocalDate.of(2026, 5, 8),
                FUTURE_TIME,
                THEME
        );

        assertThatThrownBy(() -> reservation.validateModifiableByCustomer(LocalDate.of(2026, 5, 8)))
                .isInstanceOf(ReservationModificationException.class);
    }

    @Test
    void 예약일_하루_전에는_사용자가_예약을_변경할_수_있다() {
        Reservation reservation = Reservation.of(
                1L,
                "브라운",
                LocalDate.of(2026, 5, 9),
                FUTURE_TIME,
                THEME
        );

        assertThatCode(() -> reservation.validateModifiableByCustomer(LocalDate.of(2026, 5, 8)))
                .doesNotThrowAnyException();
    }

    @Nested
    @DisplayName("슬롯이 미래 인지 조회한다")
    class IsFutureSlot {

        @Test
        void 예약_시간이_미래이면_TRUE를_반환한다() {
            // given
            Reservation reservation = Reservation.of(
                1L,
                "수달",
                NOW.toLocalDate(),
                FUTURE_TIME,
                THEME
            );

            // when
            final boolean isFutureSlot = reservation.isFutureSlot(NOW);

            // then
            assertThat(isFutureSlot).isTrue();
        }

        @Test
        void 예약_시간이_과거면_FALSE를_반환한다() {
            // given
            Reservation reservation = Reservation.of(
                1L,
                "수달",
                NOW.toLocalDate(),
                PAST_TIME,
                THEME
            );

            // when
            final boolean isFutureSlot = reservation.isFutureSlot(NOW);

            // then
            assertThat(isFutureSlot).isFalse();
        }
    }
}
