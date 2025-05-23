package roomescape.waiting.repository;

import java.time.LocalDate;
import java.util.List;
import roomescape.waiting.domain.Waiting;

public interface WaitingRepository {
    Waiting save(Waiting waiting);
    long countByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId);
    boolean existsByDateAndThemeIdAndTimeIdAndMemberId(LocalDate date, long themeId, long timeId, long memberId);
    List<Waiting> findAllByMemberId(long memberId);
}
