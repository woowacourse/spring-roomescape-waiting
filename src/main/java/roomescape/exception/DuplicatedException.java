package roomescape.exception;

public class DuplicatedException extends RuntimeException {

    public DuplicatedException(String field) {
        super("[ERROR] 이미 %s(이)가 존재합니다.".formatted(field));
    }
}
