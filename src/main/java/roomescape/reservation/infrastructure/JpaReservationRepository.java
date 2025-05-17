package roomescape.reservation.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.user.domain.UserId;

import java.util.List;

public interface JpaReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByTimeId(Long timeId);

    boolean existsByDateAndTimeIdAndThemeId(ReservationDate date, Long timeId, Long themeId);

    List<Reservation> findAllByUserId(UserId userId);

    List<Reservation> findAllByDateAndThemeId(ReservationDate date, Long themeId);
}

