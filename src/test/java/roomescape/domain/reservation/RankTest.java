package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class RankTest {
    @Test
    void 첫번째_순서라면_승인을_반환한다() {
        Rank rank = new Rank(1);

        assertThat(rank.decideStatus()).isEqualTo(Status.APPROVED);
    }

    @ValueSource(ints = {0, 2})
    @ParameterizedTest
    void 첫번째_순서가_아니면_대기를_반환한다(int value) {
        Rank rank = new Rank(value);

        assertThat(rank.decideStatus()).isEqualTo(Status.WAITING);
    }
}