package roomescape.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String targetName, Object value) {
        super(new DetailedErrorCode(targetName, value).getMessage());
    }

    private static class DetailedErrorCode {
        private final String message;

        public DetailedErrorCode(String targetName, Object value) {
            this.message = "%s를 찾을 수 없습니다. 입력값: %s".formatted(targetName, value);
        }

        public String getMessage() {
            return message;
        }
    }
}
