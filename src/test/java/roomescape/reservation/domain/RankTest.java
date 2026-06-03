package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
}
