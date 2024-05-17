package roomescape.core.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReservationTest {
    private static final Member member = new Member("리건", "test@email.com", "password", Role.ADMIN);
    private static final Theme theme = new Theme("테마", "테마 설명", "테마 이미지");
    private static final ReservationTime time = new ReservationTime("10:00");

    @Test
    @DisplayName("예약 날짜를 저장할 때, 문자열을 LocalDate 타입으로 변환한다.")
    void parseDate() {
        final String date = "2022-12-31";
        final Reservation reservation = new Reservation(member, date, time, theme);

        assertThat(reservation.getDate()).isEqualTo(LocalDate.of(2022, 12, 31));
    }

    @Test
    @DisplayName("예약 날짜 형식이 올바르지 않을 경우 예외가 발생한다.")
    void parseDateWithInvalidFormat() {
        final String date = "2222222222";

        assertThatThrownBy(() -> new Reservation(member, date, time, theme))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("날짜 형식이 잘못되었습니다.");
    }

    @Test
    @DisplayName("예약 날짜가 현재 날짜보다 이전인지 확인할 수 있다.")
    void isDatePast() {
        final String date = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_DATE);
        final Reservation reservation = new Reservation(member, date, time, theme);

        assertThatThrownBy(reservation::validateDateAndTime)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("지난 날짜에는 예약할 수 없습니다.");
    }

    @Test
    @DisplayName("예약 날짜가 오늘인지 확인할 수 있다.")
    void isDateToday() {
        final String date = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        final Reservation reservation = new Reservation(member, date, time, theme);

        assertThatThrownBy(reservation::validateDateAndTime)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("지난 시간에는 예약할 수 없습니다.");
    }
}
