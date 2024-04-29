package roomescape.reservation.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    List<Reservation> findAll();

    List<Reservation> findAllByMemberAndThemeAndPeriod(Long memberId, Long themeId, LocalDate dateFrom,
                                                       LocalDate dateTo);

    Optional<Reservation> findById(Long id);

    Reservation save(Reservation reservation);

    boolean existByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    void deleteById(Long id);
}
