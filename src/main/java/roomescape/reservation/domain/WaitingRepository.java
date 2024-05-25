package roomescape.reservation.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.member.domain.Member;

import java.time.LocalDate;
import java.util.Optional;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    @Query("SELECT new roomescape.reservation.domain.WaitingWithRank(" +
            "w, " +
            "(SELECT COUNT(w2) " +
            "FROM Waiting w2 " +
            "WHERE w2.theme = w.theme " +
            "AND w2.date = w.date " +
            "AND w2.time = w.time " +
            "AND w2.id < w.id)) " +
            "FROM Waiting w " +
            "WHERE w.member = :member")
    WaitingWithRank findByMember(@Param(value = "member") Member member);

    void deleteByMemberAndDateAndTimeAndTheme(Member member, LocalDate date, ReservationTime time, Theme theme);

    @Query("SELECT w " +
            "FROM Waiting w " +
            "WHERE w.date = :date AND w.time = :time AND w.theme = :theme " +
            "ORDER BY w.id " +
            "LIMIT 1")
    Optional<Waiting> findFistByDateAndTimeAndThemeOrderByIdAsc(LocalDate date, ReservationTime time, Theme theme);
}
