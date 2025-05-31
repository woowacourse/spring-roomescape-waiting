package roomescape.unit.fake;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import roomescape.domain.Member;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.domain.WaitingWithRank;
import roomescape.domain.repository.WaitingRepository;

public class FakeWaitingRepository implements WaitingRepository {

    private final List<Waiting> store = new ArrayList<>();
    private final AtomicLong index = new AtomicLong(1L);

    @Override
    public Waiting save(final Waiting waiting) {
        Waiting waitingWithId = new Waiting(
                index.getAndIncrement(),
                waiting.getMember(),
                waiting.getDate(),
                waiting.getReservationTime(),
                waiting.getTheme()
        );

        store.add(waitingWithId);
        return waitingWithId;
    }

    @Override
    public Optional<Waiting> findById(final Long id) {
        return store.stream()
                .filter(waiting -> waiting.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<Waiting> findAll() {
        return new ArrayList<>(store);
    }

    @Override
    public void deleteById(final Long id) {
        store.removeIf(waiting -> waiting.getId().equals(id));
    }

    @Override
    public List<Waiting> findByMemberId(Long id) {
        return store.stream()
                .filter(waiting -> waiting.getMember().getId().equals(id))
                .collect(Collectors.toList());
    }

    @Override
    public List<Waiting> findByThemeId(Long id) {
        return store.stream()
                .filter(waiting -> waiting.getTheme().getId().equals(id))
                .collect(Collectors.toList());
    }

    @Override
    public List<Waiting> findByReservationTimeId(Long id) {
        return store.stream()
                .filter(waiting -> waiting.getReservationTime().getId().equals(id))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Waiting> findByDateAndReservationTimeAndThemeAndMember(LocalDate date,
                                                                           ReservationTime time,
                                                                           Theme theme,
                                                                           Member member) {
        return store.stream()
                .filter(waiting -> waiting.getDate().equals(date) &&
                        waiting.getReservationTime().getId().equals(time.getId()) &&
                        waiting.getTheme().getId().equals(theme.getId()) &&
                        waiting.getMember().getId().equals(member.getId()))
                .findFirst();
    }

    @Override
    public List<WaitingWithRank> findByMemberIdSortedByCreateAt(Long memberId) {
        List<Waiting> memberWaitings = store.stream()
                .filter(waiting -> waiting.getMember().getId().equals(memberId))
                .sorted(Comparator.comparing(Waiting::getCreateAt))
                .toList();

        return memberWaitings.stream()
                .map(waiting -> {
                    long rank = calculateRank(waiting);
                    return new WaitingWithRank(waiting, rank);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<WaitingWithRank> findByDateAndReservationTimeAndThemeSortedByCreateAt(LocalDate date,
                                                                                      Long timeId,
                                                                                      Long themeId) {
        List<Waiting> filteredWaitings = store.stream()
                .filter(waiting -> waiting.getDate().equals(date) &&
                        waiting.getReservationTime().getId().equals(timeId) &&
                        waiting.getTheme().getId().equals(themeId))
                .sorted(Comparator.comparing(Waiting::getCreateAt))
                .toList();

        return filteredWaitings.stream()
                .map(waiting -> {
                    long rank = calculateRank(waiting);
                    return new WaitingWithRank(waiting, rank);
                })
                .collect(Collectors.toList());
    }

    private long calculateRank(Waiting waiting) {
        return store.stream()
                .filter(w -> w.getTheme().getId().equals(waiting.getTheme().getId()) &&
                        w.getDate().equals(waiting.getDate()) &&
                        w.getReservationTime().getId().equals(waiting.getReservationTime().getId()) &&
                        w.getCreateAt().isBefore(waiting.getCreateAt()))
                .count() + 1;
    }
}