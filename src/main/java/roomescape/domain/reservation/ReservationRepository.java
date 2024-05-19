package roomescape.domain.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import roomescape.domain.exception.DomainNotFoundException;
import roomescape.domain.reservation.dto.ReservationWithRankDto;

public interface ReservationRepository extends ListCrudRepository<Reservation, Long> {

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
            """)
    List<Reservation> findAllByConditions(
            @Param("memberId") Long memberId,
            @Param("themeId") Long themeId,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo
    );

    @Query("""
            SELECT
                new roomescape.domain.reservation.dto.ReservationWithRankDto(
                    r,
                    CAST ((
                        SELECT COUNT(r2)
                        FROM Reservation r2
                        WHERE r2.status = 'WAITING'
                        AND r2.theme = r.theme
                        AND r2.date = r.date
                        AND r2.time = r.time
                        AND r2.id <= r.id
                    ) AS Long)
                )
            FROM Reservation r
            JOIN FETCH r.member
            JOIN FETCH r.time
            JOIN FETCH r.theme
            WHERE r.member.id = :memberId
            """)
    List<ReservationWithRankDto> findReservationWithRanksByMemberId(Long memberId);

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
                .orElseThrow(() -> new DomainNotFoundException("해당 id의 예약이 존재하지 않습니다."));
    }

    default Reservation getByIdAndStatus(long id, ReservationStatus status) {
        return findByIdAndStatus(id, status)
                .orElseThrow(() -> new DomainNotFoundException("해당 id와 예약 상태의 예약이 존재하지 않습니다."));
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
