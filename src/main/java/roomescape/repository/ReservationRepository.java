package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;

public interface ReservationRepository {

    default Reservation getById(Long id) {
        return findById(id).orElseThrow(() -> new NoSuchElementException("[ERROR] 존재하지 않는 예약입니다."));
    }

    default Reservation getByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
        return findByDateAndTimeIdAndThemeId(date, timeId, themeId).orElseThrow(() ->
                new NoSuchElementException("[ERROR] 존재하지 않는 예약입니다.")
        );
    }

    Reservation save(Reservation reservation);

    void deleteById(Long id);

    Optional<Reservation> findById(Long id);

    Optional<Reservation> findByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    List<Long> findThemeReservationCountsForDate(LocalDate startDate, LocalDate endDate);

    List<Reservation> findAll();

    List<Reservation> findByMemberAndThemeAndDateRange(Long memberId, Long themeId, LocalDate dateFrom, LocalDate dateTo);

    List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId);

    List<Reservation> findAllByMember(Member member);

    boolean existsByTimeId(Long id);

    boolean existsByThemeId(Long id);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);
}
