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

    List<Reservation> findByMemberId(Long memberId);

    @Query("""
        SELECT r
        FROM Reservation r
        JOIN r.time t
        JOIN r.theme tm
        JOIN r.member m
        WHERE (:memberId IS NULL OR r.member.id = :memberId)
        AND (:themeId IS NULL OR r.theme.id = :themeId)
        AND (:dateFrom IS NULL OR r.date >= :dateFrom)
        AND (:dateTo IS NULL OR r.date <= :dateTo)
        ORDER BY r.id
        """)
    List<Reservation> findReservationsInConditions(
            @Param("memberId") Long memberId,
            @Param("themeId") Long themeId,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo);
}
