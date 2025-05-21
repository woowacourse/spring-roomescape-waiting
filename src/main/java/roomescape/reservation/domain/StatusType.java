package roomescape.reservation.domain;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatusType {
    RESERVED("예약"),
    WAITING("예약대기"),
    ;

    private final String value;
}
