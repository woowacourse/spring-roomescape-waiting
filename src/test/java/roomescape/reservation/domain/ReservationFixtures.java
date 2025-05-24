package roomescape.reservation.domain;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberFixtures;
import roomescape.reservation.time.domain.ReservationTime;
import roomescape.reservation.time.domain.ReservationTimeFixtures;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeFixtures;

public class ReservationFixtures {

    private static final LocalDate DEFAULT_DATE = LocalDate.now();

    public static Reservation persistReservedReservation(
            EntityManager entityManager,
            Member member,
            Theme theme,
            LocalDate date,
            ReservationTime reservationTime
    ) {
        Reservation reservation = Reservation.createReserved(member, theme, date, reservationTime);
        entityManager.persist(reservation);
        return reservation;
    }

    public static Reservation persistReservedReservation(
            EntityManager entityManager,
            Member member,
            Theme theme,
            ReservationTime reservationTime
    ) {
        return persistReservedReservation(entityManager, member, theme, DEFAULT_DATE, reservationTime);
    }

    public static Reservation persistReservedReservation(
            EntityManager entityManager,
            Theme theme,
            LocalDate date,
            ReservationTime reservationTime
    ) {
        Member member = MemberFixtures.persistUserMember(entityManager);
        return persistReservedReservation(entityManager, member, theme, date, reservationTime);
    }

    public static Reservation persistReservedReservation(
            EntityManager entityManager,
            Member member,
            Theme theme,
            LocalDate date
    ) {
        ReservationTime reservationTime = ReservationTimeFixtures.persistReservationTime(entityManager);
        return persistReservedReservation(entityManager, member, theme, date, reservationTime);
    }

    public static Reservation persistReservedReservation(
            EntityManager entityManager,
            Theme theme,
            LocalDate date
    ) {
        ReservationTime reservationTime = ReservationTimeFixtures.persistReservationTime(entityManager);
        Member member = MemberFixtures.persistUserMember(entityManager);
        return persistReservedReservation(entityManager, member, theme, date, reservationTime);
    }

    public static Reservation persistReservedReservation(
            EntityManager entityManager,
            LocalDate date,
            ReservationTime reservationTime
    ) {
        Member member = MemberFixtures.persistUserMember(entityManager);
        Theme theme = ThemeFixtures.persistTheme(entityManager);
        return persistReservedReservation(entityManager, member, theme, date, reservationTime);
    }

    public static Reservation persistReservedReservation(
            EntityManager entityManager,
            Member member
    ) {
        Theme theme = ThemeFixtures.persistTheme(entityManager);
        ReservationTime reservationTime = ReservationTimeFixtures.persistReservationTime(entityManager);
        return persistReservedReservation(entityManager, member, theme, DEFAULT_DATE, reservationTime);
    }

    public static Reservation persistReservedReservation(
            EntityManager entityManager,
            Theme theme
    ) {
        ReservationTime reservationTime = ReservationTimeFixtures.persistReservationTime(entityManager);
        Member member = MemberFixtures.persistUserMember(entityManager);
        return persistReservedReservation(entityManager, member, theme, DEFAULT_DATE, reservationTime);
    }

    public static Reservation persistReservedReservation(
            EntityManager entityManager,
            ReservationTime reservationTime
    ) {
        Member member = MemberFixtures.persistUserMember(entityManager);
        Theme theme = ThemeFixtures.persistTheme(entityManager);

        return persistReservedReservation(entityManager, member, theme, DEFAULT_DATE, reservationTime);
    }

    public static Reservation persistDefaultReservedReservation(EntityManager entityManager) {
        Member member = MemberFixtures.persistUserMember(entityManager);
        Theme theme = ThemeFixtures.persistTheme(entityManager);
        ReservationTime reservationTime = ReservationTimeFixtures.persistReservationTime(entityManager);

        return persistReservedReservation(entityManager, member, theme, DEFAULT_DATE, reservationTime);
    }

    public static Reservation persistWaitingReservation(
            EntityManager entityManager,
            Member member,
            Theme theme,
            LocalDate date,
            ReservationTime reservationTime
    ) {
        Reservation reservation = Reservation.createWaiting(member, theme, date, reservationTime);
        entityManager.persist(reservation);
        return reservation;
    }

    public static Reservation persistWaitingReservation(
            EntityManager entityManager,
            Member member,
            Theme theme,
            ReservationTime reservationTime
    ) {
        return persistWaitingReservation(entityManager, member, theme, DEFAULT_DATE, reservationTime);
    }

    public static Reservation persistWaitingReservation(
            EntityManager entityManager,
            Theme theme,
            LocalDate date,
            ReservationTime reservationTime
    ) {
        Member member = MemberFixtures.persistUserMember(entityManager);
        return persistWaitingReservation(entityManager, member, theme, date, reservationTime);
    }

    public static Reservation persistWaitingReservation(
            EntityManager entityManager,
            Member member
    ) {
        Theme theme = ThemeFixtures.persistTheme(entityManager);
        ReservationTime reservationTime = ReservationTimeFixtures.persistReservationTime(entityManager);
        return persistWaitingReservation(entityManager, member, theme, DEFAULT_DATE, reservationTime);
    }

    public static Reservation persistDefaultWaitingReservation(EntityManager entityManager) {
        Member member = MemberFixtures.persistUserMember(entityManager);
        Theme theme = ThemeFixtures.persistTheme(entityManager);
        ReservationTime reservationTime = ReservationTimeFixtures.persistReservationTime(entityManager);

        return persistWaitingReservation(entityManager, member, theme, DEFAULT_DATE, reservationTime);
    }
}
