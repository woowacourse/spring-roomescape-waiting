package roomescape.infrastructure.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DynamicQueryBuilderTest {

    @Test
    @DisplayName("쿼리가 올바르게 생성된다.")
    void createQueryTest() {
        DynamicQueryBuilder query = DynamicQueryBuilder.where()
                .equals("member_id", 1)
                .lessOrEqualThan("age", 20)
                .greaterOrEqualThan("age", 10);

        String expected = "WHERE member_id = ? AND age <= ? AND age >= ?";
        String actual = query.toSql();
        assertThat(actual).isEqualToIgnoringCase(expected);
    }

    @Test
    @DisplayName("비교값이 null인 경우, 쿼리에 포함되지 않는다.")
    void createQueryOnNullParameterTest() {
        DynamicQueryBuilder query = DynamicQueryBuilder.where()
                .equals("member_id", 1)
                .lessOrEqualThan("age", null)
                .greaterOrEqualThan("age", 10);

        String expected = "WHERE member_id = ? AND age >= ?";
        String actual = query.toSql();
        assertThat(actual).isEqualToIgnoringCase(expected);
    }

    @Test
    @DisplayName("파라미터를 올바르게 반환한다.")
    void queryParameterTest() {
        DynamicQueryBuilder query = DynamicQueryBuilder.where()
                .equals("member_id", 1)
                .lessOrEqualThan("age", "20")
                .greaterOrEqualThan("age", 10);

        Object[] expected = {1, "20", 10};
        Object[] actual = query.getParameters();
        assertThat(actual).containsExactly(expected);
    }
}
