package roomescape.repository.waiting;

import roomescape.domain.waiting.Waiting;

import java.util.Optional;

public interface WaitingRepository {

    long save(Waiting waiting);

    Optional<Waiting> findById(Long id);

    boolean existsByDateAndTimeAndThemeAndMember(Waiting waiting);
}
