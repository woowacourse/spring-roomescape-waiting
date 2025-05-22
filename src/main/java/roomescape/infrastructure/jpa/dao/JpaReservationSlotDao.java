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
            select rs
            from ReservationSlot rs
            join fetch rs.reservations r
            join fetch r.user u
            where u.id = :userId
            """)
    List<ReservationSlot> findAllSlotsContainsReserverOf(Id userId);

    Optional<ReservationSlot> findByDateAndTimeIdAndThemeId(ReservationDate date, Id timeId, Id themeId);
}
