package roomescape.reservation.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.reservation.domain.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("select r.reservationTime.id from Reservation r where r.date = :date and r.theme.id = :themeId")
    List<Long> findTimeIdsByDateAndThemeId(LocalDate date, Long themeId);

    List<Reservation> findAllByTheme_IdAndMember_IdAndDateBetween(
            Long themeId,
            Long memberId,
            LocalDate dateFrom,
            LocalDate dateTo
    );

    boolean existsByDateAndReservationTime_StartAt(LocalDate date, LocalTime startAt);

//        String sql = """
//                select exists (select 1
//                from reservation r
//                join reservation_time t on r.time_id = t.id
//                where r.date = ? and t.start_at = ?)
//                """;

}
