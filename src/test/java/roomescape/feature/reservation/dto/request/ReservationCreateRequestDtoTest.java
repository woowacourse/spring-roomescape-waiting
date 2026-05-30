package roomescape.feature.reservation.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ReservationCreateRequestDtoTest {

    private static final LocalDate VALID_DATE = LocalDate.of(2099, 5, 1);
    private static final LocalDate PAST_DATE = LocalDate.of(2000, 5, 1);

    private final Validator validator = Validation
        .buildDefaultValidatorFactory()
        .getValidator();

    @Nested
    class Success {

        @Test
        void 예약_생성_요청_Dto가_유효하면_검증_오류가_없다() {
            // given
            ReservationCreateRequestDto request = new ReservationCreateRequestDto(
                "예약자",
                VALID_DATE,
                1L,
                1L
            );

            // when
            Set<ConstraintViolation<ReservationCreateRequestDto>> violations =
                validator.validate(request);

            // then
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    class Failed {

        @Test
        void 예약_날짜가_null이면_검증_오류가_발생한다() {
            // given
            ReservationCreateRequestDto request = new ReservationCreateRequestDto(
                "예약자",
                null,
                1L,
                1L
            );

            // when
            Set<String> violationMessages = validate(request);

            // then
            assertThat(violationMessages).contains("예약 날짜는 필수입니다.");
        }

        @Test
        void 예약_날짜가_과거이면_검증_오류가_발생한다() {
            // given
            ReservationCreateRequestDto request = new ReservationCreateRequestDto(
                "예약자",
                PAST_DATE,
                1L,
                1L
            );

            // when
            Set<String> violationMessages = validate(request);

            // then
            assertThat(violationMessages).contains("예약 날짜가 현재보다 과거입니다.");
        }

        @Test
        void timeId가_null이면_검증_오류가_발생한다() {
            // given
            ReservationCreateRequestDto request = new ReservationCreateRequestDto(
                "예약자",
                VALID_DATE,
                null,
                1L
            );

            // when
            Set<String> violationMessages = validate(request);

            // then
            assertThat(violationMessages).contains("timeId는 필수입니다.");
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -1L})
        void timeId가_양수가_아니면_검증_오류가_발생한다(Long timeId) {
            // given
            ReservationCreateRequestDto request = new ReservationCreateRequestDto(
                "예약자",
                VALID_DATE,
                timeId,
                1L
            );

            // when
            Set<String> violationMessages = validate(request);

            // then
            assertThat(violationMessages).contains("timeId의 값이 유효하지 않습니다.");
        }

        @Test
        void themeId가_null이면_검증_오류가_발생한다() {
            // given
            ReservationCreateRequestDto request = new ReservationCreateRequestDto(
                "예약자",
                VALID_DATE,
                1L,
                null
            );

            // when
            Set<String> violationMessages = validate(request);

            // then
            assertThat(violationMessages).contains("themeId는 필수입니다.");
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -1L})
        void themeId가_양수가_아니면_검증_오류가_발생한다(Long themeId) {
            // given
            ReservationCreateRequestDto request = new ReservationCreateRequestDto(
                "예약자",
                VALID_DATE,
                1L,
                themeId
            );

            // when
            Set<String> violationMessages = validate(request);

            // then
            assertThat(violationMessages).contains("themeId의 값이 유효하지 않습니다.");
        }
    }

    private Set<String> validate(ReservationCreateRequestDto request) {
        Set<ConstraintViolation<ReservationCreateRequestDto>> violations =
            validator.validate(request);

        return violations.stream()
            .map(ConstraintViolation::getMessage)
            .collect(java.util.stream.Collectors.toSet());
    }
}
