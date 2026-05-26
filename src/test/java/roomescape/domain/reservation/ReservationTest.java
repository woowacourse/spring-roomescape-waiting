package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.support.exception.RoomescapeException;

class ReservationTest {

    @Test
    @DisplayName("id가 없는 예약을 생성한다.")
    void createReservationWithoutId() {
        // given
        String name = "보예";
        ReservationDate date = ReservationDate.createWithoutId(LocalDate.of(2023, 8, 5));
        ReservationTime time = ReservationTime.createWithoutId(LocalTime.of(15, 40));
        Theme theme = Theme.of(1L, "공포", "무서운 테마", "theme-url");

        // when
        Reservation reservation = Reservation.createWithoutId(name, date, time, theme);

        // then
        assertSoftly(softly -> {
                softly.assertThat(reservation.getId()).isNull();
                softly.assertThat(reservation.getName()).isEqualTo(name);
                softly.assertThat(reservation.getDate()).isEqualTo(date);
                softly.assertThat(reservation.getTime()).isEqualTo(time);
                softly.assertThat(reservation.getTheme()).isEqualTo(theme);
            }
        );
    }

    @Test
    @DisplayName("id를 부여한 예약을 생성한다.")
    void createReservationWithId() {
        // given
        ReservationTime time = ReservationTime.createWithoutId(LocalTime.of(15, 40));
        ReservationDate date = ReservationDate.createWithoutId(LocalDate.of(2023, 8, 5));
        Theme theme = Theme.of(1L, "공포", "무서운 테마", "theme-url");
        Reservation reservation = Reservation.createWithoutId(
            "보예",
            date,
            time,
            theme
        );

        // when
        Reservation reservationWithId = Reservation.of(
            1L,
            reservation.getName(),
            reservation.getDate(),
            reservation.getTime(),
            reservation.getTheme()
        );

        // then
        assertSoftly(softly -> {
                assertThat(reservationWithId.getId()).isEqualTo(1L);
                assertThat(reservationWithId.getName()).isEqualTo("보예");
                assertThat(reservationWithId.getDate()).isEqualTo(date);
                assertThat(reservationWithId.getTime()).isEqualTo(time);
                assertThat(reservationWithId.getTheme()).isEqualTo(theme);
            }
        );
    }

    @Test
    @DisplayName("DB에서 조회한 예약을 생성한다.")
    void createReservationLoadedFromDatabase() {
        // given
        long id = 1L;
        String name = "보예";
        ReservationDate date = ReservationDate.of(2L, LocalDate.of(2023, 8, 5));
        ReservationTime time = ReservationTime.createWithoutId(LocalTime.of(15, 40));
        Theme theme = Theme.of(1L, "공포", "무서운 테마", "theme-url");

        // when
        Reservation reservation = Reservation.of(id, name, date, time, theme);

        // then
        assertSoftly(softly -> {
                assertThat(reservation.getId()).isEqualTo(id);
                assertThat(reservation.getName()).isEqualTo(name);
                assertThat(reservation.getDate()).isEqualTo(date);
                assertThat(reservation.getTime()).isEqualTo(time);
                assertThat(reservation.getTheme()).isEqualTo(theme);
            }
        );
    }

    @Test
    @DisplayName("이름이 null이면 예외가 발생한다.")
    void throwExceptionWhenNameIsNull() {
        // given
        String name = null;
        ReservationDate date = ReservationDate.createWithoutId(LocalDate.of(2023, 8, 5));
        ReservationTime time = ReservationTime.createWithoutId(LocalTime.of(15, 40));
        Theme theme = Theme.of(1L, "공포", "무서운 테마", "theme-url");

        // when & then
        assertThatThrownBy(() -> Reservation.createWithoutId(name, date, time, theme))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("이름은 비어 있을 수 없습니다.");
    }

    @Test
    @DisplayName("이름이 공백이면 예외가 발생한다.")
    void throwExceptionWhenNameIsBlank() {
        // given
        String name = "            ";
        ReservationDate date = ReservationDate.createWithoutId(LocalDate.of(2023, 8, 5));
        ReservationTime time = ReservationTime.createWithoutId(LocalTime.of(15, 40));
        Theme theme = Theme.of(1L, "공포", "무서운 테마", "theme-url");

        // when & then
        assertThatThrownBy(() -> Reservation.createWithoutId(name, date, time, theme))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("이름은 비어 있을 수 없습니다.");
    }

    @Test
    @DisplayName("이름이 10자를 초과하면 예외가 발생한다.")
    void throwExceptionWhenNameExceedsTenCharacters() {
        // given
        String name = "보예보예보예보예보예보";
        ReservationDate date = ReservationDate.createWithoutId(LocalDate.of(2023, 8, 5));
        ReservationTime time = ReservationTime.createWithoutId(LocalTime.of(15, 40));
        Theme theme = Theme.of(1L, "공포", "무서운 테마", "theme-url");

        // when & then
        assertThatThrownBy(() -> Reservation.createWithoutId(name, date, time, theme))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("이름은 10자 이하여야 합니다.");
    }

    @Test
    @DisplayName("날짜가 null이면 예외가 발생한다.")
    void throwExceptionWhenDateIsNull() {
        // given
        String name = "보예";
        ReservationDate date = null;
        ReservationTime time = ReservationTime.createWithoutId(LocalTime.of(15, 40));
        Theme theme = Theme.of(1L, "공포", "무서운 테마", "theme-url");

        // when & hen
        assertThatThrownBy(() -> Reservation.createWithoutId(name, date, time, theme))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("날짜는 필수입니다.");
    }

    @Test
    @DisplayName("예약 시간이 null이면 예외가 발생한다.")
    void throwExceptionWhenReservationTimeIsNull() {
        // given
        String name = "보예";
        ReservationDate date = ReservationDate.createWithoutId(LocalDate.of(2023, 8, 5));
        ReservationTime time = null;
        Theme theme = Theme.of(1L, "공포", "무서운 테마", "theme-url");

        // when & then
        assertThatThrownBy(() -> Reservation.createWithoutId(name, date, time, theme))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("시간은 필수입니다.");
    }

    @Test
    @DisplayName("테마가 null이면 예외가 발생한다.")
    void throwExceptionWhenThemeIsNull() {
        // given
        String name = "보예";
        ReservationDate date = ReservationDate.createWithoutId(LocalDate.of(2023, 8, 5));
        ReservationTime time = ReservationTime.createWithoutId(LocalTime.of(15, 40));
        Theme theme = null;

        // when & then
        assertThatThrownBy(() -> Reservation.createWithoutId(name, date, time, theme))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("테마는 필수입니다.");
    }
}
