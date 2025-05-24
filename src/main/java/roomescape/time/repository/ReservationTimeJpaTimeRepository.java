package roomescape.time.repository;

import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.domain.ReservationDate;
import roomescape.time.controller.response.AvailableReservationTimeResponse;
import roomescape.time.domain.ReservationTime;

public interface ReservationTimeJpaTimeRepository extends JpaRepository<ReservationTime, Long>,
        ReservationTimeRepository {

    boolean existsByStartAt(LocalTime startAt);

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @Query("""
            SELECT new roomescape.time.controller.response.AvailableReservationTimeResponse(
                rt.id,
                rt.startAt,
                CASE
                    WHEN EXISTS (
                        SELECT 1 FROM Reservation r
                        WHERE r.reservationTime = rt
                        AND r.reservationDate = :date
                        AND r.theme.id = :themeId
                    )
                    THEN true
                    ELSE false
                END
            )
            FROM ReservationTime rt
            """)
    List<AvailableReservationTimeResponse> findAllAvailableReservationTimes(
            @Param("date") ReservationDate date,
            @Param("themeId") Long themeId
    );
}
