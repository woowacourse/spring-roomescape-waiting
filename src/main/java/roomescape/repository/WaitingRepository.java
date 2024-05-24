package roomescape.repository;

import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.model.Waiting;

@Repository
public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    boolean existsByDateAndTimeIdAndThemeIdAndMemberId(LocalDate date, long timeId, long themeId, long memberId);
}
