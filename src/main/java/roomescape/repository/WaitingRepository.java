package roomescape.repository;

import java.util.List;
import java.util.Optional;
import roomescape.domain.Waiting;

public interface WaitingRepository {

    void save(Waiting waiting);

    void deleteById(Long id);

    boolean isExists(Waiting waiting);

    List<Waiting> findByName(String name);

    Optional<Waiting> findById(long id);
}
