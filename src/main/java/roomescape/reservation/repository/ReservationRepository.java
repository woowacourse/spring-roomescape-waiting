package roomescape.reservation.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByTimeId(long timeId);

    List<Reservation> findByThemeId(long themeId);

    List<Reservation> findByMemberId(long memberId);

    List<Reservation> findByDateBetween(LocalDate startDate, LocalDate endDate);

    List<Reservation> findByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId);

    int countByDateAndTimeIdAndThemeIdAndCreatedAtBefore(LocalDate date,
                                                         long timeId,
                                                         long themeId,
                                                         LocalDateTime createdAt);

    List<Reservation> findByDateAndThemeId(LocalDate date, long themeId);

    @Query("""
            SELECT r FROM Reservation AS r
            LEFT JOIN FETCH r.theme
            LEFT JOIN FETCH r.member
            LEFT JOIN FETCH r.time
            WHERE (:themeId IS NULL OR r.theme.id = :themeId)
            AND (:memberId IS NULL OR r.member.id = :memberId)
            AND (:status IS NULL OR r.status = :status)
            AND (:dateFrom IS NULL OR r.date >= :dateFrom)
            AND (:dateTo IS NULL OR r.date <= :dateTo)
            """)
    List<Reservation> findByThemeIdAndMemberIdAndStatusAndDateBetween(Long themeId, Long memberId,
                                                                      Optional<Status> status,
                                                                      LocalDate dateFrom, LocalDate dateTo);

    Optional<Reservation> findFirstByDateAndTimeIdAndThemeIdOrderByCreatedAt(LocalDate date, long timeId, long themeId);
}
