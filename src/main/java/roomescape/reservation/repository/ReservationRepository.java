package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import roomescape.member.domain.MemberId;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTimeId;
import roomescape.reservation.domain.ThemeId;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("""
            SELECT r
            FROM Reservation r
            JOIN FETCH r.theme
            JOIN FETCH r.member
            JOIN FETCH r.time
            """)
    List<Reservation> findAll();

    @Query("""
            SELECT r
            FROM Reservation r
            JOIN FETCH r.member
            JOIN FETCH r.theme
            JOIN FETCH r.time
            WHERE r.member.id = :memberId
            """)
    List<Reservation> findAllByMemberId(MemberId memberId);

    List<Reservation> findByDateAndThemeId(LocalDate date, ThemeId themeId);

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
            ThemeId themeId,
            MemberId memberId,
            LocalDate dateFrom,
            LocalDate dateTo
    );

    boolean existsByDateAndTimeIdAndThemeId(
            LocalDate date,
            ReservationTimeId timeId,
            ThemeId themeId
    );

    boolean existsByTimeId(ReservationTimeId timeId);

    boolean existsByThemeId(ThemeId themeId);

    boolean existsByDateAndThemeIdAndTimeIdAndMemberId(
            LocalDate date,
            ThemeId themeId,
            ReservationTimeId timeId,
            MemberId memberId
    );
}
