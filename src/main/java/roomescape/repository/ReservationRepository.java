package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.controller.reservation.dto.ReservationSearchCondition;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.service.exception.ReservationNotFoundException;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findAllByDateAndThemeId(LocalDate date, long themeId);

    default Reservation fetchById(long id) {
        return findById(id).orElseThrow(() -> new ReservationNotFoundException("존재하지 않는 예약입니다."));
    }

    boolean existsByTimeId(long timeId);

    boolean existsByThemeIdAndTimeIdAndDate(long themeId, long timeId, LocalDate date);

    boolean existsByThemeId(long themeId);

    @Query("""
            SELECT r.theme
            FROM Reservation r
            WHERE r.date BETWEEN :dateFrom AND :dateTo
            GROUP BY r.theme
            ORDER BY COUNT(r) DESC
            LIMIT :limit""")
    List<Theme> findPopularThemes(
            @Param("dateFrom") LocalDate from,
            @Param("dateTo") LocalDate until,
            @Param("limit") int limit);

    List<Reservation> findAllByThemeIdAndMemberIdAndDateBetween(long themeId, long memberId,
                                                                LocalDate from, LocalDate until);

    default List<Reservation> searchReservations(ReservationSearchCondition condition) {
        return findAllByThemeIdAndMemberIdAndDateBetween(condition.themeId(), condition.memberId(),
                condition.dateFrom(), condition.dateTo());
    }

    @EntityGraph(attributePaths = {"theme", "time"})
    List<Reservation> findAllByMemberId(Long id);
}
