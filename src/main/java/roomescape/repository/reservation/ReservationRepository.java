package roomescape.repository.reservation;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

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

    @Modifying
    @Query("UPDATE ReservationStatus s " +
            "SET s.priority = s.priority - 1 " +
            "WHERE s.id IN (" +
            "   SELECT r.status.id FROM Reservation r " +
            "   WHERE r.date = :date " +
            "   AND r.time = :time " +
            "   AND r.theme = :theme " +
            "   AND s.priority > :priority" +
            ")")
    void updateAllWaitingReservationsAfterPriority(LocalDate date, ReservationTime time, Theme theme,
                                                   Long priority);
}
