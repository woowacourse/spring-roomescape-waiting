package roomescape.controller.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReservationRequestTest {

    private static final String VALID_NAME = "브라운";
    private static final LocalDate VALID_DATE = LocalDate.of(2026, 5, 9);
    private static final Long VALID_TIME_ID = 1L;
    private static final Long VALID_THEME_ID = 1L;

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    @DisplayName("모든 값이 유효하면 위반이 없다")
    void 모든_값이_유효하면_위반이_없다() {
        ReservationRequest request = new ReservationRequest(VALID_NAME, VALID_DATE, VALID_TIME_ID, VALID_THEME_ID);

        Set<ConstraintViolation<ReservationRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("이름이 null이면 위반이 발생한다")
    void 이름이_null이면_위반이_발생한다() {
        ReservationRequest request = new ReservationRequest(null, VALID_DATE, VALID_TIME_ID, VALID_THEME_ID);

        Set<ConstraintViolation<ReservationRequest>> violations = validator.validate(request);

        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("예약자 이름은 필수입니다");
    }

    @Test
    @DisplayName("이름이 빈문자열이면 위반이 발생한다")
    void 이름이_빈문자열이면_위반이_발생한다() {
        ReservationRequest request = new ReservationRequest("", VALID_DATE, VALID_TIME_ID, VALID_THEME_ID);

        Set<ConstraintViolation<ReservationRequest>> violations = validator.validate(request);

        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("예약자 이름은 필수입니다");
    }

    @Test
    @DisplayName("이름이 공백만으로 이루어져 있으면 위반이 발생한다")
    void 이름이_공백만으로_이루어져_있으면_위반이_발생한다() {
        ReservationRequest request = new ReservationRequest("   ", VALID_DATE, VALID_TIME_ID, VALID_THEME_ID);

        Set<ConstraintViolation<ReservationRequest>> violations = validator.validate(request);

        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("예약자 이름은 필수입니다");
    }

    @Test
    @DisplayName("날짜가 null이면 위반이 발생한다")
    void 날짜가_null이면_위반이_발생한다() {
        ReservationRequest request = new ReservationRequest(VALID_NAME, null, VALID_TIME_ID, VALID_THEME_ID);

        Set<ConstraintViolation<ReservationRequest>> violations = validator.validate(request);

        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("예약 날짜는 필수입니다");
    }

    @Test
    @DisplayName("시간 ID가 null이면 위반이 발생한다")
    void 시간_ID가_null이면_위반이_발생한다() {
        ReservationRequest request = new ReservationRequest(VALID_NAME, VALID_DATE, null, VALID_THEME_ID);

        Set<ConstraintViolation<ReservationRequest>> violations = validator.validate(request);

        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("시간 ID는 필수입니다");
    }

    @Test
    @DisplayName("시간 ID가 0이면 위반이 발생한다")
    void 시간_ID가_0이면_위반이_발생한다() {
        ReservationRequest request = new ReservationRequest(VALID_NAME, VALID_DATE, 0L, VALID_THEME_ID);

        Set<ConstraintViolation<ReservationRequest>> violations = validator.validate(request);

        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("시간 ID는 1 이상이여야 합니다");
    }

    @Test
    @DisplayName("시간 ID가 음수이면 위반이 발생한다")
    void 시간_ID가_음수이면_위반이_발생한다() {
        ReservationRequest request = new ReservationRequest(VALID_NAME, VALID_DATE, -1L, VALID_THEME_ID);

        Set<ConstraintViolation<ReservationRequest>> violations = validator.validate(request);

        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("시간 ID는 1 이상이여야 합니다");
    }

    @Test
    @DisplayName("테마 ID가 null이면 위반이 발생한다")
    void 테마_ID가_null이면_위반이_발생한다() {
        ReservationRequest request = new ReservationRequest(VALID_NAME, VALID_DATE, VALID_TIME_ID, null);

        Set<ConstraintViolation<ReservationRequest>> violations = validator.validate(request);

        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("테마 ID는 필수입니다");
    }

    @Test
    @DisplayName("테마 ID가 0이면 위반이 발생한다")
    void 테마_ID가_0이면_위반이_발생한다() {
        ReservationRequest request = new ReservationRequest(VALID_NAME, VALID_DATE, VALID_TIME_ID, 0L);

        Set<ConstraintViolation<ReservationRequest>> violations = validator.validate(request);

        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("테마 ID는 1 이상이여야 합니다");
    }

    @Test
    @DisplayName("테마 ID가 음수이면 위반이 발생한다")
    void 테마_ID가_음수이면_위반이_발생한다() {
        ReservationRequest request = new ReservationRequest(VALID_NAME, VALID_DATE, VALID_TIME_ID, -1L);

        Set<ConstraintViolation<ReservationRequest>> violations = validator.validate(request);

        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("테마 ID는 1 이상이여야 합니다");
    }
}
