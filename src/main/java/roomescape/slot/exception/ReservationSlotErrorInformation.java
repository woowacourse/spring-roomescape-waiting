package roomescape.slot.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import roomescape.common.exception.ErrorInformation;

@Getter
@AllArgsConstructor
public enum ReservationSlotErrorInformation implements ErrorInformation {

    SLOT_NOT_FOUND(HttpStatus.BAD_REQUEST, "SLOT_001", "존재하지 않는 슬롯입니다."),
    ;

    private final HttpStatus httpStatus;
    private final String errorCode;
    private final String message;

}
