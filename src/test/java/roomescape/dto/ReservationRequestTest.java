package roomescape.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ReservationRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void 이름이_비어_있으면_검증에_실패한다() {
        ReservationRequest request = new ReservationRequest(
            "",
            LocalDate.now().plusDays(1),
            1L,
            1L
        );

        Set<ConstraintViolation<ReservationRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
    }

    @Test
    void 날짜가_null이면_검증에_실패한다() {
        ReservationRequest request = new ReservationRequest(
            "브라운",
            null,
            1L,
            1L
        );

        Set<ConstraintViolation<ReservationRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
    }

    @Test
    void 예약시간_ID가_null이면_검증에_실패한다() {
        ReservationRequest request = new ReservationRequest(
            "브라운",
            LocalDate.now().plusDays(1),
            null,
            1L
        );

        Set<ConstraintViolation<ReservationRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
    }

    @Test
    void 테마_ID_null이면_검증에_실패한다() {
        ReservationRequest request = new ReservationRequest(
            "브라운",
            LocalDate.now().plusDays(1),
            1L,
            null
        );

        Set<ConstraintViolation<ReservationRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
    }

}
