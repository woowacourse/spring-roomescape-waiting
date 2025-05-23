package roomescape.waiting.repository;

import java.time.LocalDate;
import roomescape.waiting.domain.Waiting;

public interface WaitingRepository {
    Waiting save(Waiting waiting);

    /**
     * TODO
     * 세지 않고 order를 내림차순하고 첫번째 + 1해서 반환한다면?
     */
    long countByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId);

    boolean existsByDateAndThemeIdAndTimeIdAndMemberId(LocalDate date, long themeId, long timeId, long memberId);
}
