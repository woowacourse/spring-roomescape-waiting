package roomescape.support.fake;

import java.util.ArrayList;
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
        return new ArrayList<>(storage.values());
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Reservation> findReservations(String username) {
        return storage.values().stream()
            .filter(userReservation -> username.equals(userReservation.getUser().getName()))
            .sorted(Comparator.comparing(Reservation::getId).reversed())
            .toList();
    }

    @Override
    public Integer countByReservationSlotId(Long reservationSlotId) {
        return Math.toIntExact(storage.values().stream()
                .filter(userReservation -> reservationSlotId.equals(userReservation.getReservationSlot().getId()))
                .count());
    }

    @Override
    public List<Reservation> findAllBySlotIdOrderByWaitingNumber(Long reservationId) {
        return storage.values().stream()
            .filter(userReservation -> reservationId.equals(userReservation.getReservationSlot().getId()))
            .sorted(Comparator.comparing(Reservation::getReservedAt)
                .thenComparing(Reservation::getId))
            .toList();
    }

    @Override
    public Optional<Reservation> update(Long id, Reservation userReservation) {
        if (!storage.containsKey(id)) {
            return Optional.empty();
        }
        Reservation updatedUserReservation = Reservation.createWithId(id, userReservation);
        storage.put(id, updatedUserReservation);
        return Optional.of(updatedUserReservation);
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
    public void batchUpdate(List<Reservation> reservations) {
        for (Reservation reservation : reservations) {
            if (reservation.getId() == null) {
                continue;
            }
            storage.put(reservation.getId(), Reservation.createWithId(reservation.getId(), reservation));
        }
    }

    @Override
    public void deleteById(Long id) {
        storage.remove(id);
    }

    @Override
    public List<ReservationCountResult> countWaitingReservationsByThemeAndDate(Long themeId, Long dateId) {
        return storage.values().stream()
            .filter(reservation -> themeId.equals(reservation.getReservationSlot().getTheme().getId()))
            .filter(reservation -> dateId.equals(reservation.getReservationSlot().getDate().getId()))
            .filter(reservation -> reservation.getStatus() == ReservationStatus.WAITING)
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
}
