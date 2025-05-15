package roomescape.unit.domain.member;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import roomescape.domain.member.MemberEncodedPassword;
import roomescape.domain.member.MemberPassword;

class MemberEncodedPasswordTest {

    @Test
    void 인코딩된_비밀번호는_null일_수_없다() {
        // when & then
        assertThatThrownBy(() -> new MemberEncodedPassword(null))
                .isInstanceOf(NullPointerException.class);
    }
}
