package roomescape.repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.domain.BaseEntity;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationWaiting;

public class CollectionReservationWaitingRepository implements
        ReservationWaitingRepository {
    private final List<ReservationWaiting> reservationWaitings;
    private final AtomicLong atomicLong;

    public CollectionReservationWaitingRepository() {
        this(new ArrayList<>(), new AtomicLong(0));
    }

    public CollectionReservationWaitingRepository(List<ReservationWaiting> reservationWaitings, AtomicLong atomicLong) {
        this.reservationWaitings = reservationWaitings;
        this.atomicLong = atomicLong;
    }

    @Override
    public ReservationWaiting save(ReservationWaiting reservationWaiting) {
        ReservationWaiting withId = new ReservationWaiting(atomicLong.incrementAndGet(), reservationWaiting);
        reservationWaitings.add(withId);
        return withId;
    }

    @Override
    public List<ReservationWaiting> findAll() {
        return List.copyOf(reservationWaitings);
    }

    @Override
    public List<ReservationWaiting> findAllByMemberId(long memberId) {
        return reservationWaitings.stream()
                .filter(reservationWaiting -> reservationWaiting.getWaitingMember().hasIdOf(memberId))
                .toList();
    }

    @Override
    public List<ReservationWaiting> findByReservation(Reservation reservation) {
        return reservationWaitings.stream()
                .filter(reservationWaiting -> reservationWaiting.getReservation().equals(reservation))
                .toList();
    }

    @Override
    public Optional<ReservationWaiting> findTopWaitingByReservation(Reservation reservation) {
        return findByReservation(reservation)
                .stream()
                .sorted(Comparator.comparing(BaseEntity::getCreateAt))
                .limit(1)
                .findAny();
    }

    @Override
    public boolean existsByReservationAndWaitingMember(Reservation reservation, Member waitingMember) {
        return reservationWaitings.stream()
                .filter(reservationWaiting -> reservationWaiting.getReservation().equals(reservation))
                .anyMatch(reservationWaiting -> reservationWaiting.getWaitingMember().equals(waitingMember));
    }

    @Override
    public void delete(long id) {
        reservationWaitings.stream()
                .filter(reservationWaiting -> reservationWaiting.hasIdOf(id))
                .findAny()
                .ifPresent(reservationWaitings::remove);
    }
}
