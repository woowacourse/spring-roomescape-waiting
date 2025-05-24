package roomescape.reservation.waiting.infrastructure;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.waiting.domain.WaitingReservation;
import roomescape.reservation.waiting.domain.dto.WaitingReservationWithRank;

public interface JpaWaitingReservationRepository extends CrudRepository<WaitingReservation, Long> {

    @Transactional
    void deleteByIdAndMemberId(Long id, Long memberId);

    boolean existsByIdAndMemberId(Long id, Long memberId);

    boolean existsByThemeIdAndTimeIdAndDateAndMemberId(Long themeId, Long timeId, LocalDate date, Long memberId);

    List<WaitingReservation> findAll();

    @Query("""
        SELECT new roomescape.reservation.waiting.domain.dto.WaitingReservationWithRank(
            w,
            (SELECT COUNT(w2)
                FROM WaitingReservation w2
                WHERE w2.theme = w.theme
                    AND w2.date = w.date
                    AND w2.time = w.time
                    AND w2.createdAt < w.createdAt))
        FROM WaitingReservation w
        WHERE w.member.id = :memberId
        """)
    List<WaitingReservationWithRank> findWaitingsWithRankByMember_Id(Long memberId);
}
