package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.dto.response.AvailableReservationTimeResponse;

public interface JpaReservationRepository extends ListCrudRepository<Reservation, Long>, ReservationRepository {

    @EntityGraph(
            type = EntityGraph.EntityGraphType.FETCH,
            attributePaths = {"info.time", "info.theme", "member"}
    )
    List<Reservation> findByInfoThemeIdAndMemberIdAndInfoDateBetween(
            Long themeId,
            Long memberId,
            LocalDate startDate,
            LocalDate endDate
    );

    @Query("""
            SELECT EXISTS (
              SELECT 1
              FROM Reservation r
              WHERE r.info.time.id = :timeId)
            """)
    boolean existsByTimeId(@Param("timeId")Long timeId);

    @Query("""
            SELECT EXISTS (
                SELECT 1
                FROM Reservation r
                WHERE r.info.theme.id = :themeId
            )
            """)
    boolean existsByThemeId(@Param("themeId") Long themeId);

    @Query("""
            SELECT EXISTS (
                SELECT 1
                FROM Reservation r
                WHERE (r.info.date, r.info.time.id, r.info.theme.id) = (:date, :timeId, :themeId)
            )
            """)
    boolean existsByDateAndTimeIdAndThemeId(
            @Param("date") LocalDate date,
            @Param("timeId") Long timeId,
            @Param("themeId") Long themeId
    );

    @Query("""
            SELECT new roomescape.reservationtime.dto.response.AvailableReservationTimeResponse(
                rt.id,
                rt.startAt,
                CASE WHEN r.id IS NOT NULL THEN TRUE ELSE FALSE END AS already_booked
            )
            FROM ReservationTime AS rt
            LEFT JOIN Reservation r
              ON rt.id = r.info.time.id
             AND r.info.date = :date
             AND r.info.theme.id = :themeId
            ORDER BY rt.startAt
            """)
    List<AvailableReservationTimeResponse> findBookedTimesByDateAndThemeId(
            @Param("date") LocalDate date,
            @Param("themeId") Long themeId
    );

    @EntityGraph(attributePaths = "member")
    List<Reservation> findByMemberId(Long memberId);
}
