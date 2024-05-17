package roomescape.member.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemberTest {

    @Test
    @DisplayName("Default 이름을 집어넣는다.")
    void shouldUseDefaultName() {
        Member member = Member.of("polla@gmail.com", "opolla09");

        assertEquals(member.getName(), "어드민");
    }
}
