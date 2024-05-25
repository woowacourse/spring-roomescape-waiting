package roomescape.domain.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

public interface ReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {
    @Query("""
            SELECT r.time.id
            FROM Reservation r
            WHERE r.date = :date AND r.theme.id = :themeId
            """)
    List<Long> findTimeIdByDateAndThemeId(LocalDate date, long themeId);

    boolean existsByDateAndTimeAndTheme(LocalDate date, ReservationTime time, Theme theme);

    boolean existsByTimeId(long timeId);

    boolean existsByThemeId(long themeId);

    List<Reservation> findByMemberId(Long id);

    @Query("""
            SELECT t
            FROM Reservation r
            LEFT JOIN Theme t ON t.id=r.theme.id
            WHERE r.date > :startDate AND r.date < :endDate
            GROUP BY t.id
            ORDER BY COUNT(*) DESC
            LIMIT 10
            """)
    List<Theme> findThemeByMostPopularReservation(LocalDate startDate, LocalDate endDate);

    Optional<Reservation> findByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    interface Specs {
        static Specification<Reservation> hasMemberId(Long memberId) {
            return (root, query, builder) -> {
                if (memberId == null) {
                    return builder.conjunction();
                }
                return builder.equal(root.get("member").get("id"), memberId);
            };
        }

        static Specification<Reservation> hasThemeId(Long themeId) {
            return (root, query, builder) -> {
                if (themeId == null) {
                    return builder.conjunction();
                }
                return builder.equal(root.get("theme").get("id"), themeId);
            };
        }

        static Specification<Reservation> hasStartDate(LocalDate dateFrom) {
            return (root, query, builder) -> {
                if (dateFrom == null) {
                    return builder.conjunction();
                }
                return builder.greaterThanOrEqualTo(root.get("date"), dateFrom);
            };
        }

        static Specification<Reservation> hasEndDate(LocalDate dateTo) {
            return (root, query, builder) -> {
                if (dateTo == null) {
                    return builder.conjunction();
                }
                return builder.lessThanOrEqualTo(root.get("date"), dateTo);
            };
        }
    }
}
