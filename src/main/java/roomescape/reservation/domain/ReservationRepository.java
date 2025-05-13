package roomescape.reservation.domain;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository {

    Long save(Reservation reservation);

    boolean deleteById(Long id);

    List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId);

    List<Reservation> findAll();

    List<Reservation> findByMemberIdAndThemeIdAndDate(Long memberId, Long themeId, LocalDate dateFrom,
                                                      LocalDate dateTo);

    boolean existByReservationTimeId(Long timeId);

    boolean hasSameReservation(Reservation reservation);

    boolean existByThemeId(Long themeId);

}
