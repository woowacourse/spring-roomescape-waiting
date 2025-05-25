package roomescape.reservation.domain.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByTimeId(Long timeId);

    boolean existsByDateAndTimeAndTheme(LocalDate date, ReservationTime time, Theme theme);

    List<Reservation> findAllByDateAndThemeId(LocalDate date, Long themeId);

    @Query(value = "SELECT r.theme FROM Reservation r WHERE r.date BETWEEN :startDate AND :endDate GROUP BY r.theme.id ORDER BY COUNT(r) DESC")
    List<Theme> findTopThemesByReservationCountBetween(LocalDate startDate, LocalDate endDate);

    List<Reservation> findAllByThemeIdAndMemberIdAndDateBetween(Long themeId, Long memberId, LocalDate dateFrom,
                                                                LocalDate dateTo);

    List<Reservation> findAllByMember(Member member);

    long countByDateAndTimeAndTheme(LocalDate date, ReservationTime time, Theme theme);

    List<Reservation> findAllByStatus(ReservationStatus reservationStatus);

    @Query(value = "SELECT COUNT(r) "
            + "FROM Reservation r "
            + "WHERE r.date = :date "
            + "AND r.time = :time "
            + "AND r.theme = :theme "
            + "AND r.createdAt < :createdAt")
    int countReservationsBefore(LocalDate date, ReservationTime time, Theme theme, LocalDateTime createdAt);

    boolean existsByTheme(Theme theme);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM Reservation r " +
            "WHERE r.date = :date " +
            "AND r.time = :time " +
            "AND r.theme = :theme " +
            "AND r.member = :member)")
    boolean alreadyExists(LocalDate date, ReservationTime time, Theme theme, Member member);
}
