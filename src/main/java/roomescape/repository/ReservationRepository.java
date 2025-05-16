package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.User;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUser(User user);

    List<Reservation> findByThemeAndDateAndUser(Theme theme, LocalDate date, User user);

    boolean existsByReservationTime(ReservationTime reservationTime);

    boolean existsByDateAndReservationTime(LocalDate date, ReservationTime reservationTime);

    @Query(value = "SELECT reservation.id AS reservationId, " +
            "       reservation.date AS reservationDate, " +
            "       reservation_time.id AS reservationTimeId, " +
            "       reservation_time.start_at AS reservationTimeStartAt, " +
            "       theme.id AS themeId, " +
            "       theme.name AS themeName, " +
            "       theme.description AS themeDescription, " +
            "       theme.thumbnail AS themeThumbnail, " +
            "       users.id AS userId, " +
            "       users.role AS userRole, " +
            "       users.name AS userName, " +
            "       users.email AS userEmail " +
            "FROM reservation " +
            "JOIN reservation_time ON reservation.time_id = reservation_time.id " +
            "JOIN theme ON reservation.theme_id = theme.id " +
            "JOIN users ON reservation.user_id = users.id " +
            "WHERE users.id = :userId " +
            "AND theme.id = :themeId " +
            "AND reservation.date BETWEEN :fromDate AND :toDate",
            nativeQuery = true)
    List<Reservation> findReservationsByFilter(
            @Param("userId") Long userId,
            @Param("themeId") Long themeId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);
}
