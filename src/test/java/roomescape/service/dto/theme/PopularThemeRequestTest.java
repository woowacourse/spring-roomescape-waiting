package roomescape.service.dto.theme;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PopularThemeRequestTest {

    private Validator validator;

    private final String validStartDate = "2024-12-20";
    private final String validEndDate = "2024-12-25";
    private final Integer validThemeCount = 10;

    @BeforeEach
    void setUp() {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @DisplayName("불러올 테마 개수가 입력되지 않으면 예외가 발생한다.")
    @Test
    void throw_exception_when_null_theme_count_input() {
        PopularThemeRequest requestDto = new PopularThemeRequest(validStartDate, validEndDate, null);

        Set<ConstraintViolation<PopularThemeRequest>> violations = validator.validate(requestDto);

        assertThat(violations).extracting("message").
                containsOnly("불러올 테마 개수는 반드시 입력되어야 합니다.");
    }

    @DisplayName("불러올 테마 개수가 자연수가 아니면 예외가 발생한다.")
    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void throw_exception_when_not_natural_theme_count_input(Integer themeCount) {
        PopularThemeRequest requestDto = new PopularThemeRequest(validStartDate, validEndDate, themeCount);

        Set<ConstraintViolation<PopularThemeRequest>> violations = validator.validate(requestDto);

        assertThat(violations).extracting("message").
                containsOnly("불러올 테마 개수는 자연수여야 합니다. " + themeCount + "은 사용할 수 없습니다.");
    }

    @DisplayName("잘못된 날짜 형식이 입력되면 예외가 발생한다.")
    @ParameterizedTest
    @ValueSource(strings = {"23", "1-12-20", "2014-11", "2015-13-01", "2016-02-30", "2019-09-31", "2022-05-00"})
    void throw_exception_when_invalid_date_format_input(String date) {
        PopularThemeRequest requestDto = new PopularThemeRequest(validStartDate, date, validThemeCount);

        Set<ConstraintViolation<PopularThemeRequest>> violations = validator.validate(requestDto);

        assertThat(violations).extracting("message").
                containsOnly("날짜 입력 형식이 올바르지 않습니다. ex) 1999-11-30");
    }

    @DisplayName("유효한 예약 입력 시 정상 생성된다.")
    @Test
    void create_success() {
        PopularThemeRequest requestDto = new PopularThemeRequest(validStartDate, validEndDate, validThemeCount);

        Set<ConstraintViolation<PopularThemeRequest>> violations = validator.validate(requestDto);

        assertThat(violations.size()).isEqualTo(0);
    }
}
