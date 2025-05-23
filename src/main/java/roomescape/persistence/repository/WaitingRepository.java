package roomescape.persistence.repository;

import roomescape.model.Waiting;

public interface WaitingRepository {

    Waiting save(Waiting waiting);

    Waiting findById(Long id);

    void delete(Waiting waiting);
}
