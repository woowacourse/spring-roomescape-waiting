package roomescape.reservation.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.infrastructure.projection.TimeValueProjection;
import roomescape.timeslot.domain.ReservationTime;
import roomescape.user.domain.UserId;

import java.time.LocalDateTime;
import java.util.List;

public interface JpaReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByDateAndTimeAndThemeId(ReservationDate date, ReservationTime time, Long ThemeId);

    List<Reservation> findAllByUserId(UserId userId);

    List<Reservation> findAllByDateAndThemeId(ReservationDate date, Long themeId);

    List<TimeValueProjection> findTimeByDateAndThemeId(ReservationDate date, Long themeId);

    @Query("""
    select r
      from Reservation r
     where r.date = :#{#slot.date}
       and r.time = :#{#slot.time}
       and r.theme.id = :#{#slot.themeId}
       and r.createdAt <= #{createdAt}
    """)
    List<Reservation> findAllBySlotAndCreatedAtJpql(ReservationSlot slot, LocalDateTime createdAt);
}

