package roomescape.waiting;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class WaitingTest {

    @Test
    @DisplayName("대기자가 같으면 본인 대기다.")
    void 대기_테스트_1() {
        Waiting waiting = Waiting.of(1L, 1L, 1L);

        assertThat(waiting.isOwnedBy(1L)).isTrue();
    }

    @Test
    @DisplayName("대기자가 다르면 본인 대기가 아니다.")
    void 대기_테스트_2() {
        Waiting waiting = Waiting.of(1L, 1L, 1L);

        assertThat(waiting.isOwnedBy(2L)).isFalse();
    }
}
