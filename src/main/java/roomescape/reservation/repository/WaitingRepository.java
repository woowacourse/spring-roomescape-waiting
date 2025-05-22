package roomescape.reservation.repository;

import roomescape.reservation.domain.Waiting;

public interface WaitingRepository {
    Waiting save(Waiting waiting);
}
