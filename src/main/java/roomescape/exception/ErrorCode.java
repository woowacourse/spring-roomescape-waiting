package roomescape.exception;

import static roomescape.service.ThemeService.MAX_RANKING_PERIOD;

public enum ErrorCode {

    NOT_FOUND_RESERVATION("[ERROR] 해당 ID의 예약을 찾을 수 없습니다."),
    DUPLICATED_RESERVATION("[ERROR] 해당 시간에 본인의 예약이 이미 존재합니다. 예약 가능한 시간으로 다시 시도해 주세요."),
    NOT_ALLOW_PAST_TIME_RESERVATION_CREATE("[ERROR] 지나간 시간에는 예약할 수 없습니다. 예약 시간을 변경해 주세요."),
    NOT_ALLOW_PAST_TIME_RESERVATION_UPDATE("[ERROR] 지나간 시간의 예약은 수정할 수 없습니다."),
    NOT_ALLOW_PAST_TIME_RESERVATION_DELETE("[ERROR] 지나간 시간의 예약은 삭제할 수 없습니다."),

    DUPLICATED_WAIT("[ERROR] 해당 시간에 이미 예약 대기를 신청했습니다. 다시 시도해 주세요."),
    WAIT_IS_FULL("[ERROR] 해당 시간에 이미 예약 대기가 가득 찼습니다. 다른 시간으로 예약해 주세요."),

    NOT_FOUND_RESERVATION_TIME("[ERROR] 해당 ID의 예약 시간을 찾을 수 없습니다."),
    DUPLICATED_RESERVATION_TIME("[ERROR] 동일한 예약 시간이 이미 존재합니다. 시간을 변경해 다시 시도해 주세요."),
    REFERENCED_TIME("[ERROR] 현재 해당 예약 시간을 사용하는 예약이 존재합니다. 연관된 예약을 삭제한 후 다시 시도해 주세요."),
    PAST_RESERVATION_TIME_READ("[ERROR] 지나간 날짜의 예약 가능 시간은 조회할 수 없습니다."),

    NOT_FOUND_THEME("[ERROR] 해당 ID의 테마를 찾을 수 없습니다."),
    REFERENCED_THEME("[ERROR] 현재 해당 테마를 사용하는 예약이 존재합니다. 연관된 예약을 삭제한 후 다시 시도해 주세요."),
    FUTURE_RANKING_PERIOD("[ERROR] 조회 기간에 미래 날짜가 존재합니다. 현재보다 이전으로 기간을 설정해 주세요."),
    INVALID_RANKING_PERIOD("[ERROR] 종료 날짜가 시작 날짜보다 빠릅니다. 종료 날짜가 시작 날짜보다 뒤에 오도록 요청해 주세요."),
    LONG_RANKING_PERIOD(
            String.format("[ERROR] 조회 기간이 최대 기간을 초과했습니다. 기간이 1년(%s일) 이내가 되도록 다시 요청해 주세요.", MAX_RANKING_PERIOD)),

    INVALID_METHOD_REQUEST("[ERROR] 지원하지 않는 메서드입니다. 메서드를 다시 한 번 확인해 주세요."),
    INVALID_URL_REQUEST("[ERROR] 잘못된 경로입니다. URL을 다시 한 번 확인해 주세요."),
    INVALID_JSON_REQUEST("[ERROR] 요청 본문(JSON)의 형식이 올바르지 않거나 읽을 수 없습니다. 데이터를 다시 확인해 주세요."),
    TYPE_MISMATCH_REQUEST("[ERROR] 요청 파라미터 또는 경로 변수의 타입이 올바르지 않습니다. 입력된 값을 다시 확인해 주세요."),

    INTERNAL_SERVER_ERROR("[ERROR] 서버 내부에서 에러가 발생했습니다."),

    NOT_ALLOW_DATE_TIME_NULL("[ERROR] 생성 날짜와 시간은 비어 있을 수 없습니다."),
    NOT_ALLOW_RESERVATION_NULL("[ERROR] 생성 날짜와 시간은 비어 있을 수 없습니다."),
    NOT_ALLOW_NAME_NULL("[ERROR] 이름은 비어 있을 수 없습니다."),
    NOT_ALLOW_DATE_NULL("[ERROR] 날짜는 비어 있을 수 없습니다."),
    NOT_ALLOW_TIME_NULL("[ERROR] 예약 시간은 비어 있을 수 없습니다."),
    NOT_ALLOW_THEME_NULL("[ERROR] 테마는 비어 있을 수 없습니다."),
    NOT_ALLOW_DESCRIPTION_NULL("[ERROR] 설명은 비어 있을 수 없습니다."),
    NOT_ALLOW_THUMBNAIL_NULL("[ERROR] 썸네일은 비어 있을 수 없습니다."),
    ;

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
