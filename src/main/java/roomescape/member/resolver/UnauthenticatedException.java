package roomescape.member.resolver;

public class UnauthenticatedException extends RuntimeException {
    public  UnauthenticatedException(String message) {
        super(message);
    }
}
