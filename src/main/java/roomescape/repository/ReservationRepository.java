package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @EntityGraph(attributePaths = {"member", "time", "theme"})
    List<Reservation> findAll();

    List<Reservation> findAllByDateAndTheme(LocalDate date, Theme theme);

    boolean existsByTimeAndDateAndTheme(ReservationTime time, LocalDate date, Theme theme);

    List<Reservation> findAllByThemeAndMemberAndDateBetween(Theme theme, Member member, LocalDate from, LocalDate to);

    @EntityGraph(attributePaths = {"time", "theme"})
    List<Reservation> findAllByMember(Member member);
}
