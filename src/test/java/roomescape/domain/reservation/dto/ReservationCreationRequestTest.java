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
    void 이름이_null이면_예외가_발생한다() {
        // given
        ReservationCreationRequest request = new ReservationCreationRequest(
            null,
            1L,
            1L,
            1L
        );

        // when
        Set<ConstraintViolation<ReservationCreationRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations)
            .extracting(ConstraintViolation::getMessage)
            .contains("예약자명은 필수입니다");
    }

    @Test
    void 이름이_공백이면_예외가_발생한다() {
        // given
        ReservationCreationRequest request = new ReservationCreationRequest(
            "   ",
            1L,
            1L,
            1L
        );

        // when
        Set<ConstraintViolation<ReservationCreationRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations)
            .extracting(ConstraintViolation::getMessage)
            .contains("예약자명은 필수입니다");
    }

    @Test
    void 날짜가_null이면_예외가_발생한다() {
        // given
        ReservationCreationRequest request = new ReservationCreationRequest(
            "보예",
            null,
            1L,
            1L
        );

        // when
        Set<ConstraintViolation<ReservationCreationRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations)
            .extracting(ConstraintViolation::getMessage)
            .contains("예약 날짜 선택은 필수입니다");
    }

    @Test
    void 시간_id가_null이면_예외가_발생한다() {
        // given
        ReservationCreationRequest request = new ReservationCreationRequest(
            "보예",
            1L,
            null,
            1L
        );

        // when
        Set<ConstraintViolation<ReservationCreationRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations)
            .extracting(ConstraintViolation::getMessage)
            .contains("예약 시간 선택은 필수입니다");
    }

    @Test
    void 테마_id가_null이면_예외가_발생한다() {
        // given
        ReservationCreationRequest request = new ReservationCreationRequest(
            "보예",
            1L,
            1L,
            null
        );

        // when
        Set<ConstraintViolation<ReservationCreationRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations)
            .extracting(ConstraintViolation::getMessage)
            .contains("테마 선택은 필수입니다");
    }
}
