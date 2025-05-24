package roomescape.support.fake;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.reservation.domain.waiting.ReservationWaiting;
import roomescape.reservation.domain.waiting.ReservationWaitingRepository;
import roomescape.reservation.domain.waiting.ReservationWaitingWithRank;

public class FakeReservationWaitingRepository implements ReservationWaitingRepository {

    private final List<ReservationWaiting> reservationWaitings = new ArrayList<>();
    private Long index = 1L;

    @Override
    public boolean existsByReservationIdAndMemberId(final long reservationId, final long memberId) {
        return reservationWaitings.stream()
                .anyMatch(reservationWaiting -> reservationWaiting.reservation().id() == reservationId &&
                        reservationWaiting.member().id() == memberId);
    }

    @Override
    public ReservationWaiting save(final ReservationWaiting reservationWaiting) {
        final ReservationWaiting newReservationWaiting = new ReservationWaiting(index++,
                reservationWaiting.reservation(), reservationWaiting.member());
        reservationWaitings.add(newReservationWaiting);

        return newReservationWaiting;
    }

    @Override
    public void deleteById(final long id) {
        final ReservationWaiting reservationWaiting = findById(id);
        reservationWaitings.remove(reservationWaiting);
    }

    @Override
    public List<ReservationWaiting> findAll() {
        return reservationWaitings;
    }

    @Override
    public List<ReservationWaitingWithRank> findAllWithRankByMemberId(final long memberId) {
        final AtomicLong rank = new AtomicLong(1);

        return reservationWaitings.stream()
                .filter(waiting -> waiting.member().id() == memberId)
                .map(waiting -> new ReservationWaitingWithRank(waiting, rank.getAndIncrement()))
                .toList();
    }

    private ReservationWaiting findById(final long id) {
        return reservationWaitings.stream()
                .filter(reservation -> reservation.id() == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("예약 대기가 존재하지 않습니다."));
    }
}
