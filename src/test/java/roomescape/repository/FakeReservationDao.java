package roomescape.repository;

import java.util.*;

import roomescape.domain.Reservation;
import roomescape.global.exception.CustomException;
import roomescape.global.exception.ErrorCode;

public class FakeReservationDao implements ReservationRepository {

    private final Map<Long, Reservation> storage = new HashMap<>();
    private final List<Long> findByIdForUpdateHistory = new ArrayList<>();
    private long sequence = 1L;

    @Override
    public List<Reservation> findAll() {
        return storage.values()
                .stream()
                .map(this::copy)
                .toList();
    }

    @Override
    public Optional<Reservation> findById(long id) {
        return Optional.ofNullable(storage.get(id))
                .map(this::copy);
    }

    @Override
    public Optional<Reservation> findByIdForUpdate(long id) {
        findByIdForUpdateHistory.add(id);
        return findById(id);
    }

    @Override
    public Reservation save(Reservation reservation) {
        long id = sequence++;
        Reservation savedReservation = new Reservation(
                id,
                reservation.getName(),
                reservation.getThemeSlot(),
                reservation.getReservationStatus()
        );
        storage.put(id, savedReservation);
        return savedReservation;
    }

    @Override
    public void flush() {
    }

    @Override
    public void deleteById(long id) {
        storage.remove(id);
    }

    @Override
    public boolean existsConfirmedByThemeSlotId(long themeSlotId) {
        return storage.values().stream()
                .anyMatch(reservation ->
                        Objects.equals(reservation.getThemeSlot().getId(), themeSlotId)
                                && "CONFIRMED".equals(reservation.getReservationStatusName())
                );
    }

    @Override
    public List<Reservation> findByName(String name) {
        return storage.values().stream()
                .filter(reservation -> Objects.equals(reservation.getName(), name))
                .map(this::copy)
                .toList();
    }

    @Override
    public boolean updateStatus(Reservation reservation, String expectedStatus) {
        Long id = reservation.getId();
        if (!storage.containsKey(id)) {
            throw new CustomException(ErrorCode.RESERVATION_NOT_FOUND);
        }

        Reservation getReservation = storage.get(id);
        if (!getReservation.getReservationStatusName().equals(expectedStatus)) {
            return false;
        }
        getReservation.changeStatus(reservation.getReservationStatus());
        return true;
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
                reservation.getThemeSlot(),
                getReservation.getReservationStatus()
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
    public boolean existsByThemeSlotIdAndMemberName(String name, Long themeSlotId) {
        return storage.values().stream()
                .anyMatch(reservation ->
                        reservation.getName().equals(name)
                                && Objects.equals(reservation.getThemeSlot().getId(), themeSlotId)
                                && !"CANCELLED".equals(reservation.getReservationStatusName())
                );
    }

    private Reservation copy(Reservation reservation) {
        return new Reservation(
                reservation.getId(),
                reservation.getName(),
                reservation.getThemeSlot(),
                reservation.getReservationStatus()
        );
    }

    public List<Long> findByIdForUpdateHistory() {
        return List.copyOf(findByIdForUpdateHistory);
    }

    public void clearFindByIdForUpdateHistory() {
        findByIdForUpdateHistory.clear();
    }
}
