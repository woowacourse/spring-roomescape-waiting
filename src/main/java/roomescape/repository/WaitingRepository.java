package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import roomescape.domain.Waiting;

public interface WaitingRepository {
    Waiting save(Waiting waiting);

    List<Waiting> findBySlot(LocalDate date, Long timeId, Long themeId);
}
