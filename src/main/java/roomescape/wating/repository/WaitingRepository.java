package roomescape.wating.repository;

import java.util.Optional;
import roomescape.wating.domain.Waiting;

public interface WaitingRepository {

    Long save(Waiting waiting);

    boolean deleteById(long id);

    Optional<Waiting> findById(long id);
}
