package roomescape.reservation.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByMemberIdAndThemeIdAndDateValueBetween(Long memberId,
                                                                  Long themeId,
                                                                  LocalDate dateFrom,
                                                                  LocalDate dateTo);

    List<Reservation> findByDateValueAndThemeId(LocalDate date, Long themeId);

    @Query("""
        SELECT new roomescape.reservation.domain.Reservation(r.id,
                                                             r.member,
                                                             r.date.value,
                                                             r.time,
                                                             r.theme,
                                                             r.status,
                                                             r.createdAt)
        FROM Reservation AS r
        WHERE r.member.id = :memberId
        AND r.status = 'RESERVED'
        """)
    List<Reservation> findAllReservedByMemberId(Long memberId);

    @Query("""
        SELECT new roomescape.reservation.domain.ReservationWaiting(r.id,
                                                                    r.member,
                                                                    r.date.value,
                                                                    r.time,
                                                                    r.theme,
                                                                    r.status,
                                                                    r.createdAt)
        FROM Reservation AS r
        WHERE r.member.id = :memberId
        AND r.status = 'WAITING'
        """)
    List<ReservationWaiting> findAllReservationWaitingByMemberId(Long memberId);

    @Query("""
        SELECT new roomescape.reservation.domain.ReservationWaiting(r.id,
                                                                    r.member,
                                                                    r.date.value,
                                                                    r.time,
                                                                    r.theme,
                                                                    r.status,
                                                                    r.createdAt)
        FROM Reservation AS r
        WHERE r.status = :status
        """)
    List<ReservationWaiting> findAllReservationByStatus(@Param("status") Status status);

    Optional<Reservation> findFirstByDateValueAndTimeIdAndThemeIdAndStatus(LocalDate date,
                                                                           Long timeId,
                                                                           Long themeId,
                                                                           Status status);

    @Query("""
            SELECT count (r.id) FROM Reservation AS r
            WHERE r.date.value = :date
                AND r.time.id = :timeId
                AND r.theme.id = :themeId
                AND r.createdAt < :createdAt
            """)
    int countWaitingRank(LocalDate date, Long timeId, Long themeId, LocalDateTime createdAt);

    boolean existsByDateValueAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    boolean existsByDateValueAndTimeIdAndThemeIdAndMemberId(LocalDate date, Long timeId, Long themeId, Long memberId);
}
