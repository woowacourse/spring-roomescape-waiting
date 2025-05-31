package roomescape.support.fake;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.common.exception.RoomescapeException;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingRepository;
import roomescape.waiting.domain.WaitingWithRank;

public class FakeWaitingRepository implements WaitingRepository {

    private final List<Waiting> waitings = new ArrayList<>();
    private Long index = 1L;

    @Override
    public boolean existsByReservationId(final long reservationId) {
        return waitings.stream()
                .anyMatch(waiting -> waiting.reservation().id() == reservationId);
    }

    @Override
    public boolean existsByReservationIdAndMemberId(final long reservationId, final long memberId) {
        return waitings.stream()
                .anyMatch(waiting -> waiting.reservation().id() == reservationId &&
                        waiting.member().id() == memberId);
    }

    @Override
    public Waiting save(final Waiting waiting) {
        final Waiting newWaiting = new Waiting(index++, waiting.reservation(), waiting.member());
        waitings.add(newWaiting);
        return newWaiting;
    }

    @Override
    public void deleteById(final long id) {
        final Waiting waiting = findById(id);
        waitings.remove(waiting);
    }

    @Override
    public List<Waiting> findAll() {
        return waitings;
    }

    @Override
    public List<WaitingWithRank> findAllWithRankByMemberId(final long memberId) {
        final AtomicLong rank = new AtomicLong(1);

        return waitings.stream()
                .filter(waiting -> waiting.member().id() == memberId)
                .map(waiting -> new WaitingWithRank(waiting, rank.getAndIncrement()))
                .toList();
    }

    @Override
    public Optional<Waiting> findTopByReservationId(final long reservationId) {
        return waitings.stream()
                .filter(waiting -> waiting.reservation().id() == reservationId)
                .findFirst();
    }

    private Waiting findById(final long id) {
        return waitings.stream()
                .filter(reservation -> reservation.id() == id)
                .findFirst()
                .orElseThrow(() -> new RoomescapeException("예약 대기가 존재하지 않습니다."));
    }
}
