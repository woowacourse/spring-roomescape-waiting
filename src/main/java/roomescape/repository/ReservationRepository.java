package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.service.ReservationWithRank;

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
            SELECT new roomescape.dto.service.ReservationWithRank(r1, COUNT(r1))
            FROM Reservation r1
            JOIN Reservation r2
            ON r2.date = r1.date AND r2.time = r1.time AND r2.theme = r1.theme AND r2.id < r1.id
            WHERE r1.status = 'PENDING' AND r1.reservationMember.id = :memberId
            GROUP BY r1
            """)
    List<ReservationWithRank> findAllWithRankByMemberId(long memberId);

    List<Reservation> findAllByStatus(ReservationStatus status);

    Optional<Reservation> findFirstByDateAndAndTimeAndTheme(LocalDate date, ReservationTime time, Theme theme);

    boolean existsByThemeAndDateAndTime(Theme theme, LocalDate date, ReservationTime reservationTime);

    boolean existsByThemeAndDateAndTimeAndReservationMember(
            Theme theme, LocalDate date, ReservationTime reservationTime, Member member);

    boolean existsByTime(ReservationTime reservationTime);

    boolean existsByTheme(Theme theme);
}
