package roomescape;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.member.model.Member;
import roomescape.member.model.Role;
import roomescape.reservation.model.entity.Reservation;
import roomescape.reservation.model.entity.ReservationTheme;
import roomescape.reservation.model.entity.ReservationTime;

public class ReservationTestFixture {

    public static ReservationTime getReservationTimeFixture() {
        return ReservationTime.builder()
            .startAt(LocalTime.of(13, 0))
            .build();
    }

    public static ReservationTheme getReservationThemeFixture() {
        return ReservationTheme.builder()
            .name("탈출")
            .description("탈출하는 내용")
            .thumbnail("a.com")
            .build();
    }

    public static Member getUserFixture() {
        return Member.builder()
            .name("웨이드")
            .email("wade@naver.com")
            .password("1234")
            .role(Role.USER)
            .build();
    }

    public static Reservation createReservation(LocalDate date, ReservationTime reservationTime, ReservationTheme reservationTheme) {
        return Reservation.builder()
            .date(date)
            .time(reservationTime)
            .theme(reservationTheme)
            .build();
    }

    public static ReservationTime createTime(LocalTime time) {
        return ReservationTime.builder()
            .startAt(time)
            .build();
    }

    public static ReservationTheme createTheme(String themeName, String description, String thumbnail) {
        return ReservationTheme.builder()
            .name(themeName)
            .description(description)
            .thumbnail(thumbnail)
            .build();
    }

    public static Member createUser(String name, String email, String password) {
        return Member.builder()
            .name(name)
            .email(email)
            .password(password)
            .role(Role.USER)
            .build();
    }
}
