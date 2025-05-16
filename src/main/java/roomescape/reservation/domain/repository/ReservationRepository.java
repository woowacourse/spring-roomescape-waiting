package roomescape.reservation.domain.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.reservation.domain.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("""
            SELECT r FROM Reservation r
            JOIN FETCH r.time
            JOIN FETCH r.theme
            JOIN FETCH r.member
            """)
    List<Reservation> findAllWithAssociations();

    @Query("""
              SELECT r FROM Reservation r
              JOIN FETCH r.time
              WHERE r.date = :date AND r.theme.id = :themeId
            """)
    List<Reservation> findByDateAndThemeIdWithAssociations(LocalDate date, Long themeId);

    @Query("""
              SELECT r FROM Reservation r
              JOIN FETCH r.time
              JOIN FETCH r.theme
              JOIN FETCH r.member
              WHERE (:themeId IS NULL OR r.theme.id = :themeId)
                AND (:memberId IS NULL OR r.member.id = :memberId)
                AND (:from IS NULL OR r.date >= :from)
                AND (:to IS NULL OR r.date <= :to)
            """)
    List<Reservation> findByFilteringWithAssociations(
            Long themeId,
            Long memberId,
            LocalDate from,
            LocalDate to);

    @Query("""
              SELECT r FROM Reservation r
              JOIN FETCH r.theme
              JOIN FETCH r.time
              WHERE r.member.id = :memberId
            """)
    List<Reservation> findByMemberIdWithAssociations(Long memberId);

    boolean existsByTimeId(Long timeId);

    boolean existsByThemeId(Long themeId);
}
