package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.Member;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    List<Waiting> findAllByMember(Member member);

    Long countAllByDateAndTimeAndThemeAndIdLessThanEqual(LocalDate date, ReservationTime time, Theme theme, Long id);

    boolean existsByTimeAndDateAndTheme(ReservationTime time, LocalDate date, Theme theme);
}
