package roomescape.auth.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorType;

@DisplayName("페이로드 테스트")
class PayloadTest {
    @DisplayName("페이로드 생성에 성공한다.")
    @Test
    void create() {
        //given
        String name = "초코칩";

        //when
        Payload<String> payload = new Payload<>(name, () -> true);

        //then
        assertThat(payload.getValue()).isEqualTo(name);
    }

    @DisplayName("페이로드 조회 시, 검증에 실패하면 예외가 발생한다.")
    @Test
    void getValueWithException() {
        //given
        String name1 = "초코칩";
        String name2 = "초코칩2";

        //when
        Payload<String> payload1 = new Payload<>(name1, () -> isSameName(name1, "초코칩"));
        Payload<String> payload2 = new Payload<>(name2, () -> isSameName(name2, "초코칩"));

        //then
        assertThat(payload1.getValue()).isEqualTo(name1);
        assertThatThrownBy(payload2::getValue)
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorType.SECURITY_EXCEPTION.getMessage());
    }

    private boolean isSameName(String name1, String name2){
        return name1.equals(name2);
    }
}
