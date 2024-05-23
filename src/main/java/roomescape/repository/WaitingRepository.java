package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.Member;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;
import roomescape.domain.Waiting;

import java.time.LocalDate;
import java.util.List;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    List<Waiting> findAllByMemberOrderByDateAsc(Member member);

    boolean existsByDateAndTimeAndThemeAndMember(LocalDate date, TimeSlot timeSlot, Theme theme, Member member);

}
