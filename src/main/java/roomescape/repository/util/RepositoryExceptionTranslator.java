package roomescape.repository.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import roomescape.exception.DuplicateEntityException;

@Slf4j
public final class RepositoryExceptionTranslator {

    private RepositoryExceptionTranslator() {
    }

    public static void execute(Runnable action, String message) {
        try {
            action.run();
        } catch (DataIntegrityViolationException e) {
            log.warn("DB constraint violation", e);
            throw new DuplicateEntityException(message);
        }
    }
}