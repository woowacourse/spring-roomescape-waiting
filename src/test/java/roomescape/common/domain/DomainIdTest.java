package roomescape.common.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.validate.InvalidInputException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class DomainIdTest {

    @Test
    @DisplayName("할당된 ID가 null이면 예외가 발생한다")
    void validateNullAssignedId() {
        // when
        // then
        assertThatThrownBy(() -> new TestDomainId(null))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Validation failed [while checking null]: DomainId.value");
    }

    @Test
    @DisplayName("할당되지 않은 ID는 null이어도 예외가 발생하지 않는다")
    void validateNullUnassignedId() {
        // when
        // then
        assertDoesNotThrow(() -> new TestDomainId(null));
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

    @Test
    @DisplayName("할당되지 않은 ID는 값을 조회할 수 없다")
    void getValueFromUnassignedId() {
        // given
        final TestDomainId domainId = new TestDomainId(null);

        // when
        // then
        assertThatThrownBy(domainId::getValue)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("식별자가 할당되지 않았습니다.");
    }

    // DomainId를 테스트하기 위한 구체 클래스
    private static class TestDomainId extends DomainId {

        public TestDomainId(final Long value) {
            super(value);
        }
    }
}
