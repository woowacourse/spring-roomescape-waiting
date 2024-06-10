package roomescape.domain.reservation;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    Reservation save(Reservation reservation);

    List<Reservation> findAll();

    Optional<Reservation> findById(long id);


    Boolean existsByTimeId(Long id);

    Boolean existsByThemeId(Long id);

    Boolean existsByDateAndTimeIdAndThemeId(Date date, Long timeId, Long themeId);

    List<Reservation> findAllByDateAndThemeId(Date date, Long themeId);

    List<Reservation> findAllByThemeIdAndMemberIdAndDateIsBetween(Long themeId, Long memberId,
        Date dateFrom,
        Date dateTo);

    List<Reservation> findAllByMemberId(Long memberId);

    List<Reservation> findAllByDateAndTimeIdAndThemeId(Date date, Long timeId, Long themeId);

    Boolean existsByMemberIdAndTimeIdAndThemeIdAndDate(Long memberId, Long timeId, Long themeId, Date date);

    List<Reservation> findAllByStatus(ReservationStatus Status);

    Reservation deleteById(long id);
}
