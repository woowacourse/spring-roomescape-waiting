package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.repository.dto.WaitingWithNumber;
import roomescape.domain.Waiting;

public interface WaitingRepository {

    void save(Waiting waiting);

    void deleteById(Long id);

    boolean exists(String name, LocalDate date, Long timeId, Long themeId);

    List<WaitingWithNumber> findByName(String name);

    Optional<Waiting> findById(long id);
}
