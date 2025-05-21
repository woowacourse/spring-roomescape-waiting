package roomescape.reservation.infrastructure;

import java.time.LocalDate;
import org.springframework.data.repository.Repository;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.TimeSlot;
import roomescape.reservation.domain.Waiting;

public interface WaitingRepository extends Repository<Waiting, Long> {

    Waiting save(Waiting waiting);

    boolean existsByDateAndMemberAndThemeAndTimeSlot(LocalDate date, Member member, Theme theme, TimeSlot timeSlot);
}
