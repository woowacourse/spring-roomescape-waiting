package roomescape.service.dto.time;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.service.dto.reservation.ReservationTimeRequest;

class ReservationTimeRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @DisplayName("잘못된 형식이 입력되면 예외가 발생한다.")
    @ParameterizedTest
    @ValueSource(strings = {"23", "25:00", "3:3", "3시 25분", "03:60"})
    void throw_exception_when_invalid_time_format_input(String startAt) {
        ReservationTimeRequest requestDto = new ReservationTimeRequest(startAt);

        Set<ConstraintViolation<ReservationTimeRequest>> violations = validator.validate(requestDto);

        assertThat(violations).extracting("message").
                containsOnly("시간 입력 형식이 올바르지 않습니다. ex) 13:40");
    }

    @DisplayName("유효한 시간 입력 시 정상 생성된다.")
    @ParameterizedTest
    @ValueSource(strings = {"00:00", "23:59", "05:05"})
    void create_success(String startAt) {
        ReservationTimeRequest requestDto = new ReservationTimeRequest(startAt);

        Set<ConstraintViolation<ReservationTimeRequest>> violations = validator.validate(requestDto);

        assertThat(violations.size()).isEqualTo(0);
    }
}
