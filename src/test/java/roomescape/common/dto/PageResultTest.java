package roomescape.common.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

class PageResultTest {

    @Test
    @DisplayName("페이지 정보를 계산한다")
    void create_page() {
        // given
        List<String> contents = List.of("예약3", "예약4");

        // when
        PageResult<String> pageResult = PageResult.of(contents, 2, 2, 5);

        // then
        assertThat(pageResult.contents()).containsExactly("예약3", "예약4");
        assertThat(pageResult.page()).isEqualTo(2);
        assertThat(pageResult.size()).isEqualTo(2);
        assertThat(pageResult.numberOfElements()).isEqualTo(2);
        assertThat(pageResult.totalElements()).isEqualTo(5);
        assertThat(pageResult.totalPages()).isEqualTo(3);
        assertThat(pageResult.hasPrevious()).isTrue();
        assertThat(pageResult.hasNext()).isTrue();
    }
}
