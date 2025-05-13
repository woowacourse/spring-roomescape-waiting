package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.admin.domain.dto.SearchReservationRequestDto;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.user.domain.User;

public interface ReservationRepository {

    List<Reservation> findAll();

    Optional<Reservation> findById(Long id);

    Reservation findByIdOrThrow(Long id);

    List<Reservation> findByThemeAndDate(Theme theme, LocalDate date, User user);

    Reservation add(Reservation reservation);

    void deleteById(Long id);

    boolean existsByReservationTime(ReservationTime reservationTime);

    boolean existsByDateAndTime(LocalDate date, ReservationTime reservationTime);

    List<Reservation> findReservationsByUserAndThemeAndFromAndTo(
            SearchReservationRequestDto searchReservationRequestDto);
}
