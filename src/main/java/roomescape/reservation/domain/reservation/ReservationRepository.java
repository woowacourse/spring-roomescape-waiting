package roomescape.reservation.domain.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId);

    boolean existsByTimeId(long timeId);

    boolean existsByThemeId(long themeId);

    Reservation save(Reservation reservation);

    void deleteById(long id);

    List<Reservation> findAll();

    List<Reservation> findAllByDateAndThemeId(LocalDate date, long themeId);

    List<Reservation> findAllByMemberId(long memberId);

    List<Reservation> findAllByCondition(Long memberId, Long themeId, LocalDate from, LocalDate to);

    Optional<Reservation> findById(long id);

    Optional<Reservation> findByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId);
}
