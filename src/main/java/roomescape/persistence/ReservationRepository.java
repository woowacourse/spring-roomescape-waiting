package roomescape.persistence;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import roomescape.domain.Reservation;

import java.time.LocalDate;
import java.util.List;


public interface ReservationRepository extends ListCrudRepository<Reservation, Long> {

    boolean existsByTimeId(Long timeId);

    boolean existsByThemeId(Long themeId);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    List<Reservation> findByThemeIdAndDate(Long themeId, LocalDate date);

    @Query(value = """
            SELECT
                r.id as reservation_id,
                r.date,
                t.id as time_id,
                t.start_at as time_value,
                tm.id as theme_id,
                tm.name as theme_name,
                tm.description as theme_description,
                tm.thumbnail as theme_thumbnail,
                m.id as member_id,
                m.role as member_role,
                m.name as member_name,
                m.email as member_email,
                m.password as member_password
            FROM reservation as r
                inner join reservation_time as t on r.time_id = t.id
                inner join theme as tm on r.theme_id = tm.id
                inner join member as m on r.member_id = m.id
            WHERE (:memberId IS NULL OR m.id = :memberId)
              AND (:themeId IS NULL OR tm.id = :themeId)
              AND (:dateFrom IS NULL OR r.date >= :dateFrom)
              AND (:dateTo IS NULL OR r.date <= :dateTo)
            """, nativeQuery = true)
    List<Reservation> findReservationsInConditions(
            @Param("memberId") Long memberId,
            @Param("themeId") Long themeId,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo);
}
