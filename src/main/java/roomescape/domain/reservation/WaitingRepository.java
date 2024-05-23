package roomescape.domain.reservation;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import roomescape.domain.exception.DomainNotFoundException;
import roomescape.domain.reservation.detail.ReservationDetail;
import roomescape.domain.reservation.dto.WaitingWithRankDto;

public interface WaitingRepository extends ListCrudRepository<Waiting, Long> {
    @Query("""
            SELECT
                new roomescape.domain.reservation.dto.WaitingWithRankDto(
                    w,
                    COUNT(*)
                )
            FROM Waiting w
            JOIN Waiting w2
            ON w2.detail = w.detail
            JOIN FETCH w.detail
            WHERE w.member.id = :memberId AND w2.id <= w.id
            GROUP BY w.id
            """)
    List<WaitingWithRankDto> findWaitingsWithRankByMemberId(Long memberId);

    boolean existsByDetail(ReservationDetail detail);

    boolean existsByDetailAndMemberId(ReservationDetail detail, long memberId);

    default Waiting getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new DomainNotFoundException(String.format("해당 id의 예약 대기가 존재하지 않습니다. (id: %d)", id)));
    }
}
