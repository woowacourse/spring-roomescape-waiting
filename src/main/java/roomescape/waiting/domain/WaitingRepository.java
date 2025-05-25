package roomescape.waiting.domain;

import java.util.List;
import java.util.Optional;

public interface WaitingRepository {

    Waiting save(Waiting waiting);

    void deleteById(Long id);

    List<Waiting> findAll();

    Optional<Waiting> findById(Long id);
}
