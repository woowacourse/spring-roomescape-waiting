package roomescape.wating.repository;

import roomescape.wating.domain.Waiting;

public interface WaitingRepository {

    Waiting save(Waiting waiting);
}
