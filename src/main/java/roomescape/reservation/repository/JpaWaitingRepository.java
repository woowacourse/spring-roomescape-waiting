package roomescape.reservation.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.repository.dto.WaitingWithRankDto;

public interface JpaWaitingRepository extends JpaRepository<Waiting, Long> {

    boolean existsByDateAndTimeIdAndThemeId(ReservationDate date, Long timeId, Long themeId);

    boolean existsByDateAndTimeIdAndThemeIdAndMemberId(ReservationDate reservationDate, Long timeId, Long themeId,
                                                       Long memberId);

    Optional<Waiting> findFirstByDateAndTimeIdAndThemeIdOrderByCreatedAtAsc(
            ReservationDate date,
            Long timeId,
            Long themeId
    );

    @Query(value = """
            WITH RankedWaitings AS (
                SELECT 
                    w.*,
                    ROW_NUMBER() OVER (
                        PARTITION BY w.reservation_date, w.time_id, w.theme_id 
                        ORDER BY w.created_at
                    ) AS rank
                FROM waiting w
            )
            SELECT 
                rw.id AS id,
                t.name AS themeName,
                rw.reservation_date AS date,
                rt.start_at AS time,
                rw.rank AS rank
            FROM RankedWaitings rw
            LEFT JOIN theme t ON rw.theme_id = t.id
            LEFT JOIN reservation_time rt ON rw.time_id = rt.id
            WHERE rw.member_id = :memberId
            """, nativeQuery = true)
    List<WaitingWithRankDto> findWithRankByMemberId(@Param("memberId") Long memberId);
}
