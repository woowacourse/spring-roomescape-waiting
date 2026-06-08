package roomescape.domain.policy;

import roomescape.domain.ReservationDateTime;

public interface ReservationPolicy {
    void validateCreatable(ReservationDateTime when);

    void validateCancellable(ReservationDateTime when);

    void validateUpdatable(ReservationDateTime when);   // 기존 예약 시점이 과거인지

    void validateUpdateTarget(ReservationDateTime when); // 변경하려는 새 시점이 과거인지
}
