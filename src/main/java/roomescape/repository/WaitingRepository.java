package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.Member;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    List<Waiting> findAllByMember(Member member);

    Long countAllByDateAndTimeAndThemeAndIdLessThanEqual(LocalDate date, ReservationTime time, Theme theme, Long id);

    boolean existsByMemberAndTimeAndDateAndTheme(Member member, ReservationTime time, LocalDate date, Theme theme);

    Optional<Waiting> findFirstByDateAndTimeAndTheme(LocalDate date, ReservationTime time, Theme theme);

    boolean existsByDateAndTimeAndTheme(LocalDate date, ReservationTime time, Theme theme);
}
