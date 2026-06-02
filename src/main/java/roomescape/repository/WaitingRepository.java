package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.service.dto.WaitingWithNumber;
import roomescape.domain.Waiting;

public interface WaitingRepository {

    Waiting save(Waiting waiting);

    void deleteById(Long id);

    boolean exists(String name, LocalDate date, Long timeId, Long themeId);

    List<WaitingWithNumber> findByName(String name);

    Optional<Waiting> findById(long id);

    Optional<WaitingWithNumber> findWaitingWithNumberById(long id);
}
