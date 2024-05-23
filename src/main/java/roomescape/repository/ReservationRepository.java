package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;

public interface ReservationRepository {

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
