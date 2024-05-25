package roomescape.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByThemeAndDate(Theme theme, LocalDate date);

    List<Reservation> findAllByMemberId(long userId);

    List<Reservation> findByDateBetween(LocalDate dateFrom, LocalDate dateTo);

    List<Reservation> findByThemeIdAndDateBetween(Long themeId, LocalDate dateFrom, LocalDate dateTo);

    List<Reservation> findByMemberIdAndDateBetween(Long memberId, LocalDate dateFrom, LocalDate dateTo);

    List<Reservation> findByThemeIdAndMemberIdAndDateBetween(Long themeId, Long memberId, LocalDate dateFrom,
                                                             LocalDate dateTo);

    @Query("""
            SELECT r.theme, COUNT(r) AS themeCount
            FROM Reservation r
            WHERE r.date BETWEEN :startDate AND :endDate
            GROUP BY r.theme
            ORDER BY themeCount DESC
            LIMIT :limit""")
    List<Theme> findAndOrderByPopularity(@Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate,
                                         @Param("limit") int limit);

    boolean existsByTimeId(long timeId);

    boolean existsByThemeId(long themeId);

    default long calculateIndexOf(Reservation reservation) {
        return countByDateAndThemeAndTimeAndCreatedAtLessThanEqual(
                reservation.getDate(),
                reservation.getTheme(),
                reservation.getReservationTime(),
                reservation.getCreatedAt()
        );
    }

    long countByDateAndThemeAndTimeAndCreatedAtLessThanEqual(
            LocalDate date, Theme theme, ReservationTime reservationTime, LocalDateTime createdAt
    );

    default List<Reservation> findAllRemainedWaiting(LocalDateTime base) {
        List<Reservation> remainedAllReservation = findAllByDateGreaterThanEqual(base.toLocalDate());

        return remainedAllReservation.stream()
                .filter(reservation -> reservation.isAfter(base))
                .filter(this::isWaiting)
                .toList();
    }

    private boolean isWaiting(Reservation reservation) {
        return calculateIndexOf(reservation) > 1;
    }

    List<Reservation> findAllByDateGreaterThanEqual(LocalDate date);
}
