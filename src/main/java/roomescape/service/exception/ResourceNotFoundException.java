package roomescape.service.exception;

import roomescape.controller.exception.BaseException;
//todo: 이름 변경 필요
public class ResourceNotFoundException extends BaseException {

    public ResourceNotFoundException(String detail) {
        super("리소스를 찾을 수 없습니다.", detail);
    }
}
