package roomescape.domain.reservation;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Status {
    APPROVED("승인"),
    WAITING("대기");

    private final String koreanName;
}
