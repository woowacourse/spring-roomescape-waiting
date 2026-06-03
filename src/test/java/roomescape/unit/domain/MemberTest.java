package roomescape.unit.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import roomescape.domain.Member;
import roomescape.domain.exception.BusinessRuleViolationException;

class MemberTest {

    @Test
    void 이름은_앞뒤_공백을_제거한다() {
        Member member = new Member(" 민욱 ");

        assertThat(member.name()).isEqualTo("민욱");
    }

    @Test
    void 이름은_10자까지_허용한다() {
        assertThatCode(() -> new Member("1234567890"))
                .doesNotThrowAnyException();
    }

    @Test
    void 이름이_비어_있으면_생성할_수_없다() {
        assertThatThrownBy(() -> new Member(" "))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("사용자 이름");
    }

    @Test
    void 이름이_10자를_초과하면_생성할_수_없다() {
        assertThatThrownBy(() -> new Member("12345678901"))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("사용자 이름");
    }
}
