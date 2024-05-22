package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findAllByDateAndTheme(LocalDate date, Theme theme);

    boolean existsByDateAndTimeAndTheme(LocalDate date, ReservationTime time, Theme theme);

    boolean existsByDateAndThemeAndMember(LocalDate date, Theme theme, Member member);

    List<Reservation> findAllByThemeAndMemberAndDateBetween(Theme theme, Member member, LocalDate from, LocalDate to);

    List<Reservation> findAllByMember(Member member);
}
