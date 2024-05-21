package roomescape.repository;

import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.Waiting;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {
    boolean existsByDateAndTimeIdAndMemberId(LocalDate date, Long timeId, Long memberId);
}
