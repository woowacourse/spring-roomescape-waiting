package roomescape.repository;

import roomescape.domain.Session;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SessionRepository {

    List<Session> findAll();

    Optional<Session> findById(long id);

    Session save(Session session);

    Optional<Session> findByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    void deleteById(long id);
}
