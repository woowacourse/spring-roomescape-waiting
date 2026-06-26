package roomescape.controller.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.LocalTime;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReservationTimeRequestTest {

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
    @DisplayName("시작 시간이 유효하면 위반이 없다")
    void 시작_시간이_유효하면_위반이_없다() {
        ReservationTimeRequest request = new ReservationTimeRequest(LocalTime.of(10, 0));

        Set<ConstraintViolation<ReservationTimeRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("시작 시간이 null이면 위반이 발생한다")
    void 시작_시간이_null이면_위반이_발생한다() {
        ReservationTimeRequest request = new ReservationTimeRequest(null);

        Set<ConstraintViolation<ReservationTimeRequest>> violations = validator.validate(request);

        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("예약 시간은 필수입니다");
    }
}
