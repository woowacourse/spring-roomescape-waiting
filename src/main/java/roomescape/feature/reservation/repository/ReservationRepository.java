package roomescape.feature.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.feature.reservation.domain.Reservation;
import roomescape.feature.reservation.domain.ReservationStatus;
import roomescape.feature.reservation.domain.ReserverName;
import roomescape.feature.reservation.domain.Slot;
import roomescape.feature.theme.domain.Theme;
import roomescape.feature.time.domain.Time;

public interface ReservationRepository {

    List<Reservation> findAllReservations();

    Optional<Reservation> findLowestIdWaitingReservation(LocalDate date, Long timeId, Long themeId);

    boolean existsActiveReservation(LocalDate date, Long timeId, Long themeId);

    List<Slot> findDeadSlots();

    List<Reservation> findReservationsByNameAndNotDeleted(ReserverName name);

    Optional<Reservation> findReservationByIdAndNotDeleted(Long id);

    List<Long> findTimeIdsByDateAndThemeIdAndNotDeleted(LocalDate localDate, Long themeId);

    Reservation save(Reservation reservation);

    Reservation update(Reservation reservation);

    int changeStatus(Long id, ReservationStatus from, ReservationStatus to);

    int countByIdLessThanEqualAndDateAndTimeAndTheme(Long id, LocalDate date, Time time, Theme theme);

    boolean existsReservationByIdAndNotDeleted(Long id);

    boolean existsReservationByDateAndTimeAndThemeAndActive(LocalDate date, Time time, Theme theme);

    boolean existsReservationAndStatus(Reservation reservation, ReservationStatus status);

    boolean existsActiveOrWaitingReservation(LocalDate date, Time time, Theme theme);
}
