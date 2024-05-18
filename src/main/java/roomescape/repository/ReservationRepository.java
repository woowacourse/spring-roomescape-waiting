package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.controller.reservation.dto.ReservationSearchCondition;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.service.exception.ReservationNotFoundException;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByTimeId(long timeId);

    boolean existsByThemeIdAndTimeIdAndDate(long themeId, long timeId, LocalDate date);

    boolean existsByThemeId(long themeId);

    List<Reservation> findAllByThemeIdAndMemberIdAndDateBetween(long themeId, long memberId,
                                                                LocalDate from, LocalDate until);

    @EntityGraph(attributePaths = {"theme", "time"})
    List<Reservation> findAllByMemberId(Long id);

    @Query("""
            SELECT r.theme
            FROM Reservation r
            WHERE r.date BETWEEN :dateFrom AND :dateTo
            GROUP BY r.theme
            ORDER BY COUNT(r) DESC
            LIMIT :limit""")
    List<Theme> findMostBookedThemesBetweenLimited(
            @Param("dateFrom") LocalDate from,
            @Param("dateTo") LocalDate until,
            @Param("limit") int limit);

    @Query("""
            SELECT r.time
            FROM Reservation r
            WHERE r.date = :date
            AND r.theme.id = :themeId
            GROUP BY r.time
            """)
    Set<ReservationTime> findBookedTimes(@Param("date") LocalDate date,
                                         @Param("themeId") long themeId);

    default Reservation findByIdOrThrow(long id) {
        return findById(id).orElseThrow(() -> new ReservationNotFoundException("존재하지 않는 예약입니다."));
    }

    default List<Reservation> searchReservations(ReservationSearchCondition condition) {
        return findAllByThemeIdAndMemberIdAndDateBetween(condition.themeId(), condition.memberId(),
                condition.dateFrom(), condition.dateTo());
    }
}
