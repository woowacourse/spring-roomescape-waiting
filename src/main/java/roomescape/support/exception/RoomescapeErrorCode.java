package roomescape.support.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum RoomescapeErrorCode implements ErrorCode {
    BAD_REQUEST(HttpStatus.BAD_REQUEST,
        "요청 파라미터 혹은 문법이 유효하지 않습니다.", "API 명세를 확인하여 요청 파라미터 규격을 준수하십시오."),
    INVALID_JSON_FORMAT(HttpStatus.BAD_REQUEST,
        "요청 바디의 JSON 형식이 올바르지 않거나 역직렬화에 실패했습니다.", "JSON 문법 상태와 각 필드의 데이터 타입을 확인하십시오."),
    INVALID_PARAMETER_TYPE(HttpStatus.BAD_REQUEST,
        "요청 파라미터의 데이터 타입이 기대하는 타입과 일치하지 않습니다.", "경로 변수(Path Variable) 및 쿼리 파라미터의 타입을 확인하십시오."),
    MISSING_REQUIRED_PARAMETER(HttpStatus.BAD_REQUEST,
        "필수 쿼리 파라미터가 누락되었습니다.", "API 명세서의 필수 파라미터 포함 여부를 확인하십시오."),
    MISSING_PATH_VARIABLE(HttpStatus.BAD_REQUEST,
        "URL 경로 변수가 누락되었거나 형식이 올바르지 않습니다.", "URL 패턴과 리소스 식별자 포함 여부를 확인하십시오."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST,
        "도메인 유효성 검증 제약 조건을 위반했습니다.", "Validation 에러 상세 내용을 참조하여 비즈니스 규칙을 확인하십시오."),
    DATA_INTEGRITY_VIOLATION(HttpStatus.BAD_REQUEST,
        "데이터베이스 무결성 제약 조건(Unique, Foreign Key 등)을 위반했습니다.", "중복 데이터 존재 여부 및 참조 무결성 상태를 확인하십시오."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED,
        "해당 엔드포인트에서 지원하지 않는 HTTP 메서드입니다.", "Allow 헤더에 명시된 허용 메서드를 사용하여 재요청하십시오."),
    NOT_FOUND(HttpStatus.NOT_FOUND,
        "요청한 리소스 혹은 엔드포인트를 찾을 수 없습니다.", "요청 URL 경로 및 리소스 ID의 유효성을 확인하십시오."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,
        "처리되지 않은 서버 내부 오류가 발생했습니다.", "서버 로그의 Trace ID를 확인하여 백엔드 팀에 문의하십시오."),
    INVALID_GENERATED_KEY(HttpStatus.INTERNAL_SERVER_ERROR,
        "데이터베이스에서 생성된 식별자(PK)를 조회할 수 없습니다.", "DB 설정 및 엔티티 매핑 상태를 확인하십시오."),
    DATA_CONSISTENCY_VIOLATION(HttpStatus.INTERNAL_SERVER_ERROR,
        "데이터 정합성이 깨질 수 있는 내부 처리 실패가 발생했습니다.", "서버 로그를 확인하여 트랜잭션 처리 상태를 점검하십시오."),
    ;

    private final HttpStatus httpStatus;
    private final String message;
    private final String action;

    RoomescapeErrorCode(HttpStatus httpStatus, String message, String action) {
        this.httpStatus = httpStatus;
        this.message = message;
        this.action = action;
    }

    @Override
    public String getCode() {
        return name();
    }
}
