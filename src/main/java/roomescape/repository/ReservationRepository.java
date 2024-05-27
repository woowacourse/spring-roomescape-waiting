package roomescape.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationWithWaitingOrder;
import roomescape.domain.Status;

public interface ReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

    List<Reservation> findByThemeId(Long themeId);

    @Query("""
            SELECT
                new roomescape.domain.ReservationWithWaitingOrder(
                r,
                CASE WHEN r.status = 'WAITING'
                        THEN (SELECT COUNT(*) + 1
                              FROM Reservation r2
                              WHERE r2.date = r.date
                                AND r2.time.id = r.time.id
                                AND r2.theme.id = r.theme.id
                                AND r2.status = r.status
                                AND r2.createdAt < r.createdAt)
                        ELSE null
                    END as waitingOrder
                )
            FROM Reservation r
            WHERE r.member.id = :memberId
            """)
    List<ReservationWithWaitingOrder> findMyReservations(Long memberId);

    @Query("""
            SELECT r
            FROM Reservation r
            WHERE r.status = :status
            ORDER BY r.date, r.time.startAt, r.theme.name, r.id
            """)
    List<Reservation> findAllByStatus(Status status);
}
