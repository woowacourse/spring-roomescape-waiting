package roomescape.infrastructure.jpa.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.business.model.entity.Reservation;
import roomescape.business.model.entity.ReservationSlot;
import roomescape.business.model.entity.User;
import roomescape.business.model.vo.Id;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface JpaReservationDao extends JpaRepository<Reservation, Id> {

    @Query("""
            SELECT DISTINCT r
            FROM Reservation r
            JOIN FETCH r.slot s
            JOIN FETCH r.slot.time rt
            JOIN FETCH r.slot.theme t
            JOIN FETCH r.user u
            WHERE (:themeId  IS NULL OR t.id = :themeId)
            AND (:userId   IS NULL OR u.id = :userId)
            AND (:dateFrom IS NULL OR r.slot.date.value >= :dateFrom)
            AND (:dateTo   IS NULL OR r.slot.date.value <= :dateTo)
            """)
    List<Reservation> findAllWithFilter(
            @Param("themeId") Id themeId,
            @Param("userId") Id userId,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo
    );

    List<Reservation> findAllByUserId(Id userId);

    boolean existsBySlotDateValueAndSlotTimeStartTimeValueAndSlotThemeId(LocalDate date, LocalTime time, Id themeId);

    boolean existsBySlotTimeId(Id timeId);

    boolean existsBySlotThemeId(Id themeId);

    boolean existsBySlotAndUser(ReservationSlot slot, User user);
}
