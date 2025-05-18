package roomescape.reservation.domain.reservation;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository {

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId);

    boolean existsByTimeId(long timeId);

    boolean existsByThemeId(long themeId);

    Reservation save(Reservation reservation);

    void deleteById(long id);

    List<Reservation> findAll();

    List<Reservation> findAllByDateAndThemeId(LocalDate date, long themeId);

    List<Reservation> findAllByMemberId(long id);

    List<Reservation> findAllByCondition(Long memberId, Long themeId, LocalDate from, LocalDate to);
}
