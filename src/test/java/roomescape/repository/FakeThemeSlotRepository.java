package roomescape.repository;

import roomescape.domain.Reservation;
import roomescape.domain.ThemeSlot;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class FakeThemeSlotRepository implements ThemeSlotRepository {

    private final Map<Long, ThemeSlot> storage = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1L);
    private final ReservationRepository reservationRepository;

    public FakeThemeSlotRepository() {
        this.reservationRepository = null;
    }

    public FakeThemeSlotRepository(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public void clear() {
        storage.clear();
        sequence.set(1L);
    }

    @Override
    public ThemeSlot save(ThemeSlot themeSlot) {
        long id = sequence.getAndIncrement();
        ThemeSlot saved = ThemeSlot.of(id, themeSlot);
        storage.put(id, saved);
        return saved;
    }

    @Override
    public List<ThemeSlot> saveAll(List<ThemeSlot> themeSlots) {
        List<ThemeSlot> results = new ArrayList<>();
        for (ThemeSlot themeSlot : themeSlots) {
            results.add(save(themeSlot));
        }
        return results;
    }

    @Override
    public List<ThemeSlot> findByThemeIdAndDate(long themeId, LocalDate date) {
        return storage.values().stream()
                .filter(ts -> ts.getTheme().getId() == themeId && ts.getDate().equals(date))
                .toList();
    }

    @Override
    public Optional<ThemeSlot> findById(long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Optional<ThemeSlot> findWithReservations(Long themeSlotId) {
        return findById(themeSlotId).map(ts -> {
            List<Reservation> reservations = (reservationRepository != null)
                    ? reservationRepository.findAll().stream()
                    .filter(r -> r.getThemeSlotId().equals(themeSlotId))
                    .toList()
                    : List.of();
            return new ThemeSlot(ts.getId(), ts.getTheme(), ts.getDate(), ts.getTime(), ts.isReserved(), reservations);
        });
    }

    @Override
    public boolean isExistBy(long themeId, LocalDate date) {
        return storage.values().stream()
                .anyMatch(ts -> ts.getTheme().getId() == themeId && ts.getDate().equals(date));
    }

    @Override
    public void update(ThemeSlot themeSlot) {
        storage.values().stream()
                .filter(ts -> ts.getTheme().getId().equals(themeSlot.getTheme().getId())
                        && ts.getDate().equals(themeSlot.getDate())
                        && ts.getTime().getId().equals(themeSlot.getTime().getId()))
                .findFirst()
                .ifPresent(ts -> storage.put(ts.getId(), ThemeSlot.of(ts.getId(), themeSlot)));
    }

    @Override
    public void deleteById(long id) {
        storage.remove(id);
    }
}
