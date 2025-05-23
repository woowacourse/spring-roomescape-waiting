package roomescape.fake;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
    public boolean existsByDateAndThemeIdAndTimeIdAndMemberId(LocalDate date, long themeId, long timeId, long memberId) {
        return waitings.stream()
                .filter(waiting -> waiting.getDate().equals(date))
                .filter(waiting -> waiting.getTime().getId().equals(timeId))
                .filter(waiting -> waiting.getTheme().getId().equals(themeId))
                .anyMatch(waiting -> waiting.getMember().getId().equals(memberId));
    }
}
