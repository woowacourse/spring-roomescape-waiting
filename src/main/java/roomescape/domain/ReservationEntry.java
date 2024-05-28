package roomescape.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import roomescape.entity.Member;
import roomescape.entity.Reservation;
import roomescape.entity.ReservationTime;
import roomescape.entity.Theme;

public class ReservationEntry {

    private final Long id;
    private final Member member;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;
    private final ReservationStatus status;
    private final Long rank;

    public ReservationEntry(final Long id, final Member member, final LocalDate date, final ReservationTime time,
                            final Theme theme, final ReservationStatus status, Long rank) {
        this.id = id;
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.status = status;
        this.rank = rank;
    }

    public static List<ReservationEntry> createEntries(final List<Reservation> reservations,
                                                       final List<WaitingWithRank> waitingWithRanks) {
        List<ReservationEntry> reservationEntries = new ArrayList<>();

        reservationEntries.addAll(fromReservations(reservations));
        reservationEntries.addAll(fromWaitings(waitingWithRanks));

        return reservationEntries;
    }

    private static List<ReservationEntry> fromReservations(final List<Reservation> reservations) {
        List<ReservationEntry> reservationEntries = new ArrayList<>();

        for (Reservation reservation : reservations) {
            ReservationEntry reservationEntry = new ReservationEntry(
                    reservation.getId(),
                    reservation.getMember(),
                    reservation.getDate(),
                    reservation.getTime(),
                    reservation.getTheme(),
                    ReservationStatus.RESERVATION,
                    0L
            );
            reservationEntries.add(reservationEntry);
        }

        return reservationEntries;
    }

    private static List<ReservationEntry> fromWaitings(final List<WaitingWithRank> waitingWithRanks) {
        List<ReservationEntry> reservationEntries = new ArrayList<>();

        for (WaitingWithRank waitingWithRank : waitingWithRanks) {
            ReservationEntry reservationEntry = new ReservationEntry(
                    waitingWithRank.getWaiting().getId(),
                    waitingWithRank.getWaiting().getMember(),
                    waitingWithRank.getWaiting().getReservation().getDate(),
                    waitingWithRank.getWaiting().getReservation().getTime(),
                    waitingWithRank.getWaiting().getReservation().getTheme(),
                    ReservationStatus.WAITING,
                    waitingWithRank.getRank()
            );
            reservationEntries.add(reservationEntry);
        }

        return reservationEntries;
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public Long getRank() {
        return rank;
    }
}
