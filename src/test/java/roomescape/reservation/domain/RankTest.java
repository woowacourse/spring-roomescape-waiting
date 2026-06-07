package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.global.exception.RoomEscapeException;

class RankTest {

    @DisplayName("대기 순번 생성을 테스트합니다.")
    @Test
    void create_rank() {
        Rank rank = Rank.builder()
                .value(1)
                .build();

        assertThat(rank.value()).isEqualTo(1);
    }

    @DisplayName("양수가 아닌 대기 순번 생성 시 예외를 테스트합니다.")
    @Test
    void create_non_positive_rank_exception() {
        assertThatThrownBy(() -> Rank.builder()
                .value(0)
                .build())
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("대기 순번은 양수여야 합니다.");
    }

    @DisplayName("대기 순번 미루기를 테스트합니다.")
    @Test
    void postpone_rank() {
        Rank rank = Rank.builder()
                .value(1)
                .build();

        Rank postponedRank = rank.postpone(2, 4);

        assertThat(postponedRank.value()).isEqualTo(3);
    }

    @DisplayName("대기 순번을 전체 대기 수보다 뒤로 미루면 마지막 순번으로 이동하는 것을 테스트합니다.")
    @Test
    void postpone_rank_to_last_rank() {
        Rank rank = Rank.builder()
                .value(1)
                .build();

        Rank postponedRank = rank.postpone(99, 4);

        assertThat(postponedRank.value()).isEqualTo(4);
    }

    @DisplayName("양수가 아닌 순번으로 대기 순번을 미룰 시 예외를 테스트합니다.")
    @ValueSource(ints = {0, -1, -5})
    @ParameterizedTest
    void postpone_rank_with_non_positive_steps_exception(int steps) {
        Rank rank = Rank.builder()
                .value(1)
                .build();

        assertThatThrownBy(() -> rank.postpone(steps, 4))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("미룰 순번은 양수여야 합니다.");
    }

    @DisplayName("가장 마지막 대기 순번이 대기를 미룰 시 예외를 테스트합니다.")
    @Test
    void postpone_last_rank_exception() {
        Rank rank = Rank.builder()
                .value(4)
                .build();

        assertThatThrownBy(() -> rank.postpone(1, 4))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("마지막 대기 순번은 대기를 미룰 수 없습니다.");
    }
}
