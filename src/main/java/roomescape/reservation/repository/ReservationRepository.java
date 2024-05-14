package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import roomescape.reservation.domain.Reservation;

public interface ReservationRepository {

    Reservation save(Reservation reservation);

    List<Reservation> findAll();

    List<Reservation> findAll(Long themeId, Long memberId);

    List<Long> findAlreadyBookedTimeIds(LocalDate date, Long themeId);

    boolean isAlreadyBooked(Reservation reservation);

    void delete(Long id);
}
