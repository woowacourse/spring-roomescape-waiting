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

    List<Reservation> findAllByThemeIdAndDate(Long themeId, Date date);

    List<Reservation> findAllByMemberIdAndThemeIdAndDateBetween(Long memberId, Long themeId, Date fromDate,
                                                                Date toDate);

    Optional<Reservation> findById(Long id);

    Optional<Reservation> findFirstByDateAndThemeAndTimeAndReservationStatus(Date date, Theme theme, Time time,
                                                                             ReservationStatus status);

    List<Reservation> findByTimeId(Long timeId);

    Optional<Reservation> findByDateAndMemberIdAndThemeIdAndTimeId(Date date, Long memberId, Long themeId, Long timeId);

    Optional<Reservation> findByDateAndMemberIdAndThemeIdAndTimeIdAndReservationStatus(Date date, Long memberId,
                                                                                       Long themeId, Long timeId,
                                                                                       ReservationStatus reservationStatus);

    List<Reservation> findByThemeId(Long themeId);

    List<Reservation> findByReservationStatus(ReservationStatus reservationStatus);

    @Query("SELECT r.theme " +
            "FROM Reservation r " +
            "WHERE r.date.date between :startDate AND :endDate " +
            "GROUP BY r.theme.id " +
            "ORDER BY COUNT(r.theme.id) DESC " +
            "LIMIT :limitCount "
    )
    List<Theme> findAllByDateOrderByThemeIdCountLimit(LocalDate startDate, LocalDate endDate, int limitCount);

    int countByThemeAndDateAndTimeAndIdLessThan(Theme theme, Date date, Time time, Long waitingId);

    int countByThemeIdAndDateAndTimeIdAndReservationStatus(Long themeId, Date date, Long timeId,
                                                           ReservationStatus status);

    void deleteById(Long reservationId);

    List<Reservation> findAllByMemberIdAndReservationStatus(Long id, ReservationStatus reservationStatus);
}
