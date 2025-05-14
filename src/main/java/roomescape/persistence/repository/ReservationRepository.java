package roomescape.persistence.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.business.domain.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    @Query("""
        SELECT r FROM Reservation r
        WHERE (:memberId IS NULL OR r.member.id = :memberId)
            AND (:themeId IS NULL OR r.theme.id = :themeId)
            AND (:startDate IS NULL OR r.date >= :startDate)
            AND (:endDate IS NULL OR r.date <= :endDate)
    """)
    List<Reservation> findAllByFilter(@Param("memberId") Long memberId,
                                      @Param("themeId") Long themeId,
                                      @Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate);

    List<Reservation> findByDateBetween(LocalDate startDate, LocalDate endDate);

    List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId);

    boolean existsByDateAndReservationTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    List<Reservation> findByMemberId(Long memberId);
}
