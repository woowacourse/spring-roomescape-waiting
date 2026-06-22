package roomescape.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.Reservation;
import roomescape.dto.ReservationResponse;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    @Query("""
            SELECT new roomescape.dto.ReservationResponse(
                r.id,
                r.name,
                CAST(r.status AS string),
                rs.date,
                theme.name,
                theme.description,
                theme.thumbnailUrl,
                time.startAt,
                CAST(
                    CASE
                        WHEN r.status <> roomescape.domain.Status.WAITING THEN 0L
                        ELSE (
                            SELECT COUNT(r2)
                            FROM Reservation r2
                            WHERE r2.reservationSlot = r.reservationSlot
                              AND r2.status = roomescape.domain.Status.WAITING
                              AND r2.updateAt < r.updateAt
                        ) + 1L
                    END
                AS integer)
            )
            FROM Reservation r
            JOIN r.reservationSlot rs
            JOIN rs.time time
            JOIN rs.theme theme
            WHERE r.name = :username
            """)
    List<ReservationResponse> findByUserName(@Param("username") String username);

    @Query("""
            SELECT r,
                   (
                       SELECT COUNT(r2)
                       FROM Reservation r2
                       WHERE r2.reservationSlot.id = :slotId
                         AND r2.status = roomescape.domain.Status.WAITING
                         AND r2.updateAt < r.updateAt
                   )
            FROM Reservation r
            WHERE r.name = :userName
              AND r.reservationSlot.id = :slotId
              AND r.status = roomescape.domain.Status.WAITING
            """)
    List<Object[]> findWaitingWithRankByUserNameAndSlotId(
            @Param("userName") String userName,
            @Param("slotId") Long slotId
    );
}
