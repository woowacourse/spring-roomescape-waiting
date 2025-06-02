package roomescape.reservation.domain.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Reservation;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT r "
        + "FROM Reservation r "
        + "WHERE r.reservationSchedule.date = :date "
        + "AND r.reservationSchedule.theme.id = :themeId"
    )
    List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId);

    @Query("SELECT r "
        + "FROM Reservation r "
        + "WHERE r.reservationSchedule.theme.id = :themeId "
        + "AND r.member.id = :memberId "
        + "AND r.reservationSchedule.date BETWEEN :start AND :end "
    )
    List<Reservation> findByThemeIdAndMemberIdAndDateBetween(long themeId, long memberId, LocalDate start,
        LocalDate end);

    @Query("SELECT COUNT(r) > 0 "
        + "FROM Reservation r "
        + "WHERE r.reservationSchedule.reservationTime.id = :timeId ")
    boolean existsByTimeId(Long timeId);

    @Query("SELECT COUNT(r) > 0 "
        + "FROM Reservation r "
        + "WHERE r.reservationSchedule.theme.id = :themeId ")
    boolean existsByThemeId(Long themeId);

    @Query("SELECT COUNT(r) > 0 "
        + "FROM Reservation r "
        + "WHERE r.member.id = :memberId "
        + "AND r.reservationSchedule.theme.id = :themeId "
        + "AND r.reservationSchedule.reservationTime.id = :reservationTimeId "
        + "AND r.reservationSchedule.date = :date"
    )
    boolean existsByMemberIdAndThemeIdAndTimeIdAndDate(Long memberId, Long themeId, Long reservationTimeId,
        LocalDate date);

    List<Reservation> findByMemberId(Long memberId);
}
