package roomescape.exception;

public enum ErrorType {

    INVALID_DOMAIN("%s"),
    RESOURCE_NOT_FOUND("%s을(를) 찾을 수 없습니다. id=%s"),
    RESERVATION_OWNER_MISMATCH("본인의 예약만 취소 혹은 변경 가능합니다."),
    STORE_MANAGEMENT_FORBIDDEN("본인이 관리하는 매장의 예약만 관리할 수 있습니다."),
    DUPLICATE_RESERVATION("해당 날짜·시간·테마에 이미 예약이 존재합니다. 다른 날짜·시간·테마를 선택해주세요."),
    RESERVATION_TIME_IN_USE("예약이 존재하는 시간은 삭제할 수 없습니다."),
    PAST_DATE_TIME_RESERVATION("예약 일정이 유효하지 않습니다. 예약 날짜와 시간은 현시간 이후여야 합니다."),
    PAST_RESERVATION_MODIFICATION("이미 지난 예약은 수정할 수 없습니다."),
    NON_PAST_RESERVATION_DELETION("아직 지나지 않은 예약은 삭제할 수 없습니다."),
    DUPLICATE_USERNAME("이미 존재하는 username입니다. 다른 username을 입력해주세요."),
    INVALID_LOGIN("로그인 정보가 올바르지 않습니다. 아이디와 비밀번호를 확인해주세요."),
    UNAUTHENTICATED("인증이 필요합니다. 로그인 후 이용해주세요."),
    UNAUTHORIZED("접근 권한이 없습니다. 관리자만 이용할 수 있습니다."),
    RESERVATION_NOT_FOUND_FOR_WAITING("확정 예약이 없으므로 대기 예약 생성이 불가능합니다."),
    RESERVATION_NOT_RESERVED("해당 예약은 예약 확정 상태가 아닙니다. 현재 예약 상태 값: %s"),
    DUPLICATE_WAITING_RESERVATION("이미 해당 슬롯에 예약 대기 중입니다."),

    INVALID_REQUEST("%s"),
    ENDPOINT_NOT_FOUND("요청한 경로를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR("알 수 없는 서버 에러가 발생했습니다. 잠시 후 다시 시도해주세요.");

    private final String messageFormat;

    ErrorType(String messageFormat) {
        this.messageFormat = messageFormat;
    }

    public String format(Object... args) {
        return String.format(messageFormat, args);
    }
}
