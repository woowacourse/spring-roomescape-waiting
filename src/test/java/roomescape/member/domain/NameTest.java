package roomescape.member.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("이름 도메인 검증")
class NameTest {
    @DisplayName("같은 이름은 같은 객체이다.")
    @Test
    void sameName() {
        //given
        String name1 = "choco";
        String name2 = "choco";

        //when & then
        assertThat(new Name(name1)).isEqualTo(new Name(name2));
    }
}
