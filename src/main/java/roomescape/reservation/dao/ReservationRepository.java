package roomescape.reservation.dao;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.reservation.domain.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findAllByOrderByDateAsc();

    List<Reservation> findAllByTheme_IdAndDate(Long themeId, LocalDate date);

    List<Reservation> findAllByMember_Id(Long memberId);

    List<Reservation> findAllByMember_IdOrderByDateAsc(Long id);

    int countReservationsByTime_Id(Long timeId);

}
