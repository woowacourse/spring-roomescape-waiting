package roomescape.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDate;

public interface ReservationRepository extends JpaRepository<Reservation, Long>,
        JpaSpecificationExecutor<Reservation> {

    boolean existsByReservationDateAndReservationTime_Id(
            final ReservationDate reservationDate,
            final Long timeId
    );

    boolean existsByReservationTime_Id(final Long timeId);

    boolean existsByTheme_Id(final Long themeId);

    List<Reservation> findByMember_Id(final Long memberId);
}
