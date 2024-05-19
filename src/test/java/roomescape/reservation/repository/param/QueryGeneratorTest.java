package roomescape.reservation.repository.param;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.reservation.dto.SearchReservationsParams;

import java.time.LocalDate;

class QueryGeneratorTest {

    @DisplayName("회원 id, 테마 id, 시작일, 종료일을 모두 검색하는 쿼리를 생성한다.")
    @Test
    void generateAllSearchQuery() {
        // Given
        final long memberId = 1L;
        final long themeId = 3L;
        final LocalDate from = LocalDate.now().minusDays(4);
        final LocalDate to = LocalDate.now().plusDays(1);
        final SearchReservationsParams searchReservationsParams = new SearchReservationsParams(memberId, themeId, from, to);
        final String baseQuery = "SELECT * FROM member";

        final String expect = "SELECT * FROM member " +
                "WHERE member_id = " + memberId + " " +
                "AND theme_id = " + themeId + " " +
                "AND date BETWEEN \'" + from + "\' " +
                "AND \'" + to + "\'";

        // When
        final String generateSql = QueryGenerator.generateQueryWithSearchReservationsParams(searchReservationsParams, baseQuery);

        // Then
        Assertions.assertThat(generateSql).isEqualTo(expect);
    }

    @DisplayName("테마 id, 시작일, 종료일을 모두 검색하는 쿼리를 생성한다.")
    @Test
    void generateThemeIdAndFromDateAndToDateSearchQuery() {
        // Given
        final long themeId = 3L;
        final LocalDate from = LocalDate.now().minusDays(4);
        final LocalDate to = LocalDate.now().plusDays(1);
        final SearchReservationsParams searchReservationsParams = new SearchReservationsParams(null, themeId, from, to);
        final String baseQuery = "SELECT * FROM member";

        final String expect = "SELECT * FROM member " +
                "WHERE theme_id = " + themeId + " " +
                "AND date BETWEEN \'" + from + "\' " +
                "AND \'" + to + "\'";

        // When
        final String generateSql = QueryGenerator.generateQueryWithSearchReservationsParams(searchReservationsParams, baseQuery);

        // Then
        Assertions.assertThat(generateSql).isEqualTo(expect);
    }

    @DisplayName("시작일, 종료일을 모두 검색하는 쿼리를 생성한다.")
    @Test
    void generateFromDateAndToDateSearchQuery() {
        // Given
        final LocalDate from = LocalDate.now().minusDays(4);
        final LocalDate to = LocalDate.now().plusDays(1);
        final SearchReservationsParams searchReservationsParams = new SearchReservationsParams(null, null, from, to);
        final String baseQuery = "SELECT * FROM member";

        final String expect = "SELECT * FROM member " +
                "WHERE date BETWEEN \'" + from + "\' " +
                "AND \'" + to + "\'";

        // When
        final String generateSql = QueryGenerator.generateQueryWithSearchReservationsParams(searchReservationsParams, baseQuery);

        // Then
        Assertions.assertThat(generateSql).isEqualTo(expect);
    }

    @DisplayName("시작일 이후를 모두 검색하는 쿼리를 생성한다.")
    @Test
    void generateAfterFromDateSearchQuery() {
        // Given
        final LocalDate from = LocalDate.now().minusDays(4);
        final SearchReservationsParams searchReservationsParams = new SearchReservationsParams(null, null, from, null);
        final String baseQuery = "SELECT * FROM member";

        final String expect = "SELECT * FROM member " +
                "WHERE date >= \'" + from + "\'";

        // When
        final String generateSql = QueryGenerator.generateQueryWithSearchReservationsParams(searchReservationsParams, baseQuery);

        // Then
        Assertions.assertThat(generateSql).isEqualTo(expect);
    }
}
