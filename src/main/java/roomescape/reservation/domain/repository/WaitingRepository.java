package roomescape.reservation.domain.repository;

import java.util.Optional;
import roomescape.reservation.domain.Waiting;

public interface WaitingRepository {
    Optional<Waiting> findById(Long id);

    Waiting save(Waiting waiting);

    Long getRank(Waiting waiting);

    Integer delete(Long id);
}
