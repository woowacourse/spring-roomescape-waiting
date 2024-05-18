package roomescape.reservation.dao;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import roomescape.reservation.domain.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findAllByOrderByDateAsc();

    @Query("SELECT r FROM Reservation r WHERE r.theme.id = :themeId AND r.date = :date")
    List<Reservation> findAllByTheme_IdAndDate(@Param("themeId") Long themeId, @Param("date") LocalDate date);

    List<Reservation> findAllByMember_Id(Long memberId);

    List<Reservation> findAllByMember_IdOrderByDateAsc(Long id);

    int countReservationsByTime_Id(Long timeId);
}
