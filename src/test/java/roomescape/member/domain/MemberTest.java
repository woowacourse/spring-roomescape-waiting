package roomescape.member.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorType;

@DisplayName("사용자 도메인 테스트")
class MemberTest {
    @DisplayName("동일한 id는 같은 사용자다.")
    @Test
    void equals() {
        //given
        long id1 = 1;
        String name1 = "name1";
        String name2 = "name2";

        //when
        Member member1 = new Member(id1, name1);
        Member member2 = new Member(id1, name2);

        //then
        assertThat(member1).isEqualTo(member2);
    }

    @DisplayName("한글, 영어 이외의 이름에 대해 예외가 발생한다.")
    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "$#@%!"})
    void invalidName(String invalidName) {
        //given
        long id = 1;

        //when & then
        assertThatThrownBy(() -> new Member(1L, invalidName))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorType.NAME_FORMAT_ERROR.getMessage());
    }
}
