package roomescape.exception;

public class UnauthenticatedException extends RoomescapeBaseException {
    public UnauthenticatedException() {
        super("인증이 필요합니다. 로그인 후 이용해주세요.");
    }
}