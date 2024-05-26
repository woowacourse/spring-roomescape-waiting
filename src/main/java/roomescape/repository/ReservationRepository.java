package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findAllBySchedule_DateAndSchedule_Theme(LocalDate date, Theme theme);

    boolean existsBySchedule_DateAndSchedule_TimeAndSchedule_Theme(LocalDate date, ReservationTime time, Theme theme);

    boolean existsBySchedule_DateAndSchedule_ThemeAndMember(LocalDate date, Theme theme, Member member);

    boolean existsBySchedule_Theme(Theme foundTheme);

    boolean existsBySchedule_Time(ReservationTime foundReservationTime);

    List<Reservation> findAllBySchedule_ThemeAndMemberAndSchedule_DateBetween(Theme theme, Member member,
                                                                              LocalDate from, LocalDate to);

    List<Reservation> findAllByMember(Member member);

}
