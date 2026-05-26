package roomescape.domain.reservation.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import roomescape.domain.reservation.entity.Reservation;
import roomescape.domain.theme.entity.Theme;
import roomescape.domain.time.entity.Time;

public interface ReservationRepository {

    List<Reservation> findReservationsByDeletedAtIsNull();

    List<Reservation> findReservationsByNameAndDeletedAtIsNull(String name);

    Optional<Reservation> findReservationByIdAndDeletedAtIsNull(Long id);

    List<Long> findTimeIdsByDateAndThemeIdAndDeletedAtIsNull(LocalDate localDate, Long themeId);

    Reservation save(Reservation reservation);

    Reservation update(Reservation reservation);

    Reservation cancelReservationById(Long id, LocalDateTime dateTime);

    void deleteReservationById(Long id);

    boolean existsReservationByIdAndDeletedAtIsNull(Long id);

    boolean existsReservationByDateAndTimeAndThemeAndDeletedAtIsNull(LocalDate date, Time time, Theme theme);

    boolean existsReservationByDateAndTimeAndThemeAndDeletedAtIsNullAndIdNot(LocalDate date, Time time, Theme theme,
        Long id);
}
