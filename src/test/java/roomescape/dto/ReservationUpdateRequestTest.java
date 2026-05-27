package roomescape.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ReservationUpdateRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void 날짜가_null이면_검증에_실패한다() {
        ReservationUpdateRequest request = new ReservationUpdateRequest(null, 1L);

        Set<ConstraintViolation<ReservationUpdateRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
    }

    @Test
    void 예약시간이_null이면_검증에_실패한다() {
        ReservationUpdateRequest request = new ReservationUpdateRequest(LocalDate.now().plusDays(1), null);

        Set<ConstraintViolation<ReservationUpdateRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
    }
}
