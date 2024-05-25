package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWithRank;
import roomescape.domain.Theme;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    @Query("""
            SELECT r FROM Reservation r
            JOIN FETCH r.time
            JOIN FETCH r.theme
            JOIN FETCH r.reservationMember
            WHERE r.reservationMember.id = :memberId
                AND r.theme.id = :themeId
                AND r.date BETWEEN :start AND :end
            """)
    List<Reservation> findAllByMemberIdAndThemeIdAndDateBetween(
            long memberId,
            long themeId,
            LocalDate start,
            LocalDate end
    );

    @Query("""
            SELECT r FROM Reservation r 
            JOIN FETCH r.time 
            JOIN FETCH r.theme
            JOIN FETCH r.reservationMember
            WHERE r.date = :date AND r.theme = :theme
            """)
    List<Reservation> findAllByDateAndTheme(LocalDate date, Theme theme);

    @Query("""
            SELECT r FROM Reservation r 
            JOIN FETCH r.time 
            JOIN FETCH r.theme 
            JOIN FETCH r.reservationMember
            WHERE r.reservationMember.id = :memberId
            """)
    List<Reservation> findAllByMemberId(long memberId);

    @Query("""
            SELECT new roomescape.domain.ReservationWithRank(
                r, 
                (
                    SELECT COUNT(r2)
                    FROM Reservation r2
                    WHERE r2.date = r.date AND r2.time = r.time AND r2.theme = r.theme AND r2.id < r.id
                )
            )
            FROM Reservation r
            WHERE r.reservationMember.id = :memberId
            """)
    List<ReservationWithRank> findAllWithRankByMemberId(long memberId);


    Optional<Reservation> findFirstByDateAndAndTimeAndTheme(LocalDate date, ReservationTime time, Theme theme);

    boolean existsByThemeAndDateAndTime(Theme theme, LocalDate date, ReservationTime reservationTime);

    boolean existsByTime(ReservationTime reservationTime);

    boolean existsByTheme(Theme theme);
}
