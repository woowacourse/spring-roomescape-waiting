package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.dto.response.AvailableReservationTimeResponse;

public interface JpaReservationRepository extends ListCrudRepository<Reservation, Long> {

    @Query("""
            SELECT r
            FROM Reservation r
            JOIN FETCH r.info.time t
            JOIN FETCH r.info.theme th
            JOIN FETCH r.info.member m
            WHERE th.id        = :themeId
            AND m.id         = :memberId
            AND r.info.date BETWEEN :startDate AND :endDate
            """)
    List<Reservation> findFilteredReservations(Long themeId,
                                               Long memberId,
                                               LocalDate startDate,
                                               LocalDate endDate);

    @Query("""
            SELECT EXISTS (
              SELECT 1
              FROM Reservation r
              WHERE r.info.time.id = :timeId)
            """)
    boolean existsByTimeId(Long timeId);

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

    @Query("""
            SELECT r
            FROM Reservation r
            JOIN FETCH r.info.member m
            WHERE m.id = :memberId
            """)
    List<Reservation> findAllByMemberId(@Param("memberId") Long memberId);
}
