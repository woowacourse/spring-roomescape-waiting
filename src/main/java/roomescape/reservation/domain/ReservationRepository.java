package roomescape.reservation.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservationRepository {
    Reservation save(Reservation reservation);

    void deleteById(Long id);

    List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId);

    List<Reservation> findAll();

    List<Reservation> findByMemberIdAndThemeIdAndDate(Long memberId, Long themeId, LocalDate dateFrom,
                                                      LocalDate dateTo);

    List<Reservation> findByMemberId(Long id);

    boolean existsByTimeId(Long timeId);

    boolean existsByDateAndTimeStartAtAndThemeId(LocalDate date, LocalTime time, Long themeId);

    boolean existsByThemeId(Long themeId);
}
