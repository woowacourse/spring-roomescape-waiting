package roomescape.reservation.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationView;

import java.util.List;

public interface JpaReservationViewRepository extends JpaRepository<ReservationView, String> {

    boolean existsByDateAndTimeIdAndThemeIdAndUserId(ReservationDate date, Long timeId, Long themeId, Long userId);

    List<ReservationView> findAllByUserId(Long userId);
}

