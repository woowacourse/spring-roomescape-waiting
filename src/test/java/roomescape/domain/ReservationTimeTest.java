package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.exception.InvalidDomainException;

/**
 * ReservationTime 도메인 단위 테스트.
 * 보호 대상: 시간 객체의 정합성(null 금지). 단순하지만 도메인 불변식이므로 단위로 검증한다.
 */
class ReservationTimeTest {

    @Test
    @DisplayName("시작 시간이 null이면 예외")
    void 시작시간_null() {
        assertThatThrownBy(() -> ReservationTime.create(null))
                .isInstanceOf(InvalidDomainException.class)
                .hasMessage("예약 시간은 비어 있을 수 없습니다.");
    }

    @Test
    @DisplayName("유효한 시작 시간은 허용")
    void 정상_생성() {
        assertThatCode(() -> ReservationTime.create(LocalTime.of(10, 0)))
                .doesNotThrowAnyException();
    }
}
