package roomescape.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ReservationTimeRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void 예약시간이_null이면_검증에_실패한다() {
        ReservationTimeRequest request = new ReservationTimeRequest(null);

        Set<ConstraintViolation<ReservationTimeRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
    }

}
