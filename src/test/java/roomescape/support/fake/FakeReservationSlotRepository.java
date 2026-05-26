package roomescape.support.fake;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservation.ReservationSlotRepository;
import roomescape.domain.theme.Theme;

public class FakeReservationSlotRepository implements ReservationSlotRepository {

    private final Map<Long, ReservationSlot> storage = new LinkedHashMap<>();
    private long sequence = 1L;

    public Optional<ReservationSlot> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public ReservationSlot save(ReservationSlot reservation) {
        Long id = reservation.getId();
        if (id == null) {
            id = sequence++;
        } else {
            sequence = Math.max(sequence, id + 1);
        }
        ReservationSlot savedReservation = ReservationSlot.createWithId(id, reservation);
        storage.put(id, savedReservation);
        return savedReservation;
    }

    @Override
    public List<ReservationSlot> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public int deleteById(Long id) {
        ReservationSlot removedReservation = storage.remove(id);
        if (removedReservation == null) {
            return 0;
        }
        return 1;
    }

    @Override
    public int countByTimeId(Long timeId) {
        int count = 0;
        for (ReservationSlot value : storage.values()) {
            if (value.getTime().getId().equals(timeId)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public int countByReservationDateId(Long dateId) {
        int count = 0;
        for (ReservationSlot value : storage.values()) {
            if (value.getDate().getId().equals(dateId)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public List<Long> findReservedTimes(Long themeId, Long dateId) {
        List<Long> reservedTimeIds = new ArrayList<>();
        for (ReservationSlot reservation : storage.values()) {
            if (reservation.getTheme().getId().equals(themeId) && reservation.getDate().getId().equals(dateId)) {
                reservedTimeIds.add(reservation.getTime().getId());
            }
        }
        return reservedTimeIds;
    }

    @Override
    public List<Theme> findPopularThemes(int rankLimit, LocalDate startDay, LocalDate endDay) {
        return List.of();
    }

    @Override
    public int countByThemeId(Long themeId) {
        int count = 0;
        for (ReservationSlot reservation : storage.values()) {
            if (reservation.getTheme().getId().equals(themeId)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public boolean existsReservation(Long timeId, Long dateId, Long themeId) {
        return existsBySchedule(timeId, dateId, themeId);
    }

    @Override
    public boolean existsBySchedule(Long timeId, Long dateId, Long themeId) {
        for (ReservationSlot reservation : storage.values()) {
            if (timeId.equals(reservation.getTime().getId())
                && dateId.equals(reservation.getDate().getId())
                && themeId.equals(reservation.getTheme().getId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean existsOtherReservation(Long id, Long timeId, Long dateId, Long themeId) {
        for (ReservationSlot reservation : storage.values()) {
            if (!id.equals(reservation.getId())
                && timeId.equals(reservation.getTime().getId())
                && dateId.equals(reservation.getDate().getId())
                && themeId.equals(reservation.getTheme().getId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Optional<ReservationSlot> findBySchedule(Long timeId, Long dateId, Long themeId) {
        return storage.values().stream()
            .filter(reservation -> timeId.equals(reservation.getTime().getId()))
            .filter(reservation -> dateId.equals(reservation.getDate().getId()))
            .filter(reservation -> themeId.equals(reservation.getTheme().getId()))
            .findFirst();
    }

    @Override
    public List<ReservationSlot> findByName(String name) {
        return List.of();
    }

    @Override
    public Optional<ReservationSlot> update(Long id, ReservationSlot withoutId) {
        if (!storage.containsKey(id)) {
            return Optional.empty();
        }
        ReservationSlot updatedReservation = ReservationSlot.createWithId(id, withoutId);
        storage.put(id, updatedReservation);
        return Optional.of(updatedReservation);
    }

    private Comparator<ReservationSlot> latestReservationFirst() {
        return Comparator.comparing((ReservationSlot reservation) -> reservation.getDate().getDate())
            .reversed()
            .thenComparing(reservation -> reservation.getTime().getStartAt(), Comparator.reverseOrder())
            .thenComparing(ReservationSlot::getId, Comparator.reverseOrder());
    }
}
