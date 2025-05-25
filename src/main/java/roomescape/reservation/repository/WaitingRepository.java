package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.member.entity.Member;
import roomescape.reservation.entity.Waiting;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    boolean existsByDateAndTimeIdAndThemeIdAndMemberId(LocalDate date, Long timeId, Long themeId, Long memberId);

    List<Waiting> findAllByMember(Member member);

    List<Waiting> findAllByDateAndTimeIdAndThemeIdOrderByCreatedAt(LocalDate date, Long timeId, Long themeId);
}
