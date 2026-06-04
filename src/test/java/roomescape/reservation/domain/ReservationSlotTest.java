package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.common.exception.RoomescapeException;

class ReservationSlotTest {

    @Test
    @DisplayName("id가 없는 예약 슬롯을 생성한다.")
    void createReservationWithoutId() {
        // given
        ReservationDate date = ReservationDate.createWithoutId(LocalDate.of(2023, 8, 5));
        ReservationTime time = ReservationTime.createWithoutId(LocalTime.of(15, 40));
        Theme theme = Theme.of(1L, "공포", "무서운 테마", "theme-url");

        // when
        ReservationSlot reservation = ReservationSlot.createWithoutId(date, time, theme);

        // then
        assertSoftly(softly -> {
                softly.assertThat(reservation.getId()).isNull();
                softly.assertThat(reservation.getDate()).isEqualTo(date);
                softly.assertThat(reservation.getTime()).isEqualTo(time);
                softly.assertThat(reservation.getTheme()).isEqualTo(theme);
            }
        );
    }

    @Test
    @DisplayName("id를 부여한 예약 슬롯을 생성한다.")
    void createReservationWithId() {
        // given
        ReservationTime time = ReservationTime.createWithoutId(LocalTime.of(15, 40));
        ReservationDate date = ReservationDate.createWithoutId(LocalDate.of(2023, 8, 5));
        Theme theme = Theme.of(1L, "공포", "무서운 테마", "theme-url");
        ReservationSlot reservation = ReservationSlot.createWithoutId(
            date,
            time,
            theme
        );

        // when
        ReservationSlot reservationWithId = ReservationSlot.of(
            1L,
            reservation.getDate(),
            reservation.getTime(),
            reservation.getTheme()
        );

        // then
        assertSoftly(softly -> {
                assertThat(reservationWithId.getId()).isEqualTo(1L);
                assertThat(reservationWithId.getDate()).isEqualTo(date);
                assertThat(reservationWithId.getTime()).isEqualTo(time);
                assertThat(reservationWithId.getTheme()).isEqualTo(theme);
            }
        );
    }

    @Test
    @DisplayName("DB에서 조회한 예약 슬롯을 생성한다.")
    void createReservationLoadedFromDatabase() {
        // given
        long id = 1L;
        ReservationDate date = ReservationDate.of(2L, LocalDate.of(2023, 8, 5));
        ReservationTime time = ReservationTime.createWithoutId(LocalTime.of(15, 40));
        Theme theme = Theme.of(1L, "공포", "무서운 테마", "theme-url");

        // when
        ReservationSlot reservation = ReservationSlot.of(id, date, time, theme);

        // then
        assertSoftly(softly -> {
                assertThat(reservation.getId()).isEqualTo(id);
                assertThat(reservation.getDate()).isEqualTo(date);
                assertThat(reservation.getTime()).isEqualTo(time);
                assertThat(reservation.getTheme()).isEqualTo(theme);
            }
        );
    }

    @Test
    @DisplayName("날짜가 null이면 예외가 발생한다.")
    void throwExceptionWhenDateIsNull() {
        // given
        ReservationDate date = null;
        ReservationTime time = ReservationTime.createWithoutId(LocalTime.of(15, 40));
        Theme theme = Theme.of(1L, "공포", "무서운 테마", "theme-url");

        // when & hen
        assertThatThrownBy(() -> ReservationSlot.createWithoutId(date, time, theme))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("날짜는 필수입니다.");
    }

    @Test
    @DisplayName("예약 시간이 null이면 예외가 발생한다.")
    void throwExceptionWhenReservationTimeIsNull() {
        // given
        ReservationDate date = ReservationDate.createWithoutId(LocalDate.of(2023, 8, 5));
        ReservationTime time = null;
        Theme theme = Theme.of(1L, "공포", "무서운 테마", "theme-url");

        // when & then
        assertThatThrownBy(() -> ReservationSlot.createWithoutId(date, time, theme))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("시간은 필수입니다.");
    }

    @Test
    @DisplayName("테마가 null이면 예외가 발생한다.")
    void throwExceptionWhenThemeIsNull() {
        // given
        ReservationDate date = ReservationDate.createWithoutId(LocalDate.of(2023, 8, 5));
        ReservationTime time = ReservationTime.createWithoutId(LocalTime.of(15, 40));
        Theme theme = null;

        // when & then
        assertThatThrownBy(() -> ReservationSlot.createWithoutId(date, time, theme))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("테마는 필수입니다.");
    }
}
