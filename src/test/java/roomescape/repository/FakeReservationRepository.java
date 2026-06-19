package roomescape.repository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import roomescape.domain.Reservation;
import roomescape.domain.reservationStatus.PendingStatus;
import roomescape.global.exception.CustomException;
import roomescape.global.exception.ErrorCode;

public class FakeReservationRepository implements ReservationRepository {

    private final Map<Long, Reservation> storage = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1L);

    public void clear() {
        storage.clear();
        sequence.set(1L);
    }

    @Override
    public List<Reservation> findAll() {
        return List.copyOf(storage.values());
    }

    @Override
    public Optional<Reservation> findById(long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Optional<Reservation> findByOrderId(String orderId) {
        return storage.values().stream()
                .filter(r -> Objects.equals(r.getOrderId(), orderId))
                .findFirst();
    }

    @Override
    public Reservation save(Reservation reservation) {
        long id = sequence.getAndIncrement();
        Reservation savedReservation = new Reservation(
                id,
                reservation.getName(),
                reservation.getThemeSlotId(),
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme(),
                reservation.getReservationStatus(),
                reservation.getOrderId(),
                reservation.getAmount()
        );
        storage.put(id, savedReservation);
        return savedReservation;
    }

    @Override
    public void deleteById(long id) {
        storage.remove(id);
    }

    @Override
    public boolean existsByThemeSlotId(long themeSlotId) {
        return storage.values().stream()
                .anyMatch(reservation ->
                        Objects.equals(reservation.getThemeSlotId(), themeSlotId)
                                && !"CANCELLED".equals(reservation.getReservationStatusName())
                );
    }

    @Override
    public List<Reservation> findByName(String name) {
        return storage.values().stream()
                .filter(reservation -> Objects.equals(reservation.getName(), name))
                .toList();
    }

    @Override
    public List<Reservation> findByThemeSlotAndPending(Long themeSlotId) {
        return storage.values().stream()
                .filter(reservation -> Objects.equals(reservation.getThemeSlotId(), themeSlotId))
                .filter(reservation -> "PENDING".equals(reservation.getReservationStatusName()))
                .sorted(Comparator.comparing(Reservation::getId))
                .toList();
    }

    @Override
    public void updateStatus(Reservation reservation) {
        Long id = reservation.getId();
        if (!storage.containsKey(id)) {
            throw new CustomException(ErrorCode.RESERVATION_NOT_FOUND);
        }

        Reservation getReservation = storage.get(id);
        getReservation.changeStatus(reservation.getReservationStatus());
    }

    @Override
    public void updateThemeSlot(Reservation reservation) {
        Long id = reservation.getId();
        if (!storage.containsKey(id)) {
            throw new CustomException(ErrorCode.RESERVATION_NOT_FOUND);
        }

        Reservation getReservation = storage.get(id);
        Reservation newReservation = new Reservation(
                getReservation.getId(),
                getReservation.getName(),
                reservation.getThemeSlotId(),
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme(),
                getReservation.getReservationStatus(),
                getReservation.getOrderId(),
                getReservation.getAmount()
        );
        storage.remove(id);
        storage.put(id, newReservation);
    }

    @Override
    public boolean existsByThemeId(long themeId) {
        return storage.values().stream()
                .anyMatch(reservation ->
                        Objects.equals(reservation.getTheme().getId(), themeId)
                );
    }

    @Override
    public boolean existsByTimeId(long timeId) {
        return storage.values().stream()
                .anyMatch(reservation ->
                        Objects.equals(reservation.getTime().getId(), timeId)
                );
    }

    @Override
    public Optional<Reservation> findRecentReservationByThemeSlot(Long themeSlotId) {
        return storage.values().stream()
                .filter(reservation -> reservation.getThemeSlotId().equals(themeSlotId) && reservation.getReservationStatus().equals(
                        PendingStatus.getInstance()))
                .sorted()
                .findFirst();
    }
}
