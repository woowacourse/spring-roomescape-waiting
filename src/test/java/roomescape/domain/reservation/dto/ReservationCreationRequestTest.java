package roomescape.domain.reservation.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ReservationCreationRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void memberId가_null이면_예외가_발생한다() {
        ReservationCreationRequest request = new ReservationCreationRequest(null, 1L, 1L, 1L);

        Set<ConstraintViolation<ReservationCreationRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations)
            .extracting(ConstraintViolation::getMessage)
            .contains("멤버 ID는 필수입니다");
    }

    @Test
    void 날짜가_null이면_예외가_발생한다() {
        ReservationCreationRequest request = new ReservationCreationRequest(1L, null, 1L, 1L);

        Set<ConstraintViolation<ReservationCreationRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations)
            .extracting(ConstraintViolation::getMessage)
            .contains("예약 날짜 선택은 필수입니다");
    }

    @Test
    void 시간_id가_null이면_예외가_발생한다() {
        ReservationCreationRequest request = new ReservationCreationRequest(1L, 1L, null, 1L);

        Set<ConstraintViolation<ReservationCreationRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations)
            .extracting(ConstraintViolation::getMessage)
            .contains("예약 시간 선택은 필수입니다");
    }

    @Test
    void 테마_id가_null이면_예외가_발생한다() {
        ReservationCreationRequest request = new ReservationCreationRequest(1L, 1L, 1L, null);

        Set<ConstraintViolation<ReservationCreationRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations)
            .extracting(ConstraintViolation::getMessage)
            .contains("테마 선택은 필수입니다");
    }
}
