package roomescape.waiting.domain;

import java.util.Collection;

public interface WaitingRepository {
    Waiting save(Waiting waiting);

    void deleteById(Long id);

    Collection<Waiting> findAll();
}
