package roomescape.repository;

import roomescape.domain.Waiting;

import java.util.List;
import java.util.Optional;

public interface WaitingRepository {

    int calculateWaitingNumber(Waiting waiting);

    Waiting save(Waiting waiting);

    void deleteById(Long id);

    boolean isExists(Waiting waiting);

    boolean isExistsBySessionId(long sessionId);

    List<Waiting> findByName(String name);

    Optional<Waiting> findById(long id);

    Waiting findFirstBySessionId(long sessionId);

    List<Waiting> findAllBySessionId(long sessionId);
}
