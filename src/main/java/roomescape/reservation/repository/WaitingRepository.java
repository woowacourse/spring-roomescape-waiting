package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.WaitingWithRank;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    @Query("""
            SELECT w2
            FROM Waiting w2
            JOIN FETCH w2.theme
            JOIN FETCH w2.member
            JOIN FETCH w2.time
            """)
    List<Waiting> findAll();

    boolean existsByDateAndTimeIdAndThemeIdAndMemberId(
            LocalDate date,
            Long timeId,
            Long themeId,
            Long memberId
    );

    @Query("""
            SELECT new roomescape.reservation.domain.WaitingWithRank(
                w,
                (SELECT COUNT(w2)
                 FROM Waiting w2
                 WHERE w2.theme = w.theme
                   AND w2.date = w.date
                   AND w2.time = w.time
                   AND w2.id < w.id)+1)
            FROM Waiting w
            WHERE w.member.id = :memberId
            """)
    List<WaitingWithRank> findAllWaitingWithRankByMemberId(Long memberId);

    Optional<Waiting> findFirstByDateAndTimeIdAndThemeIdOrderByIdAsc(LocalDate date, Long themeId, Long timeId);
}
