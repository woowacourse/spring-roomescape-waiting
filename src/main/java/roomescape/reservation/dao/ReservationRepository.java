package roomescape.reservation.dao;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.reservation.domain.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findAllByOrderByReservationContent_Date();

    List<Reservation> findAllByReservationContent_Theme_IdAndReservationContent_Date(Long themeId, LocalDate date);

    List<Reservation> findAllByMember_Id(Long memberId);

    List<Reservation> findAllByReservationContent_IdOrderByCreatedAtAsc(Long reservationContentId);

    int countReservationsByReservationContent_Time_Id(Long timeId);

}
