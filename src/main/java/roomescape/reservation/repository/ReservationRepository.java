package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import roomescape.reservation.domain.Reservation;
import roomescape.theme.domain.Theme;

public interface ReservationRepository extends Repository<Reservation, Long> {

    Reservation save(Reservation reservation);

    List<Reservation> findAllByOrderByDateAscTimeAsc();

    List<Reservation> findAllByThemeIdAndDate_Date(long themeId, LocalDate date);

    List<Reservation> findAllByMemberIdAndThemeIdAndDate_DateBetween(long memberId, long themeId, LocalDate fromDate,
                                                                     LocalDate toDate);

    List<Reservation> findByTimeId(long timeId);

    List<Reservation> findByThemeId(long themeId);

    @Query("SELECT r.theme " +
            "FROM Reservation r " +
            "WHERE r.date.date between :startDate AND :endDate " +
            "GROUP BY r.theme.id " +
            "ORDER BY COUNT(r.theme.id) DESC "
    )
    List<Theme> findAllByDateOrderByThemeIdCountLimit(LocalDate startDate, LocalDate endDate);

    void deleteById(long reservationId);
}
