package roomescape.repository;

import roomescape.domain.Waiting;

import java.util.List;
import java.util.Optional;

public interface WaitingRepository {

    Optional<Waiting> findById(long id);

    List<Waiting> findUserWaitingList(String name, int page, int size);

    Optional<Waiting> findByScheduleAndName(Waiting waiting);

    Long findWaitingOrder(Waiting waiting);

    Waiting save(Waiting waiting);

    void delete(Waiting waiting);
}
