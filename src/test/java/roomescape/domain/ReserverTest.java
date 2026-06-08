package roomescape.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReserverTest {

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " "})
    void 이름이_null_또는_blank이면_예외(String name) {
        // when & then
        assertThatThrownBy(() -> new Reserver(name))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("name은 비어 있을 수 없습니다.");
    }

    @Test
    void 이름이_255자를_초과하면_예외() {
        // given
        String name = "a".repeat(256);

        // when & then
        assertThatThrownBy(() -> new Reserver(name))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("name은 255자를 넘을 수 없습니다.");
    }

    @Test
    void 이름이_같으면_같은_예약자로_본다() {
        // when & then
        assertThat(new Reserver("브라운"))
                .isEqualTo(new Reserver("브라운"))
                .hasSameHashCodeAs(new Reserver("브라운"));
    }

    @Test
    void 정상_생성_테스트() {
        // given
        String name = "구구";

        // when
        Reserver reserver = new Reserver(name);

        // then
        assertThat(reserver.getName()).isEqualTo(name);
    }
}
