package roomescape.unit.fake;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.dto.response.WaitingWithRankResponse;
import roomescape.reservation.infrastructure.WaitingRepository;

public class FakeWaitingRepository implements WaitingRepository {

    private final List<Waiting> waitings = new ArrayList<>();
    private final AtomicLong index = new AtomicLong(1L);

    @Override
    public Waiting save(Waiting waiting) {
        Waiting newWaiting = Waiting.builder()
                .id(index.getAndIncrement())
                .reservationTime(waiting.getReservationTime())
                .member(waiting.getMember())
                .theme(waiting.getTheme()).build();
        waitings.add(newWaiting);
        return newWaiting;
    }

    @Override
    public List<WaitingWithRankResponse> findByMemberIdWithRank(Long memberId) {
        List<Waiting> findWaitings = waitings.stream()
                .filter(waiting -> waiting.getMember().getId().equals(memberId))
                .toList();
        return findWaitings.stream()
                .map(w -> new WaitingWithRankResponse(w, countPreviousWaiting(w) + 1))
                .toList();
    }

    private Long countPreviousWaiting(Waiting waiting) {
        return waitings.stream()
                .filter(w -> w.getTheme().equals(waiting.getTheme()))
                .filter(w -> w.getReservationTime().equals(waiting.getReservationTime()))
                .filter(w ->
                        w.getQueuedAt().isBefore(waiting.getQueuedAt()) ||
                        w.getQueuedAt().equals(waiting.getQueuedAt()) && w.getId() < waiting.getId())
                .count();
    }

    @Override
    public Optional<Waiting> findById(Long id) {
        return waitings.stream()
                .filter(waiting -> waiting.getId().equals(id))
                .findFirst();
    }

    @Override
    public void delete(Waiting waiting) {
        waitings.remove(waiting);
    }

    @Override
    public List<Waiting> findAll() {
        return waitings;
    }

    @Override
    public boolean existsByReservationTimeAndMemberAndTheme(ReservationTime time, Member member, Theme theme) {
        return waitings.stream()
                .filter(waiting -> waiting.getReservationTime().equals(time))
                .filter(waiting -> waiting.getMember().equals(member))
                .anyMatch(waiting -> waiting.getTheme().equals(theme));
    }

    @Override
    public Optional<Waiting> findFirstByReservationTimeAndThemeOrderById(ReservationTime time, Theme theme) {
        return waitings.stream()
                .filter(waiting -> waiting.getReservationTime().equals(time))
                .filter(waiting -> waiting.getTheme().equals(theme))
                .min(Comparator.comparingLong(Waiting::getId));
    }
}
