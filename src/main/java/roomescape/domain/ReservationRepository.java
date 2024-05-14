package roomescape.domain;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository {

    List<Reservation> findAll();

    Reservation save(Reservation reservation);

    int deleteById(Long id);

    boolean isTimeIdExists(Long id);

    boolean isThemeIdExists(Long id);

    boolean isDuplicated(LocalDate date, Long timeId, Long themeId);

    List<Reservation> findAllByDateAndThemeId(LocalDate date, Long themeId);

    List<Reservation> findAllMemberIdAndThemeIdInPeriod(Long memberId, Long themeId, String from, String to);
}
