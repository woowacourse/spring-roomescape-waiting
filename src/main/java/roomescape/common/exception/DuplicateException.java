package roomescape.common.exception;

import org.springframework.http.HttpStatus;
import roomescape.common.domain.DomainTerm;
import roomescape.common.exception.base.BusinessException;
import roomescape.common.exception.util.ExceptionMessageFormatter;

public class DuplicateException extends BusinessException {

    public DuplicateException(final DomainTerm term, final Object... params) {
        super(
                buildLogMessage(term, params),
                buildUserMessage(term)
        );
    }

    private static String buildLogMessage(final DomainTerm term, final Object... params) {
        return ExceptionMessageFormatter.format("%s already exists.".formatted(term.name()), params);
    }

    private static String buildUserMessage(final DomainTerm term) {
        return "이미 %s이(가) 존재합니다.".formatted(term.label());
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.CONFLICT;
    }
}
