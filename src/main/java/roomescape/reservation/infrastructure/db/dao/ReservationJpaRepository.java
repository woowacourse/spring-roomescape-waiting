package roomescape.reservation.infrastructure.db.dao;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import roomescape.reservation.model.entity.Reservation;
import roomescape.reservation.model.entity.vo.ReservationStatus;

public interface ReservationJpaRepository extends JpaRepository<Reservation, Long>,
        JpaSpecificationExecutor<Reservation> {

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    boolean existsByThemeId(Long reservationThemeId);

    boolean existsByTimeId(Long reservationTimeId);

    List<Reservation> findAllByMemberId(Long memberId);

    List<Reservation> findAllByStatusIn(List<ReservationStatus> statuses);
}
