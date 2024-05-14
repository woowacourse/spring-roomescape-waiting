package roomescape.reservation.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.member.domain.Member;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByDateAndTimeAndTheme(LocalDate date, ReservationTime time, Theme theme);

    List<Reservation> findAllByMemberAndThemeAndDateBetween(Member member, Theme theme, LocalDate fromDate, LocalDate toDate);

    int countByTime(ReservationTime time);

    @Query("SELECT r.time.id FROM Reservation r WHERE r.date = :date AND r.theme = :theme")
    List<Long> findAllTimeIdsByDateAndThemeId(@Param(value = "date") LocalDate date, @Param(value = "theme")Theme theme);
}
