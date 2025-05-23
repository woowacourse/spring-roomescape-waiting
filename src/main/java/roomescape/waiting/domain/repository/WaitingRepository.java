package roomescape.waiting.domain.repository;

import roomescape.waiting.domain.Waiting;

public interface WaitingRepository {

    Waiting save(Waiting waiting);
}
