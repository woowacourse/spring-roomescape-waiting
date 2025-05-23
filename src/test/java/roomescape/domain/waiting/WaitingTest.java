package roomescape.domain.waiting;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static roomescape.DomainFixtures.JUNK_THEME;
import static roomescape.DomainFixtures.JUNK_TIME_SLOT;
import static roomescape.DomainFixtures.JUNK_USER;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.DateUtils;
import roomescape.exception.BusinessRuleViolationException;

class WaitingTest {

    @Test
    @DisplayName("예약 대기 생성 일시가 현재 일시보다 이전이면 예외가 발생한다.")
    void isBefore() {
        // given
        var yesterday = DateUtils.yesterday();

        // when & then
        assertThatThrownBy(
                () -> Waiting.register(JUNK_USER, yesterday, JUNK_TIME_SLOT, JUNK_THEME))
                .isInstanceOf(BusinessRuleViolationException.class);
    }
}
