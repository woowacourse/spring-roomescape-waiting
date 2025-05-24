package roomescape.domain.repository;

import java.util.List;
import java.util.Optional;
import roomescape.domain.Waiting;

public interface WaitingRepository {

    Waiting save(Waiting waiting);

    Optional<Waiting> findById(Long id);

    List<Waiting> findByMemberId(Long id);

    void deleteById(Long id);
}
