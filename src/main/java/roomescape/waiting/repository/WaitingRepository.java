package roomescape.waiting.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.waiting.domain.Waiting;

public interface WaitingRepository {
    Waiting save(Waiting waiting);
    long countByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId);
    boolean existsByDateAndThemeIdAndTimeIdAndMemberId(LocalDate date, long themeId, long timeId, long memberId);
    List<Waiting> findAllByMemberId(long memberId);
    void delete(Waiting waiting);
    boolean existsByIdAndMemberId(long id, long memberId);
    List<Waiting> findAllByThemeAndDateAndTime(Theme theme, LocalDate date, ReservationTime reservationTime);
    Optional<Waiting> findById(long id);
    List<Waiting> findAll();
    Optional<Waiting> popFirstWaiting(Theme theme, LocalDate date, ReservationTime time);
}
