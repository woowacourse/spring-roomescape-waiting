package roomescape.learning;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionSubclassTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.test.context.TestPropertySource;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * 학습 테스트: DB가 던진 저수준 SQLException이 어떻게 Spring의 DuplicateKeyException으로 번역되는지 코드로 확인한다.
 *
 * <p>프로덕션 코드에서 한 일은 (name, reservation_id) UNIQUE 제약 한 줄과 그 예외를 409로 매핑한 핸들러뿐이다.
 * 에러 코드 23505를 보거나 SQLState를 파싱하는 코드는 한 줄도 없다. 그런데도 정확한 타입의 예외가 와 있는 이유를
 * 네 단계로 나눠 검증한다.
 *
 * <p>Spring 6.0부터 JdbcTemplate의 기본 번역기는 sql-error-codes.xml을 읽는 SQLErrorCodeSQLExceptionTranslator가
 * 아니라 JDBC 4 표준 예외 서브클래스를 보는 SQLExceptionSubclassTranslator다.
 *
 * @see org.springframework.jdbc.support.SQLExceptionSubclassTranslator
 */
@JdbcTest
@TestPropertySource(properties = "spring.sql.init.data-locations=")
class ExceptionTranslationLearningTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long reservationId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES ('10:00')");
        Long timeId = jdbcTemplate.queryForObject("SELECT id FROM reservation_time LIMIT 1", Long.class);

        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_image_url) VALUES (?, ?, ?)",
                "공포", "무서운 테마", "https://example.com/horror.jpg"
        );
        Long themeId = jdbcTemplate.queryForObject("SELECT id FROM theme LIMIT 1", Long.class);

        jdbcTemplate.update(
                "INSERT INTO reservation (name, date, time_id, theme_id, reservation_status) VALUES (?, ?, ?, ?, 'CONFIRM')",
                "티뉴", LocalDate.of(2026, 8, 5), timeId, themeId
        );
        reservationId = jdbcTemplate.queryForObject("SELECT id FROM reservation LIMIT 1", Long.class);
    }

    @Test
    void 중복_INSERT는_JdbcTemplate을_거치며_DuplicateKeyException으로_번역된다() {
        Throwable thrown = triggerDuplicateWaiting();

        assertThat(thrown).isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void 번역된_예외의_원인은_JDBC4_표준_제약위반_예외이고_sqlState가_23505임을_드러낸다() {
        Throwable thrown = triggerDuplicateWaiting();

        Throwable cause = thrown.getCause();
        assertThat(cause).isInstanceOf(SQLIntegrityConstraintViolationException.class);

        SQLException sqlException = (SQLException) cause;
        assertThat(sqlException.getErrorCode()).isEqualTo(23505);
        assertThat(sqlException.getSQLState()).isEqualTo("23505");
    }

    @Test
    void JdbcTemplate은_sql_error_codes_경로가_아니라_SQLExceptionSubclassTranslator를_사용한다() {
        SQLExceptionTranslator translator = jdbcTemplate.getExceptionTranslator();

        assertThat(translator)
                .isInstanceOf(SQLExceptionSubclassTranslator.class)
                .isNotInstanceOf(SQLErrorCodeSQLExceptionTranslator.class);
    }

    @Test
    void SQLExceptionSubclassTranslator는_DataSource_없이도_표준예외를_DuplicateKeyException으로_번역한다() {
        SQLException original = (SQLException) triggerDuplicateWaiting().getCause();

        SQLExceptionTranslator translator = new SQLExceptionSubclassTranslator();
        DataAccessException translated = translator.translate("learning", null, original);

        assertThat(translated).isInstanceOf(DuplicateKeyException.class);
    }

    private Throwable triggerDuplicateWaiting() {
        insertWaiting();
        return catchThrowable(this::insertWaiting);
    }

    private void insertWaiting() {
        jdbcTemplate.update(
                "INSERT INTO reservation_waiting (name, created_at, reservation_id) VALUES (?, ?, ?)",
                "민욱", LocalDateTime.of(2026, 8, 1, 10, 0), reservationId
        );
    }
}
