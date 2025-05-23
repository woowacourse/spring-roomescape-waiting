package roomescape.waiting.repository;

import org.springframework.stereotype.Repository;
import roomescape.waiting.domain.Waiting;

@Repository
public interface WaitingRepository {
    Waiting save(Waiting waiting);
}
