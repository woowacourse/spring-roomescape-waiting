package roomescape.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.Reservation;
import roomescape.persistence.dto.MemberBookingProjection;

public interface JpaBookingQueryRepository extends JpaRepository<Reservation, Long> {

    //TODO: 하드코딩 문제를 어떻게 해결할 수 있을까, 식별자(0,1)로 구분해야할까?
    @Query(value = """
        SELECT
            r.id AS id,
            m.name AS memberName,
            'RESERVED' AS type,
            t.name AS themeName,
            r.date AS date,
            rt.start_at AS time,
            0 AS rank
        FROM reservation r
        JOIN member m ON r.member_id = m.id
        JOIN theme t ON r.theme_id = t.id
        JOIN reservation_time rt ON r.time_id = rt.id
        WHERE r.member_id = :memberId

        UNION ALL

        SELECT
            w.id AS id,
            m.name AS memberName,
            'WAITED' AS type,
            t.name AS themeName,
            w.date AS date,
            rt.start_at AS time,
            (
                SELECT COUNT(*) FROM waiting w2
                WHERE w2.date = w.date
                  AND w2.time_id = w.time_id
                  AND w2.theme_id = w.theme_id
                  AND (
                        w2.waiting_started_at < w.waiting_started_at OR
                        (w2.waiting_started_at = w.waiting_started_at AND w2.id < w.id)
                      )
            ) + 1 AS rank
        FROM waiting w
        JOIN member m ON w.member_id = m.id
        JOIN theme t ON w.theme_id = t.id
        JOIN reservation_time rt ON w.time_id = rt.id
        WHERE w.member_id = :memberId

        ORDER BY date, time
        """, nativeQuery = true)
    List<MemberBookingProjection> findAllBookingsByMemberId(@Param("memberId") Long memberId);
}
