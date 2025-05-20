package roomescape.repository.waiting;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.domain.Member;
import roomescape.domain.Waiting;

@Repository
public interface WaitingRepsitory extends JpaRepository<Waiting, Long> {

    List<Waiting> findAllByMember(Member member);
}
