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

    Long countAllBySchedule_DateAndSchedule_TimeAndSchedule_ThemeAndIdLessThanEqual(LocalDate date, ReservationTime time, Theme theme, Long id);

    boolean existsByMemberAndSchedule_DateAndSchedule_TimeAndSchedule_Theme(Member member, LocalDate date, ReservationTime time, Theme theme);

    Optional<Waiting> findFirstBySchedule_DateAndSchedule_TimeAndSchedule_Theme(LocalDate date, ReservationTime time, Theme theme);

    boolean existsBySchedule_DateAndSchedule_TimeAndSchedule_Theme(LocalDate date, ReservationTime time, Theme theme);
}
