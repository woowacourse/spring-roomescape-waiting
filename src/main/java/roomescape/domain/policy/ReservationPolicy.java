package roomescape.domain.policy;

import java.time.LocalDate;
import java.time.LocalTime;

public interface ReservationPolicy {
    void validateCreatable(LocalDate date, LocalTime time);

    void validateCancellable(LocalDate date, LocalTime time);

    void validateUpdatable(LocalDate date, LocalTime time);  // 기존 예약이 과거인지

    void validateUpdateTarget(LocalDate date, LocalTime time);// 새 날짜, 시간이 과거인지
}
