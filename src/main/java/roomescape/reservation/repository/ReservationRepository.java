package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import roomescape.reservation.domain.Date;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.Time;

public interface ReservationRepository extends Repository<Reservation, Long> {

    Reservation save(Reservation reservation);

    List<Reservation> findAllByOrderByDateAscTimeAsc();

    List<Reservation> findAllByThemeIdAndDate(long themeId, Date date);

    List<Reservation> findAllByMemberIdAndThemeIdAndDateBetween(long memberId, long themeId, Date fromDate,
                                                                Date toDate);

    Optional<Reservation> findById(long id);

    Optional<Reservation> findFirstByDateAndThemeAndTime(Date date, Theme theme, Time time);

    List<Reservation> findByTimeId(long timeId);

    Optional<Reservation> findByDateAndMemberIdAndThemeIdAndTimeId(Date date, long memberId, long themeId, long timeId);

    List<Reservation> findByThemeId(long themeId);

    List<Reservation> findByReservationStatus(ReservationStatus reservationStatus);

    @Query("SELECT r.theme " +
            "FROM Reservation r " +
            "WHERE r.date.date between :startDate AND :endDate " +
            "GROUP BY r.theme.id " +
            "ORDER BY COUNT(r.theme.id) DESC " +
            "LIMIT :limitCount "
    )
    List<Theme> findAllByDateOrderByThemeIdCountLimit(LocalDate startDate, LocalDate endDate, int limitCount);

    int countByThemeAndDateAndTimeAndIdLessThan(Theme theme, Date date, Time time, long waitingId);

    int countByThemeIdAndDateAndTimeIdAndReservationStatus(long themeId, Date date, long timeId,
                                                           ReservationStatus status);

    void deleteById(long reservationId);

    List<Reservation> findAllByMemberIdAndReservationStatus(long id, ReservationStatus reservationStatus);
}
