package roomescape.reservation.dao;

import java.time.LocalDate;
import java.util.List;
import roomescape.reservation.domain.Reservation;

public interface ReservationDao {

    Reservation save(Reservation reservationSimpleInfo);

    void saveMemberReservation(long reservationId, long memberId);

    List<Reservation> findAllOrderByDateAndTime();

    List<Reservation> findAllByThemeIdAndDate(long themeId, LocalDate date);

    Reservation findByIdOrderByDate(long reservationId);

    List<Long> findReservationIdsByMemberId(long memberId);

    void deleteById(long reservationId);

    int countByTimeId(long timeId);

}
