package roomescape.reservation.repository;

import java.util.List;
import java.util.Optional;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.theme.domain.Theme;

public interface ReservationRepository {

    Optional<Reservation> findById(Long id);

    List<Reservation> findAll();

    List<Reservation> findAllByMemberId(Long memberId);

    List<Reservation> findByDateAndThemeId(ReservationDate date, Long themeId);

    List<Reservation> findByMemberIdAndThemeIdAndDateBetween(Long memberId, Long themeId, ReservationDate from,
                                                             ReservationDate to);

    List<Theme> findThemesWithReservationCount(ReservationDate startDate, ReservationDate endDate, int limit);

    Reservation save(Reservation reservation);

    void deleteById(Long id);

    boolean existsByTimeId(Long timeId);

    boolean existsByDateAndTimeIdAndThemeId(ReservationDate date, Long timeId, Long themeId);
}
