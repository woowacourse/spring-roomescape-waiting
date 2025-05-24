package roomescape.repository.jpa;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.entity.Reservation;

public interface JpaReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByMemberId(Long memberId);

    List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId);

    @Query("""
            SELECT r
            FROM Reservation r
            WHERE (:themeId IS NULL OR r.theme.id = :themeId)
              AND (:memberId IS NULL OR r.member.id = :memberId)
              AND (:dateFrom IS NULL OR r.date >= :dateFrom)
              AND (:dateTo IS NULL OR r.date <= :dateTo)
        """)
    List<Reservation> findByFilters(
        @Param("themeId") Long themeId,
        @Param("memberId") Long memberId,
        @Param("dateFrom") LocalDate dateFrom,
        @Param("dateTo") LocalDate dateTo);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    boolean existsByDateAndTimeIdAndThemeIdAndMemberId(LocalDate date, Long timeId, Long themeId,
        Long memberId);
}
