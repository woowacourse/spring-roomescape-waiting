package roomescape;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.member.model.Member;
import roomescape.member.model.Role;
import roomescape.reservation.model.entity.Reservation;
import roomescape.reservation.model.entity.ReservationTheme;
import roomescape.reservation.model.entity.ReservationTime;
import roomescape.reservation.model.entity.ReservationWaiting;
import roomescape.reservation.model.entity.vo.ReservationStatus;
import roomescape.reservation.model.entity.vo.ReservationWaitingStatus;

public class ReservationTestFixture {

    private static int identifier = 0;

    public static ReservationTime getReservationTimeFixture() {
        identifier++;
        return ReservationTime.builder()
            .startAt(LocalTime.of(13, identifier % 60))
            .build();
    }

    public static ReservationTheme getReservationThemeFixture() {
        identifier++;
        return ReservationTheme.builder()
            .name("탈출" + identifier)
            .description("탈출하는 내용" + identifier)
            .thumbnail("a.com" + identifier)
            .build();
    }

    public static Member getUserFixture() {
        identifier++;
        return Member.builder()
            .name("웨이드" + identifier)
            .email("wade@naver.com" + identifier)
            .password("1234" + identifier)
            .role(Role.USER)
            .build();
    }

    public static Reservation createConfirmedReservation(LocalDate date, ReservationTime reservationTime, ReservationTheme reservationTheme) {
        return Reservation.builder()
                .date(date)
                .time(reservationTime)
                .theme(reservationTheme)
                .status(ReservationStatus.CONFIRMED)
                .build();
    }

    public static Reservation createConfirmedReservation(LocalDate date, ReservationTime reservationTime, ReservationTheme reservationTheme, Member member) {
        return Reservation.builder()
                .date(date)
                .time(reservationTime)
                .theme(reservationTheme)
                .status(ReservationStatus.CONFIRMED)
                .member(member)
                .build();
    }

    public static Reservation createCanceledReservation(LocalDate date, ReservationTime reservationTime, ReservationTheme reservationTheme, Member member) {
        return Reservation.builder()
                .date(date)
                .time(reservationTime)
                .theme(reservationTheme)
                .status(ReservationStatus.CANCELED)
                .member(member)
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

    public static ReservationWaiting createPendingWaiting(LocalDate date, ReservationTime reservationTime, ReservationTheme reservationTheme, Member member) {
        return ReservationWaiting.builder()
                .date(date)
                .time(reservationTime)
                .theme(reservationTheme)
                .member(member)
                .status(ReservationWaitingStatus.PENDING)
                .build();
    }

    public static ReservationWaiting createAcceptWaiting(LocalDate date, ReservationTime reservationTime, ReservationTheme reservationTheme, Member member) {
        return ReservationWaiting.builder()
                .date(date)
                .time(reservationTime)
                .theme(reservationTheme)
                .member(member)
                .status(ReservationWaitingStatus.ACCEPTED)
                .build();
    }
}
