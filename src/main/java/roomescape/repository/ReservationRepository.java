package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

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
    List<Reservation> findByReservationMember_IdAndTheme_IdAndDateBetween(
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
            WHERE r.reservationMember.id = :memberId
            """)
    List<Reservation> findAllByReservationMember_Id(long memberId);

    @Query("""
            SELECT r FROM Reservation r 
            JOIN FETCH r.time 
            JOIN FETCH r.theme
            JOIN FETCH r.reservationMember
            WHERE r.date = :date AND r.theme.id = :themeId
            """)
    List<Reservation> findAllByDateAndTheme_Id(LocalDate date, long themeId);

    boolean existsByThemeAndDateAndTime(Theme theme, LocalDate date, ReservationTime reservationTime);

    boolean existsByTime(ReservationTime reservationTime);

    boolean existsByTheme(Theme theme);
}
