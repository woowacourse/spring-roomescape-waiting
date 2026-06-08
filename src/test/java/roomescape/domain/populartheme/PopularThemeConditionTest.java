package roomescape.domain.populartheme;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class PopularThemeConditionTest {

    @Test
    void 인기_테마_조건_생성_성공_테스트() {
        // given
        LocalDate startDate = LocalDate.of(2026, 5, 27);
        LocalDate endDate = LocalDate.of(2026, 6, 2);

        // when
        PopularThemeCondition result = new PopularThemeCondition(startDate, endDate, 10);

        // then
        assertAll(
                () -> assertThat(result.getStartDate()).isEqualTo(startDate),
                () -> assertThat(result.getEndDate()).isEqualTo(endDate),
                () -> assertThat(result.getLimit()).isEqualTo(10));
    }

    @Test
    void 시작_날짜가_null이면_예외() {
        // when & then
        assertThatThrownBy(() -> new PopularThemeCondition(null, LocalDate.of(2026, 6, 2), 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("날짜는 비어 있을 수 없습니다.");
    }

    @Test
    void 종료_날짜가_null이면_예외() {
        // when & then
        assertThatThrownBy(() -> new PopularThemeCondition(LocalDate.of(2026, 5, 27), null, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("날짜는 비어 있을 수 없습니다.");
    }

    @Test
    void 시작_날짜가_종료_날짜보다_늦으면_예외() {
        // when & then
        assertThatThrownBy(() -> new PopularThemeCondition(
                LocalDate.of(2026, 6, 3),
                LocalDate.of(2026, 6, 2),
                10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("시작 날짜는 종료 날짜보다 늦을 수 없습니다.");
    }

    @Test
    void 조회_개수가_양수가_아니면_예외() {
        // when & then
        assertThatThrownBy(() -> new PopularThemeCondition(
                LocalDate.of(2026, 5, 27),
                LocalDate.of(2026, 6, 2),
                0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("limit은 양수이어야 합니다.");
    }
}
