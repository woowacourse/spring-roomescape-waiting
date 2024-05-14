package roomescape.reservation.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.member.domain.Member;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT r FROM Reservation r JOIN FETCH r.time JOIN FETCH r.theme JOIN FETCH r.member")
    List<Reservation> findAllWithDetails();

    @Query("""
            SELECT r FROM Reservation r
            JOIN FETCH r.time
            JOIN FETCH r.theme
            JOIN FETCH r.member
            WHERE r.member = :member AND r.theme = :theme AND r.date >= :from AND r.date <= :to
            """)
    List<Reservation> findAllByMemberAndThemeAndDateBetween(@Param(value = "member") Member member,
                                                            @Param(value = "theme") Theme theme,
                                                            @Param(value = "from") LocalDate fromDate,
                                                            @Param(value = "to") LocalDate toDate);

    @Query("SELECT r.time.id FROM Reservation r WHERE r.date = :date AND r.theme = :theme")
    List<Long> findAllTimeIdsByDateAndTheme(@Param(value = "date") LocalDate date, @Param(value = "theme") Theme theme);

    @Query("""
            SELECT r FROM Reservation r
            JOIN FETCH r.time
            JOIN FETCH r.theme
            JOIN FETCH r.member
            WHERE r.member = :member
            """)
    List<Reservation> findAllByMemberWithDetails(@Param(value = "member") Member member);

    boolean existsByDateAndTimeAndTheme(LocalDate date, ReservationTime time, Theme theme);

    int countByTime(ReservationTime time);
}
