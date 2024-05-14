package roomescape.exception;

public class ForbiddenException extends IllegalArgumentException {
    public ForbiddenException(final String path){
        super(String.format("%s는 운영자만 접근 가능합니다.",path));
    }
}
