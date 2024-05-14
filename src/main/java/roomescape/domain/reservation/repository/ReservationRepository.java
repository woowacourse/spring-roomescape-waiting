package roomescape.domain.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.reservation.domain.reservation.Reservation;
import roomescape.domain.reservation.domain.reservationTime.ReservationTime;
import roomescape.domain.theme.domain.Theme;

public interface ReservationRepository {

    List<Reservation> findAll();

    List<Reservation> findAllBy(Long themeId, Long memberId, LocalDate dateFrom, LocalDate dateTo);

    Optional<Reservation> findById(Long id);

    Reservation insert(Reservation reservation);

    boolean existByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    void deleteById(Long id);

    List<ReservationTime> findTimesByDateAndTheme(LocalDate date, Long themeId);

    List<Theme> findThemeOrderByReservationCount();

}
