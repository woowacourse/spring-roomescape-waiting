package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findAllByMember(Member member);

    List<Reservation> findAllByMemberAndThemeAndDateBetween(Member member, Theme theme, LocalDate from, LocalDate to);

    List<Reservation> findAllByDateAndTheme(LocalDate date, Theme theme);

    boolean existsByDateAndTimeAndTheme(LocalDate date, TimeSlot timeSlot, Theme theme);

    boolean existsByTheme(Theme theme);

    boolean existsByTime(TimeSlot timeSlot);
}
