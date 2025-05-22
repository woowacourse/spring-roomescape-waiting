package roomescape.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.Waiting;

public interface WaitingRepository {

    Waiting save(Waiting waiting);

    boolean hasAlreadyWaited(Long memberId, Long themeId, Long timeId, LocalDate date);

    Optional<Waiting> findFirstWaiting(LocalDate date, Long themeId, Long timeId);

    void delete(Waiting waiting);

    Optional<Waiting> findById(Long id);

    List<Waiting> findByMemberId(Long memberId);

    List<Waiting> findAll();
}
