package roomescape.fake;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.test.util.ReflectionTestUtils;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.repository.WaitingRepository;

public class FakeWaitingRepository implements WaitingRepository {

    List<Waiting> waitings = new ArrayList<>();
    Long index = 1L;

    @Override
    public Waiting save(Waiting waiting) {
        Waiting newWaiting = new Waiting(index++, waiting.getDate(), waiting.getTime(), waiting.getTheme(),
                waiting.getMember(), waiting.getPriority());
        waitings.add(newWaiting);
        return newWaiting;
    }

    @Override
    public long countByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId) {
        return waitings.stream()
                .filter(waiting -> waiting.getDate().equals(date))
                .filter(waiting -> waiting.getTime().getId().equals(timeId))
                .filter(waiting -> waiting.getTheme().getId().equals(themeId))
                .count();
    }

    @Override
    public boolean existsByDateAndThemeIdAndTimeIdAndMemberId(LocalDate date, long themeId, long timeId,
                                                              long memberId) {
        return waitings.stream()
                .filter(waiting -> waiting.getDate().equals(date))
                .filter(waiting -> waiting.getTime().getId().equals(timeId))
                .filter(waiting -> waiting.getTheme().getId().equals(themeId))
                .anyMatch(waiting -> waiting.getMember().getId().equals(memberId));
    }

    @Override
    public List<Waiting> findAllByMemberId(long memberId) {
        return waitings.stream()
                .filter(waiting -> waiting.getMember().getId() == memberId)
                .toList();
    }

    @Override
    public void delete(Waiting other) {
        waitings.removeIf(waiting -> waiting.getId() == other.getId());
    }

    @Override
    public boolean existsByIdAndMemberId(long id, long memberId) {
        return waitings.stream()
                .filter(waiting -> waiting.getMember().getId() == memberId)
                .anyMatch(waiting -> waiting.getId() == id);
    }

    @Override
    public void pullPriority(Theme theme, LocalDate date, ReservationTime reservationTime, long fromPriority,
                             int amount) {
        waitings.stream()
                .filter(waiting -> waiting.getDate().equals(date))
                .filter(waiting -> waiting.getTime().getId() == reservationTime.getId())
                .filter(waiting -> waiting.getTheme().getId() == theme.getId())
                .filter(waiting -> waiting.getPriority() >= fromPriority)
                .forEach(waiting ->
                        ReflectionTestUtils.setField(waiting, "priority", waiting.getPriority() - amount)
                );
    }

    @Override
    public Optional<Waiting> findById(long id) {
        return waitings.stream()
                .filter(waiting -> waiting.getId() == id)
                .findAny();
    }

    @Override
    public List<Waiting> findAll() {
        return new ArrayList<>(waitings);
    }

    @Override
    public Optional<Waiting> popFirstWaiting(Theme theme, LocalDate date, ReservationTime time) {
        return waitings.stream()
                .filter(waiting -> waiting.getDate().equals(date))
                .filter(waiting -> waiting.getTime().getId() == time.getId())
                .filter(waiting -> waiting.getTheme().getId() == theme.getId())
                .filter(waiting -> waiting.getPriority() == 0)
                .findAny();
    }
}
