package roomescape.repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

public class CollectionReservationRepository implements ReservationRepository {
    private final List<Reservation> reservations;
    private final AtomicLong atomicLong;


    public CollectionReservationRepository() {
        this.reservations = new ArrayList<>();
        this.atomicLong = new AtomicLong(0);
    }

    @Override
    public Reservation save(Reservation reservation) {
        Reservation saved = new Reservation(atomicLong.incrementAndGet(), reservation);
        reservations.add(saved);
        return saved;
    }

    @Override
    public List<Reservation> findAll() {
        return reservations.stream()
                .sorted()
                .toList();
    }

    @Override
    public List<Reservation> findByMemberAndThemeBetweenDates(long memberId, long themeId, LocalDate start,
                                                              LocalDate end) {
        return reservations.stream()
                .filter(reservation -> reservation.getReservationMember().getId() == memberId)
                .filter(reservation -> reservation.getTheme().getId() == themeId)
                .filter(reservation -> {
                    LocalDate date = reservation.getDate();
                    return date.isEqual(start) || date.isAfter(start);
                })
                .filter(reservation -> {
                    LocalDate date = reservation.getDate();
                    return date.isEqual(end) || date.isBefore(end);
                })
                .toList();
    }

    @Override
    public boolean existsByThemeAndDateAndTime(Theme theme, LocalDate date, ReservationTime reservationTime) {
        return reservations.stream()
                .filter(reservation -> theme.equals(reservation.getTheme()))
                .filter(reservation -> date.equals(reservation.getDate()))
                .anyMatch(reservation -> reservationTime.equals(reservation.getReservationTime()));
    }

    @Override
    public boolean existsByTime(ReservationTime reservationTime) {
        return reservations.stream()
                .anyMatch(reservation -> reservationTime.equals(reservation.getReservationTime()));
    }

    @Override
    public boolean existsByTheme(Theme theme) {
        return reservations.stream()
                .anyMatch(reservation -> theme.equals(reservation.getTheme()));
    }

    @Override
    public void delete(long id) {
        reservations.stream()
                .filter(reservation -> reservation.hasSameId(id))
                .findAny()
                .ifPresent(reservations::remove);
    }
}
