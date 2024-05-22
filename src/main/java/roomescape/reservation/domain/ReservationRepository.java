package roomescape.reservation.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.domain.Member;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
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
            WHERE r.member = :member AND r.status = :status
            """)
    List<Reservation> findAllByMemberAndStatusWithDetails(@Param(value = "member") Member member,
                                                          @Param(value = "status") ReservationStatus status);

    int countByTime(ReservationTime time);

    @Query("""
            SELECT new roomescape.reservation.domain.WaitingReservation(
                r,
                (SELECT COUNT(w) FROM Reservation w
                    WHERE w.theme = r.theme
                        AND w.date = r.date
                        AND w.time = r.time
                        AND w.status = r.status
                        AND w.id < r.id))
            FROM Reservation r
            JOIN FETCH r.time
            JOIN FETCH r.theme
            WHERE r.member = :member AND r.status = 'WAITING'
            """)
    List<WaitingReservation> findWaitingReservationsByMemberWithDetails(@Param(value = "member") Member member);

    @Query("""
            SELECT r FROM Reservation r
            JOIN FETCH r.member
            JOIN FETCH r.theme
            JOIN FETCH r.time
            WHERE r.status = :status
            """)
    List<Reservation> findAllByStatusWithDetails(@Param(value = "status") ReservationStatus status);

    Optional<Reservation> findFirstByDateAndTimeAndTheme(LocalDate date, ReservationTime time, Theme theme);

    boolean existsByDateAndTimeAndTheme(LocalDate date, ReservationTime time, Theme theme);

    boolean existsByDateAndTimeAndThemeAndMember(LocalDate date, ReservationTime time, Theme theme, Member member);
}
