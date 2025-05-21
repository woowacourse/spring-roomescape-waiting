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
import roomescape.theme.domain.Theme;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    @Query("""
            SELECT w FROM Waiting w
            JOIN FETCH w.time
            JOIN FETCH w.theme
            JOIN FETCH w.member
            """)
    List<Waiting> findAllWithAssociations();

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
            """)
    List<Waiting> findByMemberIdWithAssociations(@Param("memberId") Long memberId);

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

    boolean existsByDateAndTimeAndThemeAndMember(LocalDate date, ReservationTime time, Theme theme, Member member);

    boolean existsByIdAndMember_Id(Long reservationId, Long memberId);
}

