package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.entity.Member;
import roomescape.entity.Reservation;
import roomescape.entity.ReservationTime;
import roomescape.entity.Theme;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @EntityGraph(attributePaths = {"member", "schedule.time", "schedule.theme"})
    List<Reservation> findAll();

    List<Reservation> findAllBySchedule_DateAndSchedule_Theme(LocalDate date, Theme theme);

    boolean existsBySchedule_DateAndSchedule_Time(LocalDate date, ReservationTime reservationTime);

    List<Reservation> findAllByMemberAndSchedule_ThemeAndSchedule_DateBetween(Member member, Theme theme, LocalDate from, LocalDate to);

    @EntityGraph(attributePaths = {"schedule.time", "schedule.theme"})
    List<Reservation> findAllByMember(Member member);

    boolean existsByMemberAndSchedule_TimeAndSchedule_Date(Member member, ReservationTime reservationTime, LocalDate date);
}
