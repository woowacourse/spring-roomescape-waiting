package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.RoomTheme;

public interface ReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

    Optional<List<Reservation>> findByThemeId(Long themeId);

    List<Reservation> findByMemberId(Long memberId);

    Optional<Reservation> findByDateAndTimeAndTheme(LocalDate date, ReservationTime time, RoomTheme theme);
}
