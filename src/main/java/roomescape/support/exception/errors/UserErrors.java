package roomescape.support.exception.errors;

import lombok.Getter;

@Getter
public enum UserErrors implements Errors {

    USER_NAME_NOT_FOUND("존재하지 않는 이름 입니다."),
    ;

    private final String message;

    UserErrors(String message) {
        this.message = message;
    }

    @Override
    public String getCode() {
        return name();
    }
}
