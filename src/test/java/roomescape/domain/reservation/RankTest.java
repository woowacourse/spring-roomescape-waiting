package roomescape.domain.reservation;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class RankTest {

    @Test
    void 값이_1이면_True() {
        int input = 1;

        Rank rank = new Rank(1);

        assertThat(rank.isFirst()).isTrue();
    }

    @ValueSource(ints = {0, 2})
    @ParameterizedTest
    void 값이_1이_아니면_True(int value) {
        Rank rank = new Rank(value);

        assertThat(rank.isFirst()).isFalse();
    }

    @Test
    void 값이_1이면_상태는_승인이다() {
        int input = 1;

        Rank rank = new Rank(1);

        assertThat(rank.decideStatus()).isEqualTo(Status.APPROVED);
    }

    @ValueSource(ints = {0, 2})
    @ParameterizedTest
    void 값이_1이면_상태는_승인이다(int value) {
        Rank rank = new Rank(value);

        assertThat(rank.decideStatus()).isEqualTo(Status.WAITING);
    }
}