package roomescape.domain.reservation.repository.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.reservation.domain.reservation.Reservation;
import roomescape.domain.reservation.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.dto.ReservationWithOrderDto;

public interface JpaReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByThemeIdAndMemberIdAndDateValueBetween(Long themeId, Long memberId, LocalDate dateFrom,
                                                                  LocalDate dateTo);

    boolean existsByDateValueAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    boolean existsByMemberIdAndDateValueAndTimeIdAndThemeId(Long memberId, LocalDate date, Long timeId, Long themeId);

    Optional<Reservation> findTop1ByDateValueAndThemeIdAndTimeIdAndStatus(LocalDate date, Long timeId, Long themeId,
                                                                          ReservationStatus status);

    List<Reservation> findByDateValueAndThemeId(LocalDate date, Long themeId);

    @Query("""
            SELECT new roomescape.domain.reservation.dto.ReservationWithOrderDto(
            r,(
                SELECT COUNT(r2)
                FROM Reservation r2
                WHERE r2.date = r.date
                    AND r2.theme.id = r.theme.id
                    AND r2.time.id = r.time.id
                    AND r2.reservationTimestamp < r.reservationTimestamp
            ))
            FROM Reservation r
            WHERE r.member.id = :memberId
            """)
    List<ReservationWithOrderDto> findByMemberId(Long memberId);

    List<Reservation> findByStatus(ReservationStatus status);
}
