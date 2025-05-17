package roomescape.common.exception;

import org.springframework.http.HttpStatus;
import roomescape.common.domain.DomainTerm;
import roomescape.common.exception.base.BusinessException;
import roomescape.common.exception.util.ExceptionMessageFormatter;

public class ConstraintConflictException extends BusinessException {

    public ConstraintConflictException(final DomainTerm term, final Object... params) {
        super(buildLogMessage(term, params), buildUserMessage(term));
    }

    private static String buildLogMessage(final DomainTerm term, final Object... params) {
        return ExceptionMessageFormatter.format("[%s] is referenced by another entity.".formatted(term.name()), params);
    }

    private static String buildUserMessage(final DomainTerm term) {
        return "%s이(가) 다른 항목에 의해 사용 중이어서 삭제할 수 없습니다.".formatted(term.label());
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.CONFLICT;
    }
}
