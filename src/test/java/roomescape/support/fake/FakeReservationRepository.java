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
    public List<Reservation> findAll() {
        return storage.values().stream()
            .map(this::withCalculatedWaitingOrder)
            .toList();
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        return Optional.ofNullable(storage.get(id))
            .map(this::withCalculatedWaitingOrder);
    }

    @Override
    public List<Reservation> findReservations(String username) {
        return storage.values().stream()
            .filter(userReservation -> username.equals(userReservation.getUser().getName()))
            .sorted(Comparator.comparing(Reservation::getId).reversed())
            .map(this::withCalculatedWaitingOrder)
            .toList();
    }

    @Override
    public Long countByReservationSlotId(Long reservationSlotId) {
        return storage.values().stream()
            .filter(userReservation -> reservationSlotId.equals(userReservation.getReservationSlot().getId()))
            .count();
    }

    @Override
    public List<Reservation> findAllByReservationIdOrder(Long reservationId) {
        return storage.values().stream()
            .filter(userReservation -> reservationId.equals(userReservation.getReservationSlot().getId()))
            .sorted(Comparator.comparing(Reservation::getUpdatedAt)
                .thenComparing(Reservation::getId))
            .map(this::withCalculatedWaitingOrder)
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
            .filter(userReservation -> userId.equals(userReservation.getUser().getId()))
            .filter(userReservation -> reservationId.equals(userReservation.getReservationSlot().getId()))
            .findAny()
            .isPresent();
    }

    @Override
    public void deleteById(Long id) {
        storage.remove(id);
    }

    @Override
    public List<ReservationCountResult> countReservation(Long themeId, Long dateId) {
        return storage.values().stream()
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

    private Reservation withCalculatedWaitingOrder(Reservation reservation) {
        List<Reservation> orderedReservations = storage.values().stream()
            .filter(storedReservation -> reservation.getReservationSlot().getId()
                .equals(storedReservation.getReservationSlot().getId()))
            .sorted(Comparator.comparing(Reservation::getUpdatedAt)
                .thenComparing(Reservation::getId))
            .toList();

        int order = orderedReservations.indexOf(reservation);
        if (order == 0) {
            return Reservation.of(
                reservation.getId(),
                reservation.getReservationSlot(),
                reservation.getUser(),
                null,
                ReservationStatus.CONFIRMED,
                reservation.getCreatedAt(),
                reservation.getUpdatedAt()
            );
        }
        return Reservation.of(
            reservation.getId(),
            reservation.getReservationSlot(),
            reservation.getUser(),
            (long) order,
            ReservationStatus.WAITING,
            reservation.getCreatedAt(),
            reservation.getUpdatedAt()
        );
    }
}
