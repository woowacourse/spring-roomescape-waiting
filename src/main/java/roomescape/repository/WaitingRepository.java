package roomescape.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.waiting.Waiting;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {
    List<Waiting> findAllByMemberId(Long id);
}

