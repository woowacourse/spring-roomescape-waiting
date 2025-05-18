package roomescape.reservation.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.theme.domain.Theme;

@Repository
public interface JpaReservationRepository extends JpaRepository<Reservation, Long>, ReservationRepository {

    @Override
    List<Reservation> findAllByMemberId(Long memberId);

    @Override
    List<Reservation> findByDateAndThemeId(ReservationDate date, Long themeId);

    @Override
    List<Reservation> findByMemberIdAndThemeIdAndDateBetween(Long memberId, Long themeId, ReservationDate from,
                                                             ReservationDate to);

    @Override
    @Query("SELECT t FROM Theme t " +
           "LEFT JOIN Reservation r ON r.theme.id = t.id " +
           "WHERE r.date BETWEEN :startDate AND :endDate " +
           "GROUP BY t " +
           "ORDER BY COUNT(r) DESC " +
           "LIMIT :limit")
    List<Theme> findThemesWithReservationCount(ReservationDate startDate,
                                               ReservationDate endDate,
                                               int limit);

    @Override
    boolean existsByTimeId(Long timeId);

    @Override
    boolean existsByDateAndTimeIdAndThemeId(ReservationDate date, Long timeId, Long themeId);

}
