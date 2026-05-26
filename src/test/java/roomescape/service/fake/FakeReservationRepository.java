package roomescape.service.fake;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.domain.Reservation;
import roomescape.repository.ReservationRepository;

public class FakeReservationRepository implements ReservationRepository {

    private final Map<Long, Reservation> storage = new HashMap();
    private final AtomicLong counter = new AtomicLong(1);

    @Override
    public Reservation save(Reservation reservation) {
        Reservation saved = new Reservation(
                counter.getAndIncrement(),
                reservation.getName(),
                reservation.getDate(),
                reservation.getTheme(),
                reservation.getTime()
        );
        storage.put(saved.getId(), saved);
        return saved;
    }

    @Override
    public void delete(long id) {
        storage.values().removeIf(reservation -> reservation.getId().equals(id));
    }

    @Override
    public boolean existByDateAndThemeIdAndTimeId(LocalDate date, long themeId, long timeId) {
        return storage.values().stream()
                .anyMatch(reservation -> reservation.getDate().equals(date) &&
                        reservation.getTheme().getId().equals(themeId) &&
                        reservation.getTime().getId().equals(timeId));
    }

    @Override
    public List<Reservation> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public Optional<Reservation> findById(long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public void update(Reservation reservation) {
        storage.put(reservation.getId(), reservation);
    }
}
