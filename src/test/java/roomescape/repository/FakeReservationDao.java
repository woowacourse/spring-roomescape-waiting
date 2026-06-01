package roomescape.repository;

import java.util.*;

import roomescape.domain.Reservation;
import roomescape.domain.WaitingReservation;
import roomescape.domain.reservationStatus.PendingStatus;
import roomescape.global.exception.CustomException;
import roomescape.global.exception.ErrorCode;
import roomescape.repository.ReservationRepository;

public class FakeReservationDao implements ReservationRepository {

    private final Map<Long, Reservation> storage = new HashMap<>();
    private long sequence = 1L;

    @Override
    public List<Reservation> findAll() {
        return List.copyOf(storage.values());
    }

    @Override
    public Optional<Reservation> findById(long id) {
        return Optional.ofNullable(storage.get(id));
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
    public void deleteById(long id) {
        storage.remove(id);
    }

    @Override
    public boolean existsByThemeSlotId(long themeSlotId) {
        return storage.values().stream()
                .anyMatch(reservation ->
                        Objects.equals(reservation.getThemeSlot().getId(), themeSlotId)
                                && !"CANCELLED".equals(reservation.getReservationStatusName())
                );
    }

    @Override
    public boolean isExistBy(Long reservationId) {
        return storage.containsKey(reservationId);
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
                .filter(reservation -> Objects.equals(reservation.getThemeSlot().getId(), themeSlotId))
                .filter(reservation -> "PENDING".equals(reservation.getReservationStatusName()))
                .sorted(Comparator.comparing(Reservation::getId))
                .toList();
    }

    @Override
    public List<WaitingReservation> findWaitingReservationsWithOrder(Long themeSlotId) {
        List<Reservation> reservations = findByThemeSlotAndPending(themeSlotId);
        return createWaitingReservations(reservations);
    }

    @Override
    public List<WaitingReservation> findWaitingReservationsWithOrderByName(String name) {
        return storage.values().stream()
                .filter(reservation -> "PENDING".equals(reservation.getReservationStatusName()))
                .collect(java.util.stream.Collectors.groupingBy(Reservation::getThemeSlotId))
                .values()
                .stream()
                .flatMap(reservations -> createWaitingReservations(reservations.stream()
                        .sorted(Comparator.comparing(Reservation::getId))
                        .toList()).stream())
                .filter(waitingReservation -> waitingReservation.name().equals(name))
                .sorted(Comparator.comparing(WaitingReservation::id))
                .toList();
    }

    private List<WaitingReservation> createWaitingReservations(List<Reservation> reservations) {
        List<WaitingReservation> waitingReservations = new ArrayList<>();
        for (int index = 0; index < reservations.size(); index++) {
            Reservation reservation = reservations.get(index);
            waitingReservations.add(new WaitingReservation(
                    reservation.getId(),
                    reservation.getName(),
                    reservation.getDate(),
                    reservation.getTime(),
                    reservation.getTheme(),
                    reservation.getReservationStatusName(),
                    index + 1
            ));
        }
        return waitingReservations;
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

    @Override
    public Optional<Reservation> findRecentReservationByThemeSlot(Long themeSlotId) {
        return storage.values().stream()
                .filter(reservation -> reservation.getThemeSlot().getId().equals(themeSlotId) && reservation.getReservationStatus().equals(
                        PendingStatus.getInstance()))
                .sorted(Comparator.comparing(Reservation::getId))
                .findFirst();
    }
}
