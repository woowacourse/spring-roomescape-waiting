package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.support.exception.RoomescapeException;

class ReservationTest {

    @Test
    void id가_없는_예약을_생성한다() {
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
    void id를_부여한_예약을_생성한다() {
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
    void DB에서_조회한_예약을_생성한다() {
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
    void 이름이_null이면_예외가_발생한다() {
        // given
        String name = null;
        ReservationDate date = ReservationDate.createWithoutId(LocalDate.of(2023, 8, 5));
        ReservationTime time = ReservationTime.createWithoutId(LocalTime.of(15, 40));
        Theme theme = Theme.of(1L, "공포", "무서운 테마", "theme-url");

        // when & then
        assertThatThrownBy(() -> Reservation.createWithoutId(name, date, time, theme))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("예약자 성명 데이터가 유효하지 않습니다.");
    }

    @Test
    void 이름이_공백이면_예외가_발생한다() {
        // given
        String name = "            ";
        ReservationDate date = ReservationDate.createWithoutId(LocalDate.of(2023, 8, 5));
        ReservationTime time = ReservationTime.createWithoutId(LocalTime.of(15, 40));
        Theme theme = Theme.of(1L, "공포", "무서운 테마", "theme-url");

        // when & then
        assertThatThrownBy(() -> Reservation.createWithoutId(name, date, time, theme))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("예약자 성명 데이터가 유효하지 않습니다.");
    }

    @Test
    void 날짜가_null이면_예외가_발생한다() {
        // given
        String name = "보예";
        ReservationDate date = null;
        ReservationTime time = ReservationTime.createWithoutId(LocalTime.of(15, 40));
        Theme theme = Theme.of(1L, "공포", "무서운 테마", "theme-url");

        // when & hen
        assertThatThrownBy(() -> Reservation.createWithoutId(name, date, time, theme))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("예약 날짜 식별자 혹은 데이터가 누락되었습니다.");
    }

    @Test
    void 예약_시간이_null이면_예외가_발생한다() {
        // given
        String name = "보예";
        ReservationDate date = ReservationDate.createWithoutId(LocalDate.of(2023, 8, 5));
        ReservationTime time = null;
        Theme theme = Theme.of(1L, "공포", "무서운 테마", "theme-url");

        // when & then
        assertThatThrownBy(() -> Reservation.createWithoutId(name, date, time, theme))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("예약 시간 식별자 정보가 누락되었습니다.");
    }

    @Test
    void 테마가_null이면_예외가_발생한다() {
        // given
        String name = "보예";
        ReservationDate date = ReservationDate.createWithoutId(LocalDate.of(2023, 8, 5));
        ReservationTime time = ReservationTime.createWithoutId(LocalTime.of(15, 40));
        Theme theme = null;

        // when & then
        assertThatThrownBy(() -> Reservation.createWithoutId(name, date, time, theme))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("테마 엔티티 식별자 정보가 누락되었습니다.");
    }

    @Test
    void 날짜_시간을_업데이트_할_수_있다() {
        // given
        ReservationTime time = ReservationTime.createWithoutId(LocalTime.of(15, 40));
        ReservationTime updateTime =  ReservationTime.createWithoutId(LocalTime.of(10, 00));
        ReservationDate date = ReservationDate.createWithoutId(LocalDate.of(2026, 8, 5));
        ReservationDate updateDate = ReservationDate.createWithoutId(LocalDate.of(2026, 8, 15));
        Theme theme = Theme.of(1L, "공포", "무서운 테마", "theme-url");
        Reservation reservation = Reservation.createWithoutId("이산", date, time, theme);

        // when
        reservation.update(updateDate, updateTime);

        // then
        assertThat(reservation.getDate()).isEqualTo(updateDate);
        assertThat(reservation.getTime()).isEqualTo(updateTime);
    }
}
