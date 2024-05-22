package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.theme.Theme;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByMemberId(final Long memberId);

    List<Reservation> findByDateAndThemeId(final LocalDate date, final Long themeId);

    @Query(
            """
            SELECT r FROM Reservation AS r
            WHERE (:themeId IS NULL OR r.theme.id = :themeId)
            AND (:memberId IS NULL OR r.member.id = :memberId)
            AND (:dateFrom IS NULL OR r.date >= :dateFrom)
            AND (:dateTo IS NULL OR r.date <= :dateTo)
            AND r.status = :status
            """
    )
    List<Reservation> findByThemeIdAndMemberIdAndDateBetweenAndStatus(
            final Long themeId,
            final Long memberId,
            final LocalDate dateFrom,
            final LocalDate dateTo,
            final ReservationStatus status);

    List<Reservation> findByStatus(final ReservationStatus status);

    int countByTimeId(final Long timeId);

    int countByDateAndTimeIdAndThemeId(final LocalDate date, final Long timeId, final Long themeId);

    boolean existsById(final Long id);

    boolean existsByThemeAndDateAndTimeAndStatus(final Theme theme, final LocalDate date,
                                                 final ReservationTime time, final ReservationStatus status);
}
