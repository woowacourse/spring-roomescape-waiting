package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WaitingNumberTest {

    @Test
    @DisplayName("대기 순번을 생성한다.")
    void 대기_순번_생성() {
        WaitingNumber waitingNumber = new WaitingNumber(1);

        assertThat(waitingNumber.value()).isEqualTo(1);
    }

    @Test
    @DisplayName("컬렉션의 위치를 대기 순번으로 변환한다.")
    void 인덱스로_대기_순번_생성() {
        WaitingNumber first = WaitingNumber.fromIndex(0);
        WaitingNumber second = WaitingNumber.fromIndex(1);

        assertThat(first.value()).isEqualTo(1);
        assertThat(second.value()).isEqualTo(2);
    }

    @Test
    @DisplayName("대기 순번은 1 이상이어야 한다.")
    void 대기_순번_생성_검증() {
        assertThatThrownBy(() -> new WaitingNumber(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("대기 순번은 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("음수 인덱스로 대기 순번을 생성하면 예외가 발생한다.")
    void 음수_인덱스_대기_순번_생성_예외() {
        assertThatThrownBy(() -> WaitingNumber.fromIndex(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("대기 인덱스는 0 이상이어야 합니다.");
    }
}
