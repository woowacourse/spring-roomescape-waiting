package roomescape.theme.controller.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import roomescape.global.response.SuccessCode;

@Getter
@RequiredArgsConstructor
public enum ThemeSuccessCode implements SuccessCode {

    CREATE_THEME("TS001", "테마를 생성하였습니다."),
    GET_THEMES("TS002", "모든 테마를 조회했습니다."),
    DELETE_THEME("TS003", "테마를 삭제했습니다."),
    GET_POPULAR_THEMES("TS004", "인기 테마를 조회했습니다.");

    private final String value;
    private final String message;
}
