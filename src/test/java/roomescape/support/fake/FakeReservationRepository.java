package roomescape.support.fake;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.dto.ReservationCountResult;
import roomescape.domain.reservation.dto.ReservationWithWaitingNumber;

public class FakeReservationRepository implements ReservationRepository {

    private final Map<Long, Reservation> storage = new LinkedHashMap<>();
    private long sequence = 1L;

    @Override
    public Reservation save(Reservation userReservation) {
        Long id = userReservation.getId();
        if (id == null) {
            id = sequence++;
        } else {
            sequence = Math.max(sequence, id + 1);
        }
        Reservation savedUserReservation = Reservation.createWithId(id, userReservation);
        storage.put(id, savedUserReservation);
        return savedUserReservation;
    }

    @Override
    public List<ReservationWithWaitingNumber> findAll() {
        return storage.values().stream()
            .map(this::withWaitingNumber)
            .toList();
    }

    @Override
    public Optional<Reservation> findActiveReservation(Long id) {
        return Optional.ofNullable(storage.get(id))
            .filter(this::isActive);
    }

    @Override
    public List<ReservationWithWaitingNumber> findReservations(String username) {
        return storage.values().stream()
            .filter(userReservation -> username.equals(userReservation.getUser().getName()))
            .sorted(Comparator.comparing(Reservation::getId).reversed())
            .map(this::withWaitingNumber)
            .toList();
    }

    @Override
    public Long countByReservationSlotId(Long reservationSlotId) {
        return storage.values().stream()
            .filter(this::isActive)
            .filter(userReservation -> reservationSlotId.equals(userReservation.getReservationSlot().getId()))
            .count();
    }

    @Override
    public List<Reservation> findAllByReservationIdOrder(Long reservationId) {
        return storage.values().stream()
            .filter(this::isActive)
            .filter(userReservation -> reservationId.equals(userReservation.getReservationSlot().getId()))
            .sorted(Comparator.comparing(Reservation::getUpdatedAt)
                .thenComparing(Reservation::getId))
            .toList();
    }

    @Override
    public void update(Long id, Reservation userReservation) {
        if (!storage.containsKey(id)) {
            return;
        }
        Reservation updatedUserReservation = Reservation.createWithId(id, userReservation);
        storage.put(id, updatedUserReservation);
    }

    @Override
    public boolean existsActiveByUserIdAndReservationId(Long userId, Long reservationId) {
        return storage.values().stream()
            .filter(this::isActive)
            .filter(userReservation -> userId.equals(userReservation.getUser().getId()))
            .filter(userReservation -> reservationId.equals(userReservation.getReservationSlot().getId()))
            .findAny()
            .isPresent();
    }

    @Override
    public List<ReservationCountResult> countReservation(Long themeId, Long dateId) {
        return storage.values().stream()
            .filter(this::isActive)
            .filter(reservation -> themeId.equals(reservation.getReservationSlot().getTheme().getId()))
            .filter(reservation -> dateId.equals(reservation.getReservationSlot().getDate().getId()))
            .collect(java.util.stream.Collectors.groupingBy(
                reservation -> reservation.getReservationSlot().getTime().getId(),
                LinkedHashMap::new,
                java.util.stream.Collectors.toList()
            ))
            .values()
            .stream()
            .map(reservations -> {
                Reservation firstReservation = reservations.getFirst();
                return ReservationCountResult.of(
                    firstReservation.getReservationSlot().getTime().getId(),
                    firstReservation.getReservationSlot().getTime().getStartAt(),
                    reservations.size()
                );
            })
            .sorted(Comparator.comparing(ReservationCountResult::startAt))
            .toList();
    }

    private boolean isActive(Reservation reservation) {
        return reservation.getStatus() != ReservationStatus.CANCELED;
    }

    private ReservationWithWaitingNumber withWaitingNumber(Reservation reservation) {
        return new ReservationWithWaitingNumber(
            reservation,
            waitingNumberOf(reservation)
        );
    }

    private Long waitingNumberOf(Reservation reservation) {
        if (reservation.getStatus() != ReservationStatus.WAITING) {
            return null;
        }
        List<Reservation> orderedReservations = storage.values().stream()
            .filter(this::isActive)
            .filter(storedReservation -> storedReservation.getStatus() == ReservationStatus.WAITING)
            .filter(storedReservation -> reservation.getReservationSlot().getId()
                .equals(storedReservation.getReservationSlot().getId()))
            .sorted(Comparator.comparing(Reservation::getUpdatedAt)
                .thenComparing(Reservation::getId))
            .toList();

        int order = orderedReservations.indexOf(reservation);
        if (order < 0) {
            return null;
        }
        return (long) order + 1;
    }
}
