package roomescape.common.exception;

import java.util.NoSuchElementException;

public class EntityNotFoundException extends NoSuchElementException {

    public EntityNotFoundException(final String message) {
        super(message);
    }
}
