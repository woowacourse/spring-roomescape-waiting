package roomescape.reservation.infrastructure;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.TimeSlot;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.dto.response.WaitingWithRankResponse;

public interface WaitingRepository extends Repository<Waiting, Long> {

    Waiting save(Waiting waiting);

    Optional<Waiting> findById(Long id);

    boolean existsByDateAndMemberAndThemeAndTimeSlot(LocalDate date, Member member, Theme theme, TimeSlot timeSlot);

    @Query("""
            SELECT new roomescape.reservation.dto.response.WaitingWithRankResponse(
            w,
            (SELECT COUNT(*) + 1
            FROM Waiting w2
            WHERE w2.theme = w.theme
            AND w2.date = w.date
            AND w2.timeSlot = w.timeSlot
            AND w2.id < w.id))
            FROM Waiting w
            WHERE w.member.id = :memberId
            """)
    List<WaitingWithRankResponse> findByMemberIdWithRank(Long memberId);

    void delete(Waiting waiting);

    Optional<Waiting> findFirstByDateAndTimeSlotAndThemeOrderById(LocalDate date, TimeSlot timeSlot, Theme theme);

    List<Waiting> findAll();
}
