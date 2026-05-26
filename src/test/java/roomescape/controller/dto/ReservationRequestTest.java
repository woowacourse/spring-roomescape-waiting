package roomescape.controller.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class ReservationRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private final LocalDate date = LocalDate.parse("2026-05-02");

    @Test
    void 이름이_null이면_예외() {
        // when
        Set<ConstraintViolation<ReservationRequest>> result = validator.validate(
                new ReservationRequest(null, date, 1L, 1L));

        // then
        assertThat(result).extracting(ConstraintViolation::getMessage)
                .containsExactly("name은 비어 있을 수 없습니다.");
    }

    @Test
    void 이름이_255자를_초과하면_예외() {
        // given
        String name = "a".repeat(256);

        // when
        Set<ConstraintViolation<ReservationRequest>> result = validator.validate(
                new ReservationRequest(name, date, 1L, 1L));

        // then
        assertThat(result).extracting(ConstraintViolation::getMessage)
                .containsExactly("name은 255자를 넘을 수 없습니다.");
    }

    @Test
    void 날짜를_null_로_생성시_예외() {
        // when
        Set<ConstraintViolation<ReservationRequest>> result = validator.validate(
                new ReservationRequest("구구", null, 1L, 1L));

        // then
        assertThat(result).extracting(ConstraintViolation::getMessage)
                .containsExactly("date는 비어 있을 수 없습니다.");
    }

    @Test
    void timeId가_null이면_예외_발생() {
        // when
        Set<ConstraintViolation<ReservationRequest>> result = validator.validate(
                new ReservationRequest("캐리", date, null, 1L));

        // then
        assertThat(result).extracting(ConstraintViolation::getMessage)
                .containsExactly("timeId는 비어 있을 수 없습니다.");
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    void timeId가_양수가_아니면_예외_발생(Long timeId) {
        // when
        Set<ConstraintViolation<ReservationRequest>> result = validator.validate(
                new ReservationRequest("홍길동", date, timeId, 1L));

        // then
        assertThat(result).extracting(ConstraintViolation::getMessage)
                .contains("timeId는 양수이어야 합니다.");
    }

    @Test
    void themeId가_null이면_예외_발생() {
        // when
        Set<ConstraintViolation<ReservationRequest>> result = validator.validate(
                new ReservationRequest("캐리", date, 1L, null));

        // then
        assertThat(result).extracting(ConstraintViolation::getMessage)
                .containsExactly("themeId는 비어 있을 수 없습니다.");
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    void themeId가_양수가_아니면_예외_발생(Long themeId) {
        // when
        Set<ConstraintViolation<ReservationRequest>> result = validator.validate(
                new ReservationRequest("홍길동", date, 1L, themeId));

        // then
        assertThat(result).extracting(ConstraintViolation::getMessage)
                .contains("themeId는 양수이어야 합니다.");
    }

    @Test
    void 정상_생성_테스트() {
        // given
        String name = "홍길동";
        Long timeId = 1L;
        Long themeId = 1L;

        // when
        ReservationRequest result = new ReservationRequest(name, date, timeId, themeId);

        // then
        assertAll(
                () -> assertThat(validator.validate(result)).isEmpty(),
                () -> assertThat(result.name()).isEqualTo(name),
                () -> assertThat(result.date()).isEqualTo(date),
                () -> assertThat(result.timeId()).isEqualTo(timeId),
                () -> assertThat(result.themeId()).isEqualTo(themeId));
    }
}
