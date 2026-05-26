package roomescape.support.exception.errors;

import lombok.Getter;

@Getter
public enum UserErrors implements Errors {

    USER_NAME_NOT_FOUND("존재하지 않는 이름 입니다."),
    INVALID_USER_NAME("이름은 비어 있을 수 없습니다."),
    INVALID_USER_NAME_LENGTH("이름은 10자 이하여야 합니다."),
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
