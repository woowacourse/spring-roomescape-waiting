package roomescape.exception;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String field, Long id) {
        super("[ERROR] 아이디가 %s인 %s가 존재하지 않습니다.".formatted(id, field));
    }
}
