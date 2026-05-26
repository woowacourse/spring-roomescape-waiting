package roomescape.exception;

public class InvalidLoginException extends RoomescapeBaseException {
    public InvalidLoginException() {
        super("로그인 정보가 올바르지 않습니다. 아이디와 비밀번호를 확인해주세요.");
    }
}
