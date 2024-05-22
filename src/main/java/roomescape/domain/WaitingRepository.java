package roomescape.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {
    List<Waiting> findByMemberId(Long id);
}
