package roomescape.repository;

import roomescape.domain.Reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class FakeReservationRepository implements ReservationRepository {

    private final List<Reservation> reservations;
    private final AtomicLong reservationId = new AtomicLong(1);

    public FakeReservationRepository(final List<Reservation> reservations) {
        this.reservations = reservations;
    }

    @Override
    public Reservation save(final Reservation reservation) {
        Reservation newReservation = new Reservation(reservationId.getAndIncrement(), reservation.getMember(), reservation.getDate(), reservation.getTime(), reservation.getTheme());
        reservations.add(newReservation);
        return findById(newReservation.getId()).get();
    }

    @Override
    public List<Reservation> findAll() {
        return reservations;
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        return reservations.stream()
                .filter(reservation -> Objects.equals(reservation.getId(), id))
                .findFirst();
    }

    @Override
    public List<Reservation> findByDateTimeAndThemeId(LocalDate date, LocalTime time, long themeId) {
        return reservations.stream()
                .filter(reservation -> reservation.getDate().equals(date) && reservation.getTime().startAt().equals(time) && Objects.equals(reservation.getTheme().id(), themeId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Reservation> findByDateAndTheme(LocalDate date, long themeId) {
        return reservations.stream()
                .filter(reservation -> reservation.getDate().equals(date) && reservation.getTheme().id().equals(themeId))
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByThemeId(long themeId) {
        return reservations.stream().anyMatch(reservation -> Objects.equals(reservation.getTheme().id(), themeId));
    }

    @Override
    public boolean existsByTimeId(long timeId) {
        return reservations.stream().anyMatch(reservation -> Objects.equals(reservation.getTime().id(), timeId));
    }

    @Override
    public List<Reservation> findReservationsByPeriodAndMemberAndTheme(long themeId, long memberId, LocalDate from, LocalDate to) {
        return List.of();
    }

    @Override
    public int deleteById(long id) {
        Reservation deleteReservation = reservations.stream()
                .filter(reservation -> Objects.equals(reservation.getId(), id))
                .findFirst().orElse(null);

        if (deleteReservation != null) {
            int affectedRows = (int) reservations.stream()
                    .filter(reservation -> Objects.equals(reservation.getId(), deleteReservation.getId()))
                    .count();

            reservations.remove(deleteReservation);
            return affectedRows;
        }
        return 0;
    }
}
