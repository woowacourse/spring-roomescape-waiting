package roomescape.reservation.domain;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository {

    Reservation save(Reservation reservation);

    void deleteById(Long id);

    List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId);

    List<Reservation> findAll();

    List<Reservation> findByMemberIdAndThemeIdAndDate(Long memberId, Long themeId, LocalDate dateFrom,
                                                      LocalDate dateTo);

    boolean existsByTimeId(Long timeId);

    boolean hasReservedReservation(Reservation reservation);

    boolean hasSameReservation(Reservation reservation);

    boolean existsByThemeId(Long themeId);

    List<Reservation> findByMemberId(Long memberId);
}
