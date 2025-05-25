package roomescape.support.fake;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.reservation.domain.waiting.Waiting;
import roomescape.reservation.domain.waiting.WaitingRepository;
import roomescape.reservation.domain.waiting.WaitingWithRank;

public class FakeWaitingRepository implements WaitingRepository {

    private final List<Waiting> waitings = new ArrayList<>();
    private Long index = 1L;

    @Override
    public boolean existsByReservation(final LocalDate date, final long timeId, final long themeId) {
        return waitings.stream()
                .anyMatch(waiting -> waiting.date() == date &&
                        waiting.time().id() == timeId &&
                        waiting.theme().id() == themeId);
    }

    @Override
    public     boolean existsByReservationAndMemberId(LocalDate date, long timeId, long themeId, long memberId) {
        return waitings.stream()
                .anyMatch(waiting -> waiting.date() == date &&
                        waiting.time().id() == timeId &&
                        waiting.theme().id() == themeId &&
                        waiting.member().id() == memberId);
    }

    @Override
    public Waiting save(final Waiting waiting) {
        final Waiting newWaiting = new Waiting(index++,
                waiting.date(), waiting.time(), waiting.theme(), waiting.member());
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
    public Optional<Waiting> findTopByReservation(final LocalDate date, final long timeId,
                                                  final long themeId) {
        return waitings.stream()
                .filter(waiting -> waiting.date() == date && waiting.time().id() == timeId && waiting.theme().id() == themeId)
                .findFirst();
    }

    private Waiting findById(final long id) {
        return waitings.stream()
                .filter(reservation -> reservation.id() == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("예약 대기가 존재하지 않습니다."));
    }
}
