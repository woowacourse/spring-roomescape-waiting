package roomescape.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import roomescape.model.*;

import java.time.LocalDate;
import java.util.List;

public interface WaitingRepository extends CrudRepository<Waiting, Long> {

    Waiting save(Waiting waiting);

    void deleteById(long id);

    boolean existsById(long id);

    @Query("""
            SELECT new roomescape.model.WaitingWithRank(
            w, 
            (SELECT COUNT(w2) + 1 
                FROM Waiting w2 
                WHERE w2.theme = w.theme 
                AND w2.date = w.date 
                AND w2.time = w.time 
                AND w2.id < w.id)) 
            FROM Waiting w 
            WHERE w.member.id = :memberId
            """)
    List<WaitingWithRank> findWaitingWithRankByMemberId(Long memberId);

    boolean existsWaitingByThemeAndDateAndTimeAndMember(Theme theme, LocalDate date, ReservationTime time, Member member);
}
