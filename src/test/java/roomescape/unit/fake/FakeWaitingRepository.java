package roomescape.unit.fake;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.TimeSlot;
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
                .date(waiting.getDate())
                .timeSlot(waiting.getTimeSlot())
                .member(waiting.getMember())
                .theme(waiting.getTheme()).build();
        waitings.add(newWaiting);
        return newWaiting;
    }

    @Override
    public boolean existsByDateAndMemberAndThemeAndTimeSlot(LocalDate date, Member member, Theme theme,
                                                            TimeSlot timeSlot) {
        return waitings.stream()
                .filter(waiting -> waiting.getDate().equals(date))
                .filter(waiting -> waiting.getMember().equals(member))
                .filter(waiting -> waiting.getTheme().equals(theme))
                .anyMatch(waiting -> waiting.getTimeSlot().equals(timeSlot));
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
                .filter(w -> w.getTimeSlot().equals(waiting.getTimeSlot()))
                .filter(w -> w.getDate().equals(waiting.getDate()))
                .filter(w -> w.getId() < waiting.getId())
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
}
