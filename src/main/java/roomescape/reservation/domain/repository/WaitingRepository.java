package roomescape.reservation.domain.repository;

import roomescape.reservation.domain.Waiting;

public interface WaitingRepository {
    Waiting save(Waiting waiting);

    Long getRank(Waiting waiting);
}
