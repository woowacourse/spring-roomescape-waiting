package roomescape.fake;

import java.util.ArrayList;
import java.util.List;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.repository.WaitingRepository;

public class FakeWaitingRepository implements WaitingRepository {

    List<Waiting> waitings = new ArrayList<>();
    Long index = 1L;

    @Override
    public Waiting save(Waiting waiting) {
        Waiting newWaiting = new Waiting(index++, waiting.getDate(), waiting.getTime(), waiting.getTheme(),
                waiting.getMember());
        waitings.add(newWaiting);
        return newWaiting;
    }
}
