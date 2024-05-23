package roomescape.domain.reservation;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import roomescape.domain.exception.DomainNotFoundException;
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
            ON w2.theme = w.theme AND w2.time = w.time AND w2.date = w.date
            JOIN FETCH w.member
            JOIN FETCH w.time
            JOIN FETCH w.theme
            WHERE w.member.id = :memberId AND w2.id <= w.id
            GROUP BY w.id
            """)
    List<WaitingWithRankDto> findWaitingsWithRankByMemberId(Long memberId);

    boolean existsByDateAndTimeIdAndThemeIdAndMemberId(LocalDate date, long timeId, long themeId, long memberId);

    default Waiting getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new DomainNotFoundException(String.format("해당 id의 예약 대기가 존재하지 않습니다. (id: %d)", id)));
    }
}
