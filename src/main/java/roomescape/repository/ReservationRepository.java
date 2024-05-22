package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.Member;
import roomescape.domain.Reservation;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    boolean existsByDateAndTimeIdAndThemeIdAndMemberId(LocalDate date, Long timeId, Long themeId, Long memberId);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    boolean existsByTimeId(Long timeId);

    boolean existsByThemeId(Long themeId);

    List<Reservation> findByMember(Member member);

    List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId);

    List<Reservation> findAllByThemeIdAndMemberIdAndDateBetween(Long themeId, Long memberId, LocalDate dateFrom,
                                                                LocalDate dateTo);

    List<Reservation> findByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);
}
