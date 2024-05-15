package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.controller.reservation.dto.ReservationSearchCondition;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.service.exception.ReservationNotFoundException;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findAllByDateAndThemeId(LocalDate date, long themeId);

    default Reservation fetchById(long id) {
        return findById(id).orElseThrow(() -> new ReservationNotFoundException("존재하지 않는 예약입니다."));
    }

    boolean existsByTimeId(long timeId);

    boolean existsByThemeIdAndTimeIdAndDate(long themeId, long timeId, LocalDate date);

    boolean existsByThemeId(long themeId);

    List<Reservation> findAllByDateBetween(LocalDate from, LocalDate until);

    default List<Theme> findPopularThemes(LocalDate from, LocalDate until, int limit) {

        final List<Reservation> allBetween = findAllByDateBetween(from, until);
        return allBetween.stream()
                .collect(Collectors.groupingBy(Reservation::getTheme))
                .entrySet()
                .stream()
                .sorted(Comparator.comparingInt(a -> -a.getValue().size()))
                .limit(limit)
                .map(Map.Entry::getKey)
                .toList();
    }

    List<Reservation> findAllByThemeIdAndMemberIdAndDateBetween(long themeId, long memberId, LocalDate from, LocalDate until);

    default List<Reservation> searchReservations(ReservationSearchCondition condition) {
        return findAllByThemeIdAndMemberIdAndDateBetween(condition.themeId(), condition.memberId(),
                condition.dateFrom(), condition.dateTo());
    }

    List<Reservation> findAllByMemberId(Long id);
}
