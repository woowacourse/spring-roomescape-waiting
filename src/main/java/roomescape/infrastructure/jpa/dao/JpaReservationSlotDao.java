package roomescape.infrastructure.jpa.dao;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.business.model.entity.ReservationSlot;
import roomescape.business.model.vo.Id;
import roomescape.business.model.vo.ReservationDate;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface JpaReservationSlotDao extends JpaRepository<ReservationSlot, Id> {
    
    @Query("""
            SELECT DISTINCT rs
            FROM ReservationSlot rs
            JOIN FETCH rs.reservations r
            JOIN FETCH r.user
            JOIN FETCH rs.theme
            JOIN FETCH rs.time
            WHERE rs IN (
                SELECT rs2
                FROM ReservationSlot rs2
                JOIN rs2.reservations r2
                JOIN r2.user u
                WHERE (:themeId  IS NULL OR rs2.theme.id = :themeId)
                AND (:userId   IS NULL OR u.id = :userId)
                AND (:dateFrom IS NULL OR r.slot.date.value >= :dateFrom)
                AND (:dateTo   IS NULL OR r.slot.date.value <= :dateTo)
            )
            """)
    List<ReservationSlot> findAllBy(Id themeId, Id userId, LocalDate dateFrom, LocalDate dateTo);

    @EntityGraph(attributePaths = {"reservations", "reservations.user", "theme", "time"})
    List<ReservationSlot> findAll();

    Optional<ReservationSlot> findByDateAndTimeIdAndThemeId(ReservationDate date, Id timeId, Id themeId);
}
