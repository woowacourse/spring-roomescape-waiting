package roomescape.common.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.validate.InvalidArgumentException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DomainIdTest {

    @Test
    @DisplayName("할당된 ID가 null이면 예외가 발생한다")
    void validateNullAssignedId() {
        // when
        // then
        assertThatThrownBy(() -> new TestDomainId(null))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessage("Validation failed [while checking null]: DomainId.value");
    }

    @Test
    @DisplayName("할당된 ID는 값을 조회할 수 있다")
    void getValueFromAssignedId() {
        // given
        final TestDomainId domainId = new TestDomainId(1L);

        // when
        // then
        assertThat(domainId.getValue()).isEqualTo(1L);
    }

    // DomainId를 테스트하기 위한 구체 클래스
    private static class TestDomainId extends DomainId {

        public TestDomainId(final Long value) {
            super(value);
        }
    }
}
