package roomescape.fake;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.repository.WaitingRepository;

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
    public long countByDateAndTimeAndTheme(LocalDate date, ReservationTime time, Theme theme) {
        return waitings.stream()
                .filter(waiting -> waiting.getDate().equals(date))
                .filter(waiting -> waiting.getTime().getId().equals(time.getId()))
                .filter(waiting -> waiting.getTheme().getId().equals(theme.getId()))
                .count();
    }
}
