package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import roomescape.domain.Waiting;

public interface WaitingRepository {

    int calculateWaitingNumber(Waiting waiting);

    void save(Waiting waiting);

    void delete(Waiting waiting);

    boolean isExists(Waiting waiting);

    int countAllBy(LocalDate date, Long timeSlotId, Long themeId);

    List<Waiting> findByName(String name);
}
