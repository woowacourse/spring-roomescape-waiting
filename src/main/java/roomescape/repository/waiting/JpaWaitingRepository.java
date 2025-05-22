package roomescape.repository.waiting;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.member.Member;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.domain.waiting.Waiting;

import java.time.LocalDate;

public interface JpaWaitingRepository extends JpaRepository<Waiting, Long> {

    boolean existsByDateAndTimeAndThemeAndMember(LocalDate date, ReservationTime time, Theme theme, Member member);
}
