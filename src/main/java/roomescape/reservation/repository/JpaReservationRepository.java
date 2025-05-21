package roomescape.reservation.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.theme.domain.Theme;

@Repository
public interface JpaReservationRepository extends JpaRepository<Reservation, Long>, ReservationRepository {

    @Override
    Optional<Reservation> findByInfoDateAndInfoTimeIdAndInfoThemeId(ReservationDate date, Long timeId, Long themeId);

    @Override
    List<Reservation> findAllByInfoMemberId(Long memberId);

    @Override
    List<Reservation> findByInfoDateAndInfoThemeId(ReservationDate date, Long themeId);

    @Override
    List<Reservation> findByInfoMemberIdAndInfoThemeIdAndInfoDateBetween(Long memberId, Long themeId, ReservationDate from,
                                                                         ReservationDate to);

    @Override
    @Query("""
           SELECT t FROM Theme t
           LEFT JOIN Reservation r ON r.info.theme = t
           AND r.info.date BETWEEN :startDate AND :endDate
           GROUP BY t
           ORDER BY COUNT(r) DESC
           LIMIT :limit
           """)
    List<Theme> findThemesWithReservationCount(ReservationDate startDate,
                                               ReservationDate endDate,
                                               int limit);

    @Override
    boolean existsByInfoTimeId(Long timeId);

    @Override
    boolean existsByInfoDateAndInfoTimeIdAndInfoThemeId(ReservationDate date, Long timeId, Long themeId);

}
