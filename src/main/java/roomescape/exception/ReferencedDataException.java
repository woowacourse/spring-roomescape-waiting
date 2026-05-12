package roomescape.exception;

public class ReferencedDataException extends RuntimeException {
    public ReferencedDataException() {
        super("참조 중인 데이터가 있어 삭제할 수 없습니다.");
    }
}
