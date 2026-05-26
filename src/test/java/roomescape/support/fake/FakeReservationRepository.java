package roomescape.support.fake;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.theme.Theme;

public class FakeReservationRepository implements ReservationRepository {

    private final Map<Long, Reservation> storage = new LinkedHashMap<>();
    private long sequence = 1L;

    public Optional<Reservation> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Reservation save(Reservation reservation) {
        Long id = reservation.getId();
        if (id == null) {
            id = sequence++;
        } else {
            sequence = Math.max(sequence, id + 1);
        }
        Reservation savedReservation = Reservation.createWithId(id, reservation);
        storage.put(id, savedReservation);
        return savedReservation;
    }

    @Override
    public List<Reservation> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public int deleteById(Long id) {
        Reservation removedReservation = storage.remove(id);
        if (removedReservation == null) {
            return 0;
        }
        return 1;
    }

    @Override
    public int countByTimeId(Long timeId) {
        int count = 0;
        for (Reservation value : storage.values()) {
            if (value.getTime().getId().equals(timeId)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public int countByReservationDateId(Long dateId) {
        int count = 0;
        for (Reservation value : storage.values()) {
            if (value.getDate().getId().equals(dateId)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public List<Long> findReservedTimes(Long themeId, Long dateId) {
        List<Long> reservedTimeIds = new ArrayList<>();
        for (Reservation reservation : storage.values()) {
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
        for (Reservation reservation : storage.values()) {
            if (reservation.getTheme().getId().equals(themeId)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public boolean existsReservation(Long timeId, Long dateId, Long themeId) {
        for (Reservation reservation : storage.values()) {
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
        for (Reservation reservation : storage.values()) {
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
    public List<Reservation> findByName(String name) {
        List<Reservation> reservations = new ArrayList<>(storage.values().stream()
            .filter(reservation -> name.equals(reservation.getName()))
            .toList());
        reservations.sort(latestReservationFirst());
        return reservations;
    }

    @Override
    public Optional<Reservation> update(Long id, Reservation withoutId) {
        if (!storage.containsKey(id)) {
            return Optional.empty();
        }
        Reservation updatedReservation = Reservation.createWithId(id, withoutId);
        storage.put(id, updatedReservation);
        return Optional.of(updatedReservation);
    }

    private Comparator<Reservation> latestReservationFirst() {
        return Comparator.comparing((Reservation reservation) -> reservation.getDate().getDate())
            .reversed()
            .thenComparing(reservation -> reservation.getTime().getStartAt(), Comparator.reverseOrder())
            .thenComparing(Reservation::getId, Comparator.reverseOrder());
    }
}
