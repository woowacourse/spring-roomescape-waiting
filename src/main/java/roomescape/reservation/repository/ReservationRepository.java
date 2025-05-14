package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.service.dto.ReservationInfo;

public interface ReservationRepository {

    List<Reservation> findAll();

    Reservation save(Reservation reservation);

    void deleteById(long id);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId);

    boolean existsByTimeId(long timeId);

    boolean existsByThemeId(Long themeId);

    List<Reservation> findAllByDateAndThemeId(LocalDate date, long themeId);

    List<Reservation> findAllByMemberId(long id);

    List<Reservation> findAllByCondition(Long memberId, Long themeId, LocalDate from, LocalDate to);
}
