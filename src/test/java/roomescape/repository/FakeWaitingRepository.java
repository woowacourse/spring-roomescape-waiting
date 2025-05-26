package roomescape.repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.dao.DuplicateKeyException;
import roomescape.reservation.domain.ReservationDetails;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.WaitingWithRank;
import roomescape.reservation.repository.WaitingRepository;

public class FakeWaitingRepository implements WaitingRepository {

    private final List<Waiting> waitings;
    private final AtomicLong waitingId;

    public FakeWaitingRepository(List<Waiting> waitings) {
        this.waitings = new ArrayList<>(waitings);
        this.waitingId = new AtomicLong(waitings.size() + 1);
    }

    @Override
    public Waiting save(Waiting waiting) {
        List<Waiting> existingWaitings = findByDateTimeThemeMember(waiting.getDate(), waiting.getTime().getId(), waiting.getTheme().getId(), waiting.getMember().getId());
        if (!existingWaitings.isEmpty()) {
            throw new DuplicateKeyException("동일한 예약대기가 존재합니다.");
        }
        long id = waitingId.getAndIncrement();
        Waiting newWaiting = new Waiting(id, waiting.getMember(), new ReservationDetails(waiting.getDate(), waiting.getTime(), waiting.getTheme()));
        waitings.add(newWaiting);
        return newWaiting;
    }

    private List<Waiting> findByDateTimeThemeMember(LocalDate date, Long timeId, Long themeId, Long memberId) {
        return waitings.stream()
                .filter(waiting -> waiting.getTheme().getId().equals(themeId))
                .filter(waiting -> waiting.getDate().equals(date))
                .filter(waiting -> waiting.getTime().getId().equals(timeId))
                .filter(waiting -> waiting.getMember().getId().equals(memberId))
                .toList();
    }

    @Override
    public List<Waiting> findAll() {
        return waitings;
    }

    @Override
    public List<WaitingWithRank> findWaitingWithRankByMemberId(Long memberId) {
        List<Waiting> waitingsByMemberId = waitings.stream()
                .filter(waiting -> Objects.equals(waiting.getMember().getId(), memberId))
                .toList();
        if (waitingsByMemberId.isEmpty()) return List.of();
        return waitingsByMemberId.stream()
                .map(waiting -> new WaitingWithRank(waiting, findRank(waiting.getId())))
                .toList();
    }

    private long findRank(Long id) {
        List<Long> ids = waitings.stream()
                .filter(waiting -> waiting.getId().equals(id))
                .sorted(Comparator.comparing(Waiting::getId))
                .map(Waiting::getId)
                .toList();
        return ids.indexOf(id) + 1;
    }

    @Override
    public Optional<Waiting> findById(Long id) {
        return waitings.stream()
                .filter(waiting -> Objects.equals(waiting.getId(), id))
                .findFirst();
    }

    @Override
    public Optional<Waiting> findByIdAndMemberId(Long id, Long memberId) {
        return waitings.stream()
                .filter(waiting -> Objects.equals(waiting.getId(), id))
                .filter(waiting -> Objects.equals(waiting.getMember().getId(), memberId))
                .findFirst();
    }

    @Override
    public Optional<Waiting> findFirstWaitingByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
        List<Waiting> waitings = findByDateTimeTheme(date, timeId, themeId);
        if (waitings.isEmpty()) return Optional.empty();
        return Optional.of(waitings.getFirst());
    }

    private List<Waiting> findByDateTimeTheme(LocalDate date, Long timeId, Long themeId) {
        return waitings.stream()
                .filter(waiting -> waiting.getTheme().getId().equals(themeId))
                .filter(waiting -> waiting.getDate().equals(date))
                .filter(waiting -> waiting.getTime().getId().equals(timeId))
                .sorted(Comparator.comparing(Waiting::getId))
                .toList();
    }

    @Override
    public boolean existsByDateAndTimeIdAndThemeIdAndMemberId(LocalDate date, Long timeId, Long themeId, Long memberId) {
        return waitings.stream()
                .filter(w -> w.getTheme().getId().equals(themeId))
                .filter(w -> w.getTime().getId().equals(timeId))
                .filter(w -> w.getMember().getId().equals(memberId))
                .anyMatch(w -> w.getDate().equals(date));
    }

    @Override
    public void deleteById(Long id) {
        waitings.removeIf(waiting -> waiting.getId().equals(id));
    }
}
