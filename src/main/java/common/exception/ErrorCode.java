package common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum ErrorCode {
    // 요청 형식 및 필수값 위반 (BAD REQUEST)
    INVALID_NAME_LENGTH("이름 길이는 1자 ~ 20자 사이여야 합니다.", HttpStatus.BAD_REQUEST),
    INVALID_THEME_NAME_LENGTH("테마 이름 길이는 1자 ~ 50자 사이여야 합니다.", HttpStatus.BAD_REQUEST),
    INVALID_THUMBNAIL_URL("유효하지 않은 이미지 주소입니다. URL은 https로 시작해야 합니다.", HttpStatus.BAD_REQUEST),

    // 리소스가 존재하지 않음 (NOT FOUND)
    THEME_NOT_FOUND("존재하지 않는 테마입니다. 입력을 확인해 주세요.", HttpStatus.NOT_FOUND),
    RESERVATION_NOT_FOUND("존재하지 않는 예약입니다. 입력을 확인해 주세요.", HttpStatus.NOT_FOUND),
    RESERVATION_TIME_NOT_FOUND("존재하지 않는 시간입니다. 입력을 확인해 주세요.", HttpStatus.NOT_FOUND),
    SLOT_NOT_FOUND("존재하지 않는 슬롯입니다. 입력을 확인해 주세요", HttpStatus.NOT_FOUND),

    // DB 제약 조건 관련 위반 (CONFLICT)
    DUPLICATE_RESERVATION("이미 예약된 시간입니다. 다른 시간을 선택해 주세요.", HttpStatus.CONFLICT),
    RESERVATION_TIME_IN_USE("시간을 사용하는 예약이 존재합니다. 관련 예약을 지우고 요청해 주세요.", HttpStatus.CONFLICT),
    THEME_IN_USE("테마를 사용하는 예약이 존재합니다. 관련 예약을 지우고 요청해 주세요", HttpStatus.CONFLICT),

    // 비즈니스 규칙 위반 (UNPROCESSABLE_ENTITY),
    PAST_DATE_NOT_ALLOWED("기준 날짜는 과거일 수 없습니다. 오늘 이후 날짜를 입력해 주세요", HttpStatus.UNPROCESSABLE_ENTITY),
    PAST_RESERVATION_NOT_ALLOWED("과거 예약에 대한 조작은 불가능합니다. 오늘 이후 날짜와 시간으로 다시 시도해 주세요", HttpStatus.UNPROCESSABLE_ENTITY),

    // 서버 내부 오류 (INTERNAL_SERVER_ERROR);
    INVALID_RANK_VALUE("잘못된 순번이 설정되었습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String message;
    private final HttpStatus httpStatus;
}
