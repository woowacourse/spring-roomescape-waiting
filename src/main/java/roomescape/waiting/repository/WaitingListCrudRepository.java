package roomescape.waiting.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import roomescape.member.domain.Member;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingWithRank;

public interface WaitingListCrudRepository extends ListCrudRepository<Waiting, Long> {

    @Query("SELECT new roomescape.waiting.domain.WaitingWithRank(" +
            "    w, " +
            "    (SELECT COUNT(w2) " +
            "     FROM Waiting w2 " +
            "     WHERE w2.theme = w.theme " +
            "       AND w2.date = w.date " +
            "       AND w2.time = w.time " +
            "       AND w2.id < w.id)) " +
            "FROM Waiting w " +
            "WHERE w.member.id = :memberId")
    List<WaitingWithRank> findWaitingWithRankByMemberId(Long memberId);

    Long countByCreatedAtBeforeAndDateAndThemeAndTime(LocalDateTime createAt,
                                                      LocalDate date,
                                                      Theme theme,
                                                      ReservationTime time);

    boolean existsByMemberAndDateAndTime(Member member, LocalDate date, ReservationTime time);
}
