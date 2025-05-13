package roomescape.reservation.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservationRepository {

    Long save(Reservation reservation);

    boolean deleteById(Long id);

    List<Reservation> findBy(LocalDate date, Long themeId);

    List<Reservation> findBy(Long memberId, Long themeId, LocalDate from, LocalDate to);

    List<Reservation> findAll();

    boolean existByReservationTimeId(Long timeId);

    boolean existBy(Long themeId, LocalDate date, LocalTime time);

    boolean existBy(Long themeId);

}
