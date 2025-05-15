package roomescape.reservation.repository;

import java.util.List;
import java.util.Optional;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.theme.domain.Theme;

public interface ReservationRepository {

    boolean existsByTimeId(Long timeId);

    boolean existsByDateAndTimeIdAndThemeId(ReservationDate date, Long timeId, Long themeId);

    List<Reservation> findByMemberIdAndThemeIdAndDateBetween(Long memberId, Long themeId, ReservationDate from,
                                                             ReservationDate to);

    List<Reservation> findByDateAndThemeId(ReservationDate date, Long themeId);

    List<Reservation> findAllByMemberId(Long memberId);

    List<Theme> findThemesWithReservationCount(ReservationDate startDate, ReservationDate endDate, int limit);

    Reservation save(Reservation reservation);

    List<Reservation> findAll();

    void deleteById(Long id);

    Optional<Reservation> findById(Long id);
}
