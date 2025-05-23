package roomescape.reservation.repository;

import java.time.LocalDate;
import org.springframework.data.repository.ListCrudRepository;
import roomescape.reservation.domain.Waiting;

public interface WaitingListCrudRepository extends ListCrudRepository<Waiting, Long> {

    boolean existsByDateAndTimeIdAndThemeIdAndMemberId(LocalDate date, Long timeId, Long themeId, Long memberId);
}
