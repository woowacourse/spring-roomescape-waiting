package roomescape.reservation.repository;

import java.util.List;
import java.util.Optional;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.theme.domain.Theme;

public interface ReservationRepository {

    Optional<Reservation> findById(Long id);

    Optional<Reservation> findByInfoDateAndInfoTimeIdAndInfoThemeId(ReservationDate date, Long timeId, Long themeId);

    List<Reservation> findAll();

    List<Reservation> findAllByInfoMemberId(Long memberId);

    List<Reservation> findByInfoDateAndInfoThemeId(ReservationDate date, Long themeId);

    List<Reservation> findByInfoMemberIdAndInfoThemeIdAndInfoDateBetween(Long memberId, Long themeId, ReservationDate from,
                                                                         ReservationDate to);

    List<Theme> findThemesWithReservationCount(ReservationDate startDate, ReservationDate endDate, int limit);

    Reservation save(Reservation reservation);

    void deleteById(Long id);

    boolean existsByInfoTimeId(Long timeId);

    boolean existsByInfoDateAndInfoTimeIdAndInfoThemeId(ReservationDate date, Long timeId, Long themeId);
}
