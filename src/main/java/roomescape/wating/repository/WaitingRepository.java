package roomescape.wating.repository;

import roomescape.wating.domain.Waiting;

public interface WaitingRepository {

    Long save(Waiting waiting);

    boolean deleteById(long waitingId);
}
