package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.member.entity.Member;
import roomescape.reservation.entity.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findAllByThemeIdAndMemberIdAndDateBetween(Long themeId, Long memberId, LocalDate dateAfter, LocalDate dateBefore);

    List<Reservation> findAllByMember(Member member);

    boolean existsByTimeId(Long timeId);

    boolean existsByDateAndTimeId(LocalDate date, Long timeId);

    boolean existsByThemeId(Long themeId);
}
