package roomescape.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.ReservationTestFixture;
import roomescape.member.model.Member;
import roomescape.member.model.Role;
import roomescape.reservation.model.dto.ReservationDetails;
import roomescape.reservation.model.entity.Reservation;
import roomescape.reservation.model.entity.ReservationTheme;
import roomescape.reservation.model.entity.ReservationTime;
import roomescape.reservation.model.exception.ReservationException.InvalidReservationTimeException;

class  ReservationTest {

    @DisplayName("예약을 생성할 수 있다")
    @Test
    void createFutureReservationSuccess() {
        //given
        ReservationDetails details = new ReservationDetails(
                new Member("1234", Role.USER, "a@a.com", "웨이드"),
                LocalDate.now().plusDays(10),
                new ReservationTime(LocalTime.of(10, 0)),
                new ReservationTheme("테마 이름", "테마 설명", "테마 url")
        );

        //when
        Reservation reservation = Reservation.createFutureReservation(details);

        //then
        assertThat(reservation.getDate()).isEqualTo(details.date());
        assertThat(reservation.getMember()).isEqualTo(details.member());
    }

    @DisplayName("예약 생성시 예약 시간이 과거 시간이면 예외를 발생시킨다")
    @Test
    void createFutureReservationExceptionIfPastGetTime() {
        //given
        ReservationDetails details = new ReservationDetails(
                new Member("1234", Role.USER, "a@a.com", "홍길동"),
                LocalDate.now().minusDays(1),
                new ReservationTime(LocalTime.of(10, 0)),
                new ReservationTheme("테마 이름", "테마 설명", "테마 url")
        );

        //when & then
        assertThatThrownBy(() -> Reservation.createFutureReservation(details))
                .isInstanceOf(InvalidReservationTimeException.class);
    }

    @DisplayName("예약 날짜와 시간을 LocalDateTime으로 반환한다 ")
    @Test
    void returnLocalDateTime() {
        //given
        LocalTime time = LocalTime.of(10, 0);
        LocalDate date = LocalDate.now().plusDays(10);
        ReservationDetails details = new ReservationDetails(
            new Member("1234", Role.USER, "a@a.com", "웨이드"),
            date,
            new ReservationTime(time),
            new ReservationTheme("테마 이름", "테마 설명", "테마 url")
        );

        //when
        Reservation reservation = Reservation.createFutureReservation(details);

        //then
        assertThat(reservation.getReservationDateTime()).isEqualTo(LocalDateTime.of(date, time));
    }
}
