package roomescape.waiting.controller.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import roomescape.global.response.SuccessCode;

@Getter
@RequiredArgsConstructor
public enum WaitingSuccessCode implements SuccessCode {

    WAITING_SUCCESS_CODE("RW001", "대기열에 성공적으로 추가되었습니다."),
    DELETE_WAITING_SUCCESS_CODE("RW002", "대기열에서 성공적으로 삭제되었습니다."),
    ;

    private final String value;
    private final String message;
}
