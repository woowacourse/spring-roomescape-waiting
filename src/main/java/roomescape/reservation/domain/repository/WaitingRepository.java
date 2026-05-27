package roomescape.reservation.domain.repository;

import java.util.Optional;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.repository.dto.WaitingDetail;

public interface WaitingRepository {
    Optional<WaitingDetail> findDetailById(Long id);

    Waiting save(Waiting waiting);

    Integer delete(Long id);
}
