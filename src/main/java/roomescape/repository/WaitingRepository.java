package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.model.Waiting;

import java.util.List;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    List<Waiting> findByMemberId(long memberId);
}
