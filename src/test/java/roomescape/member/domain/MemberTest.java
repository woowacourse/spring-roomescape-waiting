package roomescape.member.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemberTest {

    @DisplayName("필드 값을 바탕으로 동등한 멤버인지 확인한다")
    @Test
    void isEqual() {
        Member member1 = new Member("user", "test@gmail.com", "1234");
        Member member2 = new Member("user", "test@gmail.com", "1234");
        assertThat(member1.equals(member2)).isTrue();
    }
}
