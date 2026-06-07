package roomescape.exception.custom;

public class WaitNotExistsException extends CustomException {

    public WaitNotExistsException() {
        super("해당 대기가 존재하지 않습니다.");
    }
}
