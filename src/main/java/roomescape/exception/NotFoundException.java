package roomescape.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException() {
       super("존재하지 않는 리소스입니다.");
    }

    public NotFoundException(String message) {
        super(message);
    }
}
