package roomescape.reservation.dao;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.reservation.domain.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // void saveMemberReservation(Long reservationId, Long memberId);

    List<Reservation> findAllByOrderByDateAsc();

    @Query("SELECT r FROM Reservation r WHERE r.theme.id = :themeId AND r.date = :date")
    List<Reservation> findAllByTheme_IdAndDate(@Param("themeId") Long themeId, @Param("date") LocalDate date);

    Reservation findByIdOrderByDateAsc(Long reservationId);

    @Query("SELECT r.id FROM Reservation r WHERE r.member.id = :memberId")
    List<Long> findReservationIdsByMember_Id(@Param("memberId") Long memberId);

    int countReservationsByTime_Id(Long timeId);

}
