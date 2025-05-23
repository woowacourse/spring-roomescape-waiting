package roomescape.reservation.repository;

import java.time.LocalDate;
import roomescape.reservation.domain.Waiting;

public interface WaitingRepository {

    Waiting save(Waiting waiting);

    boolean existsByDateAndTimeIdAndThemeIdAndMemberId(LocalDate date, Long timeId, Long themeId, Long memberId);
}
