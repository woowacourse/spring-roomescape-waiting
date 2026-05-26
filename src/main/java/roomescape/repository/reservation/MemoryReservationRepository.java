package roomescape.repository.reservation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.domain.reservation.Reservation;

public class MemoryReservationRepository implements ReservationRepository {
    List<Reservation> reservations = new ArrayList<>();
    private final AtomicLong index = new AtomicLong(0);

    @Override
    public List<Reservation> findAll() {
        return List.copyOf(reservations);
    }

    @Override
    public List<Reservation> findAllByName(final String name) {
        return reservations.stream()
                .filter(it -> it.hasName(name))
                .toList();
    }

    @Override
    public Optional<Reservation> findById(final long id) {
        return reservations.stream()
                .filter(it -> it.getId() == id)
                .findFirst();
    }

    @Override
    public Optional<Reservation> findByIdAndName(final long id, final String name) {
        return reservations.stream()
                .filter(it -> it.getId() == id)
                .filter(it -> it.hasName(name))
                .findFirst();
    }

    @Override
    public void deleteById(long id) {
        Reservation reservation = reservations.stream()
                .filter(it -> it.getId() == id)
                .findFirst()
                .orElseThrow(RuntimeException::new);

        reservations.remove(reservation);
    }

    @Override
    public Reservation save(Reservation reservation) {
        Reservation newReservation = reservation.withId(index.incrementAndGet());
        reservations.add(newReservation);
        return newReservation;
    }

    @Override
    public Reservation update(final Reservation reservation) {
        deleteById(reservation.getId());
        reservations.add(reservation);
        return reservation;
    }

    @Override
    public boolean existsByDateAndThemeIdAndTimeId(LocalDate date, long themeId, long timeId) {
        for (Reservation reservation : reservations) {
            if (reservation.getDate().equals(date)
                    && reservation.getTheme().getId() == themeId
                    && reservation.getTime().getId() == timeId) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean existsByDateAndThemeIdAndTimeIdExcludingId(
            final LocalDate date,
            final long themeId,
            final long timeId,
            final long reservationId
    ) {
        for (Reservation reservation : reservations) {
            if (reservation.getId() == reservationId) {
                continue;
            }

            if (reservation.getDate().equals(date)
                    && reservation.getTheme().getId() == themeId
                    && reservation.getTime().getId() == timeId) {
                return true;
            }
        }

        return false;
    }

    @Override
    public List<Long> findReservedTimeIdsByDateAndThemeId(final LocalDate date, final long themeId) {
        return reservations.stream()
                .filter(reservation -> reservation.getDate().equals(date))
                .filter(reservation -> reservation.getTheme().getId() == themeId)
                .map(reservation -> reservation.getTime().getId())
                .toList();
    }

    @Override
    public boolean existsByTimeId(final long timeId) {
        return reservations.stream()
                .anyMatch(reservation -> reservation.getTime().getId() == timeId);
    }

    @Override
    public boolean existsByThemeId(final long themeId) {
        return reservations.stream()
                .anyMatch(reservation -> reservation.getTheme().getId() == themeId);
    }
}
