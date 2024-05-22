package roomescape.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.member.model.Member;
import roomescape.reservation.model.ReservationDate;
import roomescape.reservation.model.ReservationTime;
import roomescape.reservation.model.ReservationWaiting;
import roomescape.reservation.model.Theme;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationWaitingRepository extends JpaRepository<ReservationWaiting, Long> {

    List<ReservationWaiting> findAllByMemberId(Long memberId);

    Optional<ReservationWaiting> findTopByDateAndTimeAndThemeOrderByCreatedAtAsc(final ReservationDate date, final ReservationTime time, final Theme theme);

    int countAllByThemeAndDateAndTimeAndCreatedAtBefore(Theme theme, ReservationDate date, ReservationTime time, LocalDateTime createdAt);

    boolean existsByMemberAndDateAndTimeAndTheme(Member member, ReservationDate date, ReservationTime time, Theme theme);
}
