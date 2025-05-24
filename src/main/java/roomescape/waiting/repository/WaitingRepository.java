package roomescape.waiting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.repository.dto.WaitingInfoDataResponse;

import java.util.List;
import java.util.Optional;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {
    Optional<Waiting> findByIdAndMemberId(Long id, Long memberId);

    @Query("SELECT w FROM Waiting w JOIN FETCH w.time JOIN FETCH w.theme JOIN FETCH w.member")
    List<Waiting> findAll();

    @Query("""
    SELECT
        new roomescape.waiting.repository.dto.WaitingInfoDataResponse(
            w,
            (COUNT(w2) + 1)
        )
    FROM Waiting w
    LEFT JOIN Waiting w2
    ON w2.createdAt < w.createdAt AND w2.date = w.date AND w2.time.id = w.time.id AND w2.theme.id = w.theme.id
    WHERE w.member.id = :memberId
    GROUP BY w
    ORDER BY w.createdAt ASC
    """)
    List<WaitingInfoDataResponse> findAllWaitingInfoByMemberId(@Param("memberId") Long memberId);
}
