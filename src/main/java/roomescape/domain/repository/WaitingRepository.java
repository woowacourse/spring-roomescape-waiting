package roomescape.domain.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.entity.Waiting;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    boolean existsByGameScheduleIdAndMemberId(Long gameScheduleId, Long memberId);

    List<Waiting> findByMemberId(Long memberId);
}
