package roomescape.reservation.infrastructure;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.infrastructure.vo.ThemBookingCount;
import roomescape.user.domain.UserId;

import java.util.List;

public interface JpaReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

    boolean existsByTimeId(Long timeId);

    boolean existsByDateAndTimeIdAndThemeId(ReservationDate date, Long timeId, Long themeId);

    List<Reservation> findAllByUserId(UserId userId);

    List<Reservation> findAllByDateAndThemeId(ReservationDate date, Long themeId);

    @Query("""
            SELECT new roomescape.reservation.infrastructure.vo.ThemBookingCount(t, COUNT(r))
            FROM Reservation r
            JOIN r.theme t
            WHERE r.date BETWEEN :startDate AND :endDate
            GROUP BY t
            ORDER BY COUNT(r) DESC
            """)
    List<ThemBookingCount> findThemesWithBookedCount(
            @Param("startDate") ReservationDate startDate,
            @Param("endDate") ReservationDate endDate,
            Pageable pageable);
}

