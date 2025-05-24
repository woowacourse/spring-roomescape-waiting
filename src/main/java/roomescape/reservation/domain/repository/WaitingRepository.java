package roomescape.reservation.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.WaitingStatus;
import roomescape.theme.domain.Theme;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    @Query("""
            SELECT w FROM Waiting w
            JOIN FETCH w.time
            JOIN FETCH w.theme
            JOIN FETCH w.member
            WHERE w.waitingStatus = :waitingStatus
            """)
    List<Waiting> findByWaitingWithAssociations(@Param("waitingStatus") WaitingStatus waitingStatus);

    @Query("""
            SELECT w FROM Waiting w
            JOIN FETCH w.theme
            JOIN FETCH w.time
            WHERE w.id = :id
            """)
    Optional<Waiting> findByIdWithAssociations(@Param("id") Long id);

    @Query("""
            SELECT w FROM Waiting w
            JOIN FETCH w.theme
            JOIN FETCH w.time
            WHERE w.member.id = :memberId
              AND w.waitingStatus = :waitingStaus
            """)
    List<Waiting> findByMemberIdAndWaitingStatusWithAssociations(@Param("memberId") Long memberId,
                                                                 @Param("waitingStaus") WaitingStatus waitingStatus);

    @Query("""
              SELECT COUNT(w)
              FROM Waiting w
              WHERE w.theme = :theme
                AND w.date = :date
                AND w.time = :time
                AND w.id < :id
            """)
    Long countByThemeAndDateAndTimeAndIdLessThan(
            @Param("theme") Theme theme,
            @Param("date") LocalDate date,
            @Param("time") ReservationTime time,
            @Param("id") Long id
    );

    Optional<Waiting> findByMemberId(Long memberId);

    boolean existsByDateAndTimeAndThemeAndMember(LocalDate date, ReservationTime time, Theme theme, Member member);

    boolean existsByIdAndMemberId(Long reservationId, Long memberId);
}

