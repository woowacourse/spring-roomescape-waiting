package roomescape.waiting.infrastructure;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.test.util.ReflectionTestUtils;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingRepository;
import roomescape.waiting.domain.WaitingStatus;

public class FakeWaitingRepository implements WaitingRepository {
    private final List<Waiting> waitings;
    private AtomicLong index = new AtomicLong(0);

    public FakeWaitingRepository(List<Waiting> waitings) {
        this.waitings = waitings;
    }


    @Override
    public Waiting save(Waiting waiting) {
        long currentIndex = index.incrementAndGet();

        ReflectionTestUtils.setField(waiting, "id", currentIndex);

        waitings.add(waiting);
        return waiting;
    }

    @Override
    public List<Waiting> findByMemberIdAndStatus(Long memberId, WaitingStatus status) {
        return waitings.stream()
                .filter(w -> w.getMember().getId().equals(memberId))
                .filter(w -> w.getStatus() == status)
                .toList();
    }

    @Override
    public long countByDateAndThemeIdAndTimeIdAndStatusAndCreatedAtBefore(LocalDate date, Long themeId, Long timeId, WaitingStatus status, LocalDateTime createdAt) {
        return waitings.stream()
                .filter(w -> w.getDate().equals(date))
                .filter(w -> w.getTheme().getId().equals(themeId))
                .filter(w -> w.getTime().getId().equals(timeId))
                .filter(w -> w.getStatus() == status)
                .filter(w -> w.getCreatedAt().isBefore(createdAt))
                .count();
    }

    @Override
    public long countByDateAndThemeIdAndTimeIdAndStatus(LocalDate date, Long themeId, Long timeId, WaitingStatus status) {
        return waitings.stream()
                .filter(w -> w.getDate().equals(date))
                .filter(w -> w.getTheme().getId().equals(themeId))
                .filter(w -> w.getTime().getId().equals(timeId))
                .filter(w -> w.getStatus() == status)
                .count();
    }

    @Override
    public Optional<Waiting> findById(Long id) {
        return waitings.stream()
                .filter(w -> w.getId().equals(id))
                .findFirst();
    }

    @Override
    public boolean existsByMemberIdAndDateAndTimeIdAndStatus(Long memberId, LocalDate date, Long timeId, WaitingStatus status) {
        return waitings.stream()
                .anyMatch(w -> w.getMember().getId().equals(memberId)
                        && w.getDate().equals(date)
                        && w.getTime().getId().equals(timeId)
                        && w.getStatus() == status);
    }

    @Override
    public List<Waiting> findAll() {
        return Collections.unmodifiableList(waitings);
    }

    @Override
    public List<Waiting> findByDateAndThemeIdAndTimeIdAndStatusOrderByCreatedAtAsc(LocalDate date, Long themeId, Long timeId, WaitingStatus status) {
        return waitings.stream()
                .filter(w -> w.getDate().equals(date))
                .filter(w -> w.getTheme().getId().equals(themeId))
                .filter(w -> w.getTime().getId().equals(timeId))
                .filter(w -> w.getStatus() == status)
                .sorted((w1, w2) -> w1.getCreatedAt().compareTo(w2.getCreatedAt()))
                .toList();
    }

    @Override
    public void delete(Waiting waiting) {
        waitings.removeIf(w -> w.getId().equals(waiting.getId()));
    }
}
