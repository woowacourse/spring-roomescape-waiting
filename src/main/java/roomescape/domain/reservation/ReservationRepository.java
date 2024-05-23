package roomescape.domain.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import roomescape.domain.exception.DomainNotFoundException;
import roomescape.domain.reservation.dto.WaitingWithRankDto;

public interface ReservationRepository extends ListCrudRepository<Reservation, Long> {

    List<Reservation> findByMemberIdAndStatus(long memberId, ReservationStatus status);

    boolean existsByTimeId(long id);

    boolean existsByThemeId(long id);

    boolean existsByDateAndTimeIdAndThemeIdAndStatus(
            LocalDate date,
            long timeId,
            long themeId,
            ReservationStatus status
    );

    boolean existsByDateAndTimeIdAndThemeIdAndMemberIdAndStatus(
            LocalDate date,
            long timeId,
            long themeId,
            long memberId,
            ReservationStatus status
    );

    Optional<Reservation> findByIdAndStatus(long id, ReservationStatus status);

    @Query("""
                SELECT r
                FROM Reservation r
                JOIN FETCH r.member m
                JOIN FETCH r.time t
                JOIN FETCH r.theme th
                WHERE (:memberId IS NULL OR r.member.id = :memberId)
                AND (:themeId IS NULL OR r.theme.id = :themeId)
                AND (:dateFrom IS NULL OR r.date >= :dateFrom)
                AND (:dateTo IS NULL OR r.date <= :dateTo)
                AND r.status = 'RESERVED'
            """)
    List<Reservation> findAllByConditions(
            @Param("memberId") Long memberId,
            @Param("themeId") Long themeId,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo
    );

    @Query("""
            SELECT
                new roomescape.domain.reservation.dto.WaitingWithRankDto(
                    r,
                    COUNT(*)
                )
            FROM Reservation r
            JOIN Reservation r2
            ON r.date = r2.date AND r.time = r2.time AND r.theme = r2.theme
            JOIN FETCH r.member
            JOIN FETCH r.time
            JOIN FETCH r.theme
            WHERE r.member.id = :memberId AND r.status = 'WAITING' AND r2.status = 'WAITING' AND r.id >= r2.id
            GROUP BY r.id
            """)
    List<WaitingWithRankDto> findWaitingsWithRankByMemberId(Long memberId);

    @Query("""
            SELECT r
            FROM Reservation r
            JOIN FETCH r.member
            JOIN FETCH r.time
            JOIN FETCH r.theme
            WHERE r.status = :reservationStatus
            """)
    List<Reservation> findAllByStatus(ReservationStatus reservationStatus);

    default Reservation getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new DomainNotFoundException(String.format("해당 id의 예약이 존재하지 않습니다. (id: %d)", id)));
    }

    default Reservation getByIdAndStatus(long id, ReservationStatus status) {
        String message = String.format("해당 id와 예약 상태의 예약이 존재하지 않습니다. (id: %d, status: %s)", id, status);

        return findByIdAndStatus(id, status)
                .orElseThrow(() -> new DomainNotFoundException(message));
    }

    default boolean existsByReservation(LocalDate date, long timeId, long themeId, ReservationStatus status) {
        return existsByDateAndTimeIdAndThemeIdAndStatus(date, timeId, themeId, status);
    }

    default boolean existsByReservationWithMemberId(
            LocalDate date,
            long timeId,
            long themeId,
            long memberId,
            ReservationStatus status
    ) {
        return existsByDateAndTimeIdAndThemeIdAndMemberIdAndStatus(date, timeId, themeId, memberId, status);
    }
}
