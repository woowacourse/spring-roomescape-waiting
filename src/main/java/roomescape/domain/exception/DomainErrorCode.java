package roomescape.domain.exception;

public enum DomainErrorCode {

    INVALID_INPUT,
    PAST_RESERVATION,
    DUPLICATE_RESERVATION,
    REFERENTIAL_INTEGRITY,
    UNAUTHORIZED_RESERVATION,
    RESERVATION_NOT_FOUND,
    WAITLIST_NOT_FOUND,
    RESERVATION_TIME_NOT_FOUND,
    THEME_NOT_FOUND;

    public String messageKey() {
        return "error." + name().toLowerCase();
    }

    public String titleKey() {
        return "error.title." + name().toLowerCase();
    }

}
