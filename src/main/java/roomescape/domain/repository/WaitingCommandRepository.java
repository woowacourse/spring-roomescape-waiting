package roomescape.domain.repository;

import org.springframework.data.repository.Repository;
import roomescape.domain.Waiting;

public interface WaitingCommandRepository extends Repository<Waiting, Long> {
    Waiting save(Waiting waiting);

    void deleteById(Long waitingId);
}
