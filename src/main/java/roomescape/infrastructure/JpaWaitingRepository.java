package roomescape.infrastructure;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.domain.Waiting;

@Repository
public interface JpaWaitingRepository extends JpaRepository<Waiting, Long> {

    List<Waiting> findByMemberId(Long id);
}
