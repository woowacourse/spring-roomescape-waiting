package roomescape.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import roomescape.model.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WaitingRepository extends CrudRepository<Waiting, Long> {

    Waiting save(Waiting waiting);

    void deleteById(long id);

    boolean existsById(long id);

    @Query("""
            SELECT new roomescape.model.WaitingWithRank(
            w, COUNT(w2))
            FROM Waiting w
            INNER JOIN Waiting w2
            ON w2.theme = w.theme
            AND w2.date = w.date
            AND w2.time = w.time
            AND w2.id <= w.id
            WHERE w.member.id = :memberId
            GROUP BY w.id
            """)
    List<WaitingWithRank> findWaitingWithRankByMemberId(Long memberId);

    boolean existsWaitingByThemeAndDateAndTimeAndMember(Theme theme, LocalDate date, ReservationTime time, Member member);

    List<Waiting> findAll();

    boolean existsWaitingByThemeAndDateAndTime(Theme theme, LocalDate date, ReservationTime time);

    Optional<Waiting> findFirstByThemeAndDateAndTime(Theme theme, LocalDate date, ReservationTime time);
}
