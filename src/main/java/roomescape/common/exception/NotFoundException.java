package roomescape.common.exception;

import org.springframework.http.HttpStatus;
import roomescape.common.domain.DomainTerm;
import roomescape.common.exception.base.BusinessException;
import roomescape.common.exception.util.ExceptionMessageFormatter;

public class NotFoundException extends BusinessException {

    public NotFoundException(final DomainTerm term, final Object... params) {
        super(
                buildLogMessage(term, params),
                buildUserMessage(term)
        );
    }

    private static String buildLogMessage(final DomainTerm term, final Object... params) {
        return ExceptionMessageFormatter.format("[%s] not found.".formatted(term.name()), params);
    }

    private static String buildUserMessage(final DomainTerm term) {
        return "%s을(를) 찾을 수 없습니다.".formatted(term.label());
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
