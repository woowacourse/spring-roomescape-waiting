package roomescape.reservation.unit.repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.reservation.entity.Reservation;
import roomescape.reservation.repository.ReservationRepository;

public class FakeReservationRepository implements ReservationRepository {

    private final Map<Long, Reservation> reservations = new ConcurrentHashMap<>();
    private final AtomicLong index = new AtomicLong(1);

    @Override
    public Reservation save(Reservation reservation) {
        Long id = this.index.getAndIncrement();
        Reservation savedReservation = new Reservation(
                id,
                reservation.getDate(),
                reservation.getTime(),
                reservation.getThemeId(),
                reservation.getMemberId()
        );
        reservations.put(id, savedReservation);
        return savedReservation;
    }

    @Override
    public List<Reservation> findAll() {
        return new ArrayList<>(reservations.values());
    }

    @Override
    public List<Reservation> findAllByTimeId(Long id) {
        return reservations.values().stream()
                .filter(reservation -> reservation.getTime().getId().equals(id))
                .toList();
    }

    @Override
    public List<Reservation> findAllFiltered(Long themeId, Long memberId, LocalDate dateFrom, LocalDate dateTo) {
        return reservations.values().stream()
                .filter(reservation -> reservation.getThemeId().equals(themeId))
                .filter(reservation -> reservation.getMemberId().equals(memberId))
                .filter(reservation -> !reservation.getDate().isBefore(dateFrom))
                .filter(reservation -> !reservation.getDate().isAfter(dateTo))
                .toList();
    }

    @Override
    public boolean deleteById(Long id) {
        return reservations.remove(id) != null;
    }

    @Override
    public boolean existsByDateAndTimeId(LocalDate date, Long timeId) {
        return reservations.values().stream()
                .anyMatch(reservation -> reservation.getDate().equals(date) &&
                        reservation.getTime().getId().equals(timeId));
    }
}
