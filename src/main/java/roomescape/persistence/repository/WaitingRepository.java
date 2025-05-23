package roomescape.persistence.repository;

import java.util.List;
import roomescape.model.Waiting;

public interface WaitingRepository {

    Waiting save(Waiting waiting);

    Waiting findById(Long id);

    void delete(Waiting waiting);

    List<Waiting> findForMember(Long id);

    int countWaitingBefore(Waiting waiting);
}
