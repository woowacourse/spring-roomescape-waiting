package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import roomescape.member.domain.MemberId;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.ReservationTimeId;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.ThemeId;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.WaitingId;
import roomescape.reservation.domain.WaitingWithRank;

public interface WaitingRepository extends JpaRepository<Waiting, WaitingId> {

    @Query("""
            SELECT w2
            FROM Waiting w2
            JOIN FETCH w2.theme
            JOIN FETCH w2.member
            JOIN FETCH w2.time
            """)
    List<Waiting> findAll();

    boolean existsByDateAndTimeIdAndThemeId(
            LocalDate date,
            ReservationTimeId timeId,
            ThemeId themeId
    );

    boolean existsByDateAndTimeIdAndThemeIdAndMemberId(
            LocalDate date,
            ReservationTimeId timeId,
            ThemeId themeId,
            MemberId memberId
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
    List<WaitingWithRank> findAllWaitingWithRankByMemberId(MemberId memberId);

    Optional<Waiting> findFirstByDateAndTimeAndThemeOrderByCreatedAtAsc(
            LocalDate date,
            ReservationTime time,
            Theme theme
    );
}
