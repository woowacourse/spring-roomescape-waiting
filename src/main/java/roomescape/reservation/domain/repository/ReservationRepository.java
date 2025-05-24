package roomescape.reservation.domain.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByTimeId(Long timeId);

    boolean existsByDateAndTimeAndTheme(LocalDate date, ReservationTime time, Theme theme);

    List<Reservation> findAllByDateAndThemeId(LocalDate date, Long themeId);

    @Query(value = "SELECT r.theme FROM Reservation r WHERE r.date BETWEEN :startDate AND :endDate GROUP BY r.theme.id ORDER BY COUNT(r) DESC")
    List<Theme> findTopThemesByReservationCountBetween(LocalDate startDate, LocalDate endDate);

    List<Reservation> findAllByThemeIdAndMemberIdAndDateBetween(Long themeId, Long memberId, LocalDate dateFrom,
                                                                LocalDate dateTo);

    List<Reservation> findAllByMember(Member member);

    long countByDateAndTimeAndTheme(LocalDate date, ReservationTime time, Theme theme);

    List<Reservation> findAllByStatus(ReservationStatus reservationStatus);

    List<Reservation> findAllByDateAndThemeAndTime(LocalDate date, Theme theme, ReservationTime time);

    boolean existsByTheme(Theme theme);
}
