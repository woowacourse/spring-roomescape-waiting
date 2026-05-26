package roomescape.exception;

public class DuplicateUsernameException extends RoomescapeBaseException {
    public DuplicateUsernameException() {
        super("이미 존재하는 username입니다. 다른 username을 입력해주세요.");
    }
}
