package roomescape.domain.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.reservation.entity.Reservation;
import roomescape.domain.reservation.entity.ReservationStatus;
import roomescape.domain.theme.entity.Theme;
import roomescape.domain.time.entity.Time;

public interface ReservationRepository {

    List<Reservation> findReservationsByNotDeleted();

    List<Reservation> findReservationsByNameAndNotDeleted(String name);

    Optional<Reservation> findReservationByIdAndNotDeleted(Long id);

    List<Long> findTimeIdsByDateAndThemeIdAndNotDeleted(LocalDate localDate, Long themeId);

    Reservation save(Reservation reservation);

    Reservation update(Reservation reservation);

    int countByIdLessThanEqualAndDateAndTimeAndTheme(Long id, LocalDate date, Time time, Theme theme);

    void deleteReservationById(Long id);

    boolean existsReservationByIdAndNotDeleted(Long id);

    boolean existsReservationByDateAndTimeAndThemeAndNotDeleted(LocalDate date, Time time, Theme theme);

    boolean existsReservationAndStatus(Reservation reservation, ReservationStatus status);

    boolean existsReservationByDateAndTimeAndThemeAndNotDeletedAndIdNot(LocalDate date, Time time, Theme theme,
                                                                        Long id);
}
