package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static roomescape.domain.reservation.UserName.NAME_MAX_LENGTH;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserNameTest {

    @Test
    @DisplayName("올바른 정보로 예약을 생성하면 성공한다.")
    void 이름_생성_테스트() {
        String userName = "브라운";

        assertDoesNotThrow(() -> new UserName(userName));
    }

    @Test
    @DisplayName("예약자 이름의 값이 없으면 예외가 발생한다.")
    void 이름_빈칸_예외_테스트() {
        String userName = "";

        assertThatThrownBy(() -> new UserName(userName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약자 이름은 비어 있을 수 없습니다.");
    }

    @Test
    @DisplayName("예약자 이름 글자 수 제한을 초과하면 예외가 발생한다.")
    void 이름_글자_수_초과_예외_테스트() {
        String userName = "0".repeat(NAME_MAX_LENGTH+1);

        assertThatThrownBy(() -> new UserName(userName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약자 이름은 %d자를 초과할 수 없습니다.".formatted(NAME_MAX_LENGTH));
    }
}
