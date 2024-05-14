package roomescape.domain.reservation;

import roomescape.exception.UnauthorizedException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {
    List<Reservation> findAll();

    Reservation save(Reservation reservation);

    void deleteById(long id);

    boolean existsByDateAndTimeAndTheme(String date, long timeId, long themeId);

    boolean existsByTimeId(long timeId);

    boolean existsByThemeId(long themeId);

    boolean existsById(long id);

    Optional<Reservation> findById(long id);

    default Reservation getById(long id) {
        return findById(id).orElseThrow(() -> new UnauthorizedException("더이상 존재하지 않는 예약입니다."));
    }

    List<Reservation> findBy(Long memberId, Long themeId, LocalDate dateFrom, LocalDate dateTo);
}
