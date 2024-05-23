package roomescape.reservation.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.member.domain.Member;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByDateAndTimeAndTheme(LocalDate date, ReservationTime time, Theme theme);

    List<Reservation> findAllByMemberAndThemeAndDateBetween(Member member, Theme theme, LocalDate fromDate, LocalDate toDate);

    int countByTime(ReservationTime time);

    List<Reservation> findAllByDateAndTheme(LocalDate date, Theme theme);

    List<Reservation> findAllByMember(Member member);

    boolean existsByMemberAndDateAndTimeAndTheme(Member member, LocalDate date, ReservationTime time, Theme theme);
}
