package roomescape.theme.exception;

import roomescape.global.exception.ErrorCode;

public enum ThemeErrorCode implements ErrorCode {
    THEME_NOT_FOUND("요청하신 테마를 찾을 수 없습니다. 테마 목록을 새로고침하여 다시 확인해 주세요."),
    DUPLICATE_THEME("이미 등록된 테마 이름입니다. 다른 테마 이름을 입력하거나 기존 테마를 수정해 주세요."),
    THEME_IN_USE("해당 테마에 진행 중이거나 대기 중인 예약이 있습니다. 기존 예약을 취소하거나 완료한 후 삭제해 주세요."),
    INVALID_PERIOD_OR_LIMIT("요청 기간 및 개수는 1 이상이어야 합니다.");

    private final String message;

    ThemeErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
