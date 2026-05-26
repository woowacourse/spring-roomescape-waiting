package roomescape.support.exception.errors;

import lombok.Getter;

@Getter
public enum ReservationErrors implements Errors {

    INVALID_RESERVATION_NAME("이름은 비어 있을 수 없습니다."),
    INVALID_RESERVATION_NAME_LENGTH("이름은 10자 이하여야 합니다."),
    INVALID_RESERVATION_DATE("날짜는 필수입니다."),
    RESERVATION_NOT_FOUND("존재하지 않는 예약건 입니다"),
    DUPLICATED_RESERVATION("중복 예약입니다. 예약 정보를 다시 확인해주세요."),
    INVALID_RESERVATION_UPDATE_REQUEST("수정할 예약 날짜 또는 시간을 입력해주세요."),
    ;

    private final String message;

    ReservationErrors(String message) {
        this.message = message;
    }

    @Override
    public String getCode() {
        return name();
    }
}
