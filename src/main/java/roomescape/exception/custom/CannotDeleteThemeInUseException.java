package roomescape.exception.custom;

public class CannotDeleteThemeInUseException extends CustomException {

    public CannotDeleteThemeInUseException() {
        super("예약에서 사용 중인 테마는 삭제할 수 없습니다.");
    }
}
