package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.Member;
import roomescape.domain.TimeSlot;
import roomescape.domain.Waiting;

import java.time.LocalDate;
import java.util.List;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    List<Waiting> findAllByMember(Member member);

    boolean existsByDateAndTimeAndMember(LocalDate date, TimeSlot timeSlot, Member member);

}
