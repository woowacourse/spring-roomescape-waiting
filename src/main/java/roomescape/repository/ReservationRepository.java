package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.entity.Member;
import roomescape.entity.Reservation;
import roomescape.entity.ReservationTime;
import roomescape.entity.Theme;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @EntityGraph(attributePaths = {"member", "time", "theme"})
    List<Reservation> findAll();

    List<Reservation> findAllByDateAndTheme(LocalDate date, Theme theme);

    Optional<Reservation> findByDateAndTimeAndTheme(LocalDate date, ReservationTime reservationTime, Theme theme);

    boolean existsByDateAndTime(LocalDate date, ReservationTime reservationTime);

    List<Reservation> findAllByMemberAndThemeAndDateBetween(Member member, Theme theme, LocalDate from, LocalDate to);

    @EntityGraph(attributePaths = {"time", "theme"})
    List<Reservation> findAllByMember(Member member);

    boolean existsByMemberAndTimeAndDate(Member member, ReservationTime reservationTime, LocalDate date);
}
