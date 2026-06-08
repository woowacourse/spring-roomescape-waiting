package roomescape.domain;

public enum DomainErrorCode {
    RESOURCE_NOT_FOUND,
    ALREADY_EXISTS,
    INVALID_INPUT,
    PAST_DATE,
    FORBIDDEN,
    RESOURCE_IN_USE;

    public String messageKey() {
        return "error." + name().toLowerCase();
    }

    public String titleKey() {
        return "error.title." + name().toLowerCase();
    }
}
