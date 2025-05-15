package roomescape.reservation.dao.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import roomescape.reservation.model.Reservation;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface JpaReservationDao extends JpaRepository<Reservation, Long> {

    List<Reservation> findByMemberId(Long memberId);

    List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId);

    @Query("""
        SELECT r FROM Reservation r
        WHERE (:memberId IS NULL OR r.member.id = :memberId)
          AND (:themeId IS NULL OR r.theme.id = :themeId)
          AND (:startDate IS NULL OR r.date >= :startDate)
          AND (:endDate IS NULL OR r.date <= :endDate)
    """)
    List<Reservation> findByMemberIdAndThemeIdAndDateBetween(
            @Param("memberId") Long memberId,
            @Param("themeId") Long themeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    int countById(Long id);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);
}
