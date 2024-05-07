package roomescape.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.model.member.Member;
import roomescape.model.member.Role;
import roomescape.model.theme.Theme;
import roomescape.service.dto.ReservationDto;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.*;

public class ReservationTest {

    @DisplayName("데이터의 id가 0 이하인 경우 예외를 발생시킨다.")
    @ParameterizedTest
    @ValueSource(longs = {0, -1, -999})
    void should_throw_exception_when_invalid_id(long id) {
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.now());
        Theme theme = new Theme(1L, "n", "d", "t");
        Member member = new Member(1L, "에버", "treeboss@gmail.com", "treeboss123!", Role.USER);
        assertThatThrownBy(() -> new Reservation(id, LocalDate.now(), reservationTime, theme, member))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("id는 0 이하일 수 없습니다.");
    }

    @DisplayName("데이터의 사용자가 null인 경우 예외를 발생시킨다.")
    @Test
    void should_throw_exception_when_invalid_member() {
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.now());
        Theme theme = new Theme(1L, "n", "d", "t");
        assertThatThrownBy(() -> new Reservation(1L, LocalDate.now(), reservationTime, theme, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("데이터는 null일 수 없습니다.");
    }

    @DisplayName("데이터의 날짜가 null인 경우 예외를 발생시킨다.")
    @Test
    void should_throw_exception_when_invalid_date() {
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.now());
        Theme theme = new Theme(1L, "n", "d", "t");
        Member member = new Member(1L, "에버", "treeboss@gmail.com", "treeboss123!", Role.USER);
        assertThatThrownBy(() -> new Reservation(1L, null, reservationTime, theme, member))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("데이터는 null일 수 없습니다.");
    }

    @DisplayName("데이터의 시간이 null인 경우 예외를 발생시킨다.")
    @Test
    void should_throw_exception_when_invalid_time() {
        ReservationTime reservationTime = null;
        Theme theme = new Theme(1L, "n", "d", "t");
        Member member = new Member(1L, "에버", "treeboss@gmail.com", "treeboss123!", Role.USER);
        assertThatThrownBy(() -> new Reservation(1L, LocalDate.now(), reservationTime, theme, member))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("데이터는 null일 수 없습니다.");
    }

    @DisplayName("데이터의 테마가 null인 경우 예외를 발생시킨다.")
    @Test
    void should_throw_exception_when_invalid_theme() {
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.now());
        Theme theme = null;
        Member member = new Member(1L, "에버", "treeboss@gmail.com", "treeboss123!", Role.USER);
        assertThatThrownBy(() -> new Reservation(1L, LocalDate.now(), reservationTime, theme, member))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("데이터는 null일 수 없습니다.");
    }

    @DisplayName("데이터가 유효한 경우 예외가 발생하지 않는다.")
    @ParameterizedTest
    @CsvSource(value = {"1,nnn,2024-05-07,02:00,ddd,ttt", " 1 ,n nn,2024-05-07,02:00,  dd  d, t!tt "})
    void should_not_throw_exception_when_valid_data(long id, String name, String rawDate, String rawTime, String description, String thumbnail) {
        LocalDate date = LocalDate.parse(rawDate);
        LocalTime time = LocalTime.parse(rawTime);
        ReservationTime reservationTime = new ReservationTime(1L, time);
        Theme theme = new Theme(1L, name, description, thumbnail);
        Member member = new Member(1L, "에버", "treeboss@gmail.com", "treeboss123!", Role.USER);
        assertThatCode(() -> new Reservation(id, date, reservationTime, theme, member))
                .doesNotThrowAnyException();
    }

    @DisplayName("DTO를 도메인으로 변환하는 경우 id는 0이 될 수 있다.")
    @Test
    void should_be_zero_id_when_convert_dto() {
        ReservationTime time = new ReservationTime(1L, LocalTime.now());
        Theme theme = new Theme(1L, "n", "d", "t");
        Member member = new Member(1L, "에버", "treeboss@gmail.com", "treeboss123!", Role.USER);
        ReservationDto ReservationDto = new ReservationDto(LocalDate.now(), 1L, 1L, 1L);
        assertThatCode(() -> {
            Reservation reservation = Reservation.of(ReservationDto, time, theme, member);
            assertThat(reservation.getId()).isEqualTo(0);
        }).doesNotThrowAnyException();
    }
}
