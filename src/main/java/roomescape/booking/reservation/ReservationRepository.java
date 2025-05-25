package roomescape.booking.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.booking.schedule.Schedule;
import roomescape.reservationtime.ReservationTime;
import roomescape.theme.Theme;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findAllBySchedule_ThemeAndSchedule_Date(Theme theme, LocalDate date);

    List<Reservation> findAllByMember_IdAndSchedule_Theme_IdAndSchedule_DateBetween(Long memberId, Long themeId, LocalDate from, LocalDate to);

    Boolean existsBySchedule_ReservationTime(ReservationTime reservationTime);

    Boolean existsBySchedule_Theme(Theme theme);

    Boolean existsBySchedule(Schedule schedule);

    List<Reservation> findAllByMember_Email(String email);
}
