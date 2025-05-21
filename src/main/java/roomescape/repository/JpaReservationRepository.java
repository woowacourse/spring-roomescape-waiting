package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.Reservation;

public interface JpaReservationRepository extends JpaRepository<Reservation, Long> {
    boolean existsByThemeId(Long id);

    List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId);

    List<Reservation> findReservationsByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId);

    @Query(
            """
        SELECT r 
        FROM Reservation r 
        WHERE r.date between :start AND :end
          AND r.theme.id = :themeId
          AND r.member.id = :memberId
        """)
    List<Reservation> findByPeriod(LocalDate start, LocalDate end, long themeId, long memberId);

    @Query("""
        SELECT exists 
            (SELECT r
             FROM Reservation r
             WHERE r.date = :date AND r.time.id = :timeId AND r.theme.id = :themeId AND r.member.id = :memberId)
    """)
    boolean existsFor(LocalDate date, long timeId, long themeId, long memberId);

    boolean existsByTimeId(Long id);

    List<Reservation> findReservationsByMemberId(long id);
}
