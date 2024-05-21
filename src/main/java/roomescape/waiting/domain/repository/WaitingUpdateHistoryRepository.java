package roomescape.waiting.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.waiting.domain.WaitingUpdateHistory;

public interface WaitingUpdateHistoryRepository extends JpaRepository<WaitingUpdateHistory, Long> {
}
