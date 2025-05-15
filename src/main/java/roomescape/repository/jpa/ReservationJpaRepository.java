package roomescape.repository.jpa;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationV2;

@Repository
public interface ReservationJpaRepository extends JpaRepository<ReservationV2, Long> {
    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId);

    List<ReservationV2> findByMemberId(Long memberId);
}
