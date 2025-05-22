package roomescape.infrastructure.jpa.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.business.model.entity.ReservationSlot;
import roomescape.business.model.vo.Id;
import roomescape.business.model.vo.ReservationDate;

import java.util.List;
import java.util.Optional;

public interface JpaReservationSlotDao extends JpaRepository<ReservationSlot, Id> {

    @Query("""
            select distinct rs
            from ReservationSlot rs
            join fetch rs.reservations r
            join fetch r.user
            where rs in (
                select rs2
                from ReservationSlot rs2
                join rs2.reservations r2
                join r2.user u
                where u.id = :userId
            )
            """)
    List<ReservationSlot> findAllSlotsContainsReserverOf(Id userId);

    Optional<ReservationSlot> findByDateAndTimeIdAndThemeId(ReservationDate date, Id timeId, Id themeId);
}
