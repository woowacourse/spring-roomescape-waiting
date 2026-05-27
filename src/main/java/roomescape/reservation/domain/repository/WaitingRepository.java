package roomescape.reservation.domain.repository;

import roomescape.reservation.domain.Waiting;

public interface WaitingRepository {
    WaitingDetail save(Waiting waiting);
}
