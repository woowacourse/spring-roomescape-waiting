package roomescape.controller.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationTimeRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void null로_생성시_예외() {
        // when
        Set<ConstraintViolation<ReservationTimeRequest>> result = validator.validate(
                new ReservationTimeRequest(null));

        // then
        assertThat(result).extracting(ConstraintViolation::getMessage)
                .containsExactly("startAt은 비어 있을 수 없습니다.");
    }

    @Test
    void 정상_생성_테스트() {
        // given
        LocalTime startAt = LocalTime.of(10, 0);

        // when
        ReservationTimeRequest result = new ReservationTimeRequest(startAt);

        // then
        assertThat(validator.validate(result)).isEmpty();
        assertThat(result.startAt()).isEqualTo(startAt);
    }
}
