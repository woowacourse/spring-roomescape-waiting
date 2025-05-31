package roomescape.reservation.domain.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Reservation;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByReservationSchedule_DateAndReservationSchedule_Theme_Id(LocalDate date, Long themeId);

    List<Reservation> findByReservationSchedule_Theme_IdAndMemberIdAndReservationSchedule_DateBetween(long themeId, long memberId, LocalDate start,
        LocalDate end);

    boolean existsByReservationSchedule_ReservationTime_Id(Long timeId);

    boolean existsByReservationSchedule_Theme_Id(Long themeId);

    boolean existsByMemberIdAndReservationSchedule_Theme_IdAndReservationSchedule_ReservationTime_IdAndReservationSchedule_Date(Long memberId, Long themeId, Long reservationTimeId,
        LocalDate date);

    List<Reservation> findByMemberId(Long memberId);
}
