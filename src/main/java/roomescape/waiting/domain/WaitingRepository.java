package roomescape.waiting.domain;

import java.util.Optional;

public interface WaitingRepository {
    Waiting save(Waiting waiting);
    Optional<Waiting> findById(Long id);
    void deleteByIdAndName(Long id, String name);
}
