package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import roomescape.reservation.domain.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findAllByMemberId(Long memberId);

    Optional<Reservation> findById(Long id);

    List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId);

    @Query("""
            SELECT r FROM Reservation r
            JOIN FETCH r.member
            JOIN FETCH r.theme
            JOIN FETCH r.time
            WHERE (:memberId IS NULL OR r.member.id = :memberId)
                AND (:themeId IS NULL OR r.theme.id = :themeId)
                AND (:dateFrom IS NULL OR r.date >= :dateFrom)
                AND (:dateTo IS NULL OR r.date <= :dateTo)
            """)
    List<Reservation> findByThemeIdAndMemberIdAndDateBetween(
            Long themeId,
            Long memberId,
            LocalDate dateFrom,
            LocalDate dateTo
    );

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    boolean existsByTimeId(final Long timeId);

    boolean existsByThemeId(final Long themeId);
}
