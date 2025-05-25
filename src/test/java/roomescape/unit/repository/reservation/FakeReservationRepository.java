package roomescape.unit.repository.reservation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.exception.reservation.InvalidReservationException;
import roomescape.repository.reservation.ReservationRepository;

public class FakeReservationRepository implements ReservationRepository {

    private final AtomicLong index = new AtomicLong(1L);
    private final List<Reservation> reservations = new ArrayList<>();

    @Override
    public Reservation save(Reservation reservation) {
        Reservation newReservation = new Reservation(
                index.getAndIncrement(),
                reservation.getDate(),
                reservation.getReservationTime(),
                reservation.getTheme(),
                reservation.getReservationStatus()
        );
        reservations.add(newReservation);
        return newReservation;
    }

    @Override
    public List<Reservation> findAll() {
        return Collections.unmodifiableList(reservations);
    }

    @Override
    public void deleteById(Long id) {
        Reservation reservation = reservations.stream()
                .filter(r -> r.getId().equals(id))
                .findAny()
                .orElseThrow(() -> new InvalidReservationException("존재하지 않는 id입니다: " + id));
        reservations.remove(reservation);
    }

    @Override
    public boolean existsByTimeId(Long id) {
        return reservations.stream()
                .anyMatch(r -> r.getReservationTime().getId().equals(id));
    }

    @Override
    public boolean existsByDateAndTimeAndTheme(LocalDate date, ReservationTime time, Theme theme) {
        return reservations.stream()
                .anyMatch(r ->
                        r.getDate().isEqual(date) &&
                                r.getReservationTime().getId().equals(time.getId()) &&
                                r.getTheme().getId().equals(theme.getId())
                );
    }

    @Override
    public boolean existsByThemeId(Long themeId) {
        return reservations.stream()
                .anyMatch(r -> r.getTheme().getId().equals(themeId));
    }

    @Override
    public List<Reservation> findAllByDateAndThemeId(LocalDate date, Long themeId) {
        return reservations.stream()
                .filter(r -> r.getDate().isEqual(date) &&
                        r.getTheme().getId().equals(themeId))
                .toList();
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        return reservations.stream()
                .filter(r -> r.getId().equals(id))
                .findAny();
    }

    @Override
    public int countByThemeIdAndDateAndTimeId(Long themeId, LocalDate date, Long timeId) {
        return (int) reservations.stream()
                .filter(r -> r.getTheme().isSameTheme(themeId))
                .filter(r -> r.getDate().isEqual(date))
                .filter(r -> r.getReservationTime().getId().equals(timeId))
                .count();
    }

    @Override
    public List<Reservation> findAllByDateBetween(LocalDate start, LocalDate end) {
        return reservations.stream()
                .filter(r -> !r.getDate().isBefore(start) && !r.getDate().isAfter(end))
                .toList();
    }
}
