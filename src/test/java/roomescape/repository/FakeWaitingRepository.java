package roomescape.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.member.domain.Member;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingStatus;
import roomescape.waiting.dto.WaitingWithRank;
import roomescape.waiting.repository.WaitingRepository;

public class FakeWaitingRepository implements WaitingRepository {

    private final List<Waiting> waitings;
    private final AtomicLong waitingId;

    public FakeWaitingRepository(List<Waiting> waitings) {
        this.waitings = new ArrayList<>(waitings);
        this.waitingId = new AtomicLong(waitings.size() + 1);
    }

    @Override
    public Waiting save(Waiting waiting) {
        Waiting savedWaiting = new Waiting(
                waitingId.getAndIncrement(),
                waiting.getDate(),
                waiting.getMember(),
                waiting.getTheme(),
                waiting.getTime(),
                LocalDateTime.now(),
                WaitingStatus.PENDING);
        waitings.add(savedWaiting);
        return savedWaiting;
    }

    @Override
    public List<Waiting> findAllEligibleWaitingForReservation() {
        return waitings;
    }

    @Override
    public List<WaitingWithRank> findWaitingWithRankByMemberId(Long memberId) {
        return waitings.stream()
                .map(w -> new WaitingWithRank(w, 1L))
                .toList();
    }

    @Override
    public Optional<Waiting> findById(Long id) {
        return waitings.stream()
                .filter(waiting -> waiting.getId().equals(id))
                .findAny();
    }

    @Override
    public boolean existsByMemberAndDateAndTime(Member member, LocalDate date, ReservationTime time) {
        return waitings.stream()
                .filter(w -> w.getMember().equals(member))
                .filter(w -> w.getDate().equals(date))
                .anyMatch(w -> w.getTime().equals(time));
    }

    @Override
    public Long countRankByCreateAt(Waiting waiting) {
        return 0L;
    }

    @Override
    public void delete(Waiting waiting) {
        waitings.removeIf(w -> w.equals(waiting));
    }
}
