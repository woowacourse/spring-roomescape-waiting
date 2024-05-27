package roomescape.exception;

public class AuthenticationInformationNotFoundException extends RuntimeException {
    public AuthenticationInformationNotFoundException() {
        super("인증 정보를 찾을 수 없습니다.");
    }
}
