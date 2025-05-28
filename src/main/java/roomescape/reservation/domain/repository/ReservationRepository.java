package roomescape.reservation.domain.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("""
            SELECT r FROM Reservation r
            WHERE (:memberId IS NULL OR r.member.id = :memberId)
              AND (:themeId IS NULL OR r.theme.id = :themeId)
              AND (:from IS NULL OR :to IS NULL OR r.date BETWEEN :from AND :to)
            """)
    Collection<Reservation> findAllByMemberIdAndThemeIdAndDateBetween(
            @Param("memberId") Long memberId,
            @Param("themeId") Long themeId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    default Collection<Reservation> findAllBySearchFilter(Long memberId, Long themeId, LocalDate from, LocalDate to) {
        return findAllByMemberIdAndThemeIdAndDateBetween(memberId, themeId, from, to);
    }

    boolean existsByDateAndTimeId(LocalDate reservationDate, Long id);

    @Query("""
            SELECT COUNT(r) > 0 FROM Reservation r 
            WHERE (r.date = :reservationDate) 
              AND (r.time.id = :timeId) 
              AND (r.status = :status)
            """)
    boolean existsByDateAndTimeIdAndStatus(
            @Param("reservationDate") LocalDate reservationDate,
            @Param("timeId") Long timeId,
            @Param("status") Status status
    );

    Collection<Reservation> findAllByMemberId(Long memberId);

    @Query("SELECT r FROM Reservation r WHERE r.status = 'WAITING'")
    Collection<Reservation> findAllWaiting();

    @Query("SELECT r FROM Reservation r WHERE (r.status = 'WAITING') AND (r.id = :id)")
    Optional<Reservation> findByWaitingId(@Param("id") Long id);
}
