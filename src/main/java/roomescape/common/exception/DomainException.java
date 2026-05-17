package roomescape.common.exception;

import lombok.Getter;

@Getter
public class DomainException extends RuntimeException {

    private final ErrorPolicy errorPolicy;

    public DomainException(ErrorPolicy errorPolicy) {
        super(errorPolicy.message());
        this.errorPolicy = errorPolicy;
    }

}
