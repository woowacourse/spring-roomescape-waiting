package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class RankTest {
    @ValueSource(ints = {1, 2, 3})
    @ParameterizedTest
    void 순번_값을_반환한다(int value) {
        Rank rank = new Rank(value);

        assertThat(rank.getValue()).isEqualTo(value);
    }
}
