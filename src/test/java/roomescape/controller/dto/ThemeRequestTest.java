package roomescape.controller.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ThemeRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " "})
    void 이름이_null_또는_blank이면_예외(String name) {
        // when
        Set<ConstraintViolation<ThemeRequest>> result = validator.validate(
                new ThemeRequest(name, "설명", "썸네일"));

        // then
        assertThat(result).extracting(ConstraintViolation::getMessage)
                .containsExactly("name은 비어 있을 수 없습니다.");
    }

    @Test
    void 이름이_255자를_초과하면_예외() {
        // given
        String name = "a".repeat(256);

        // when
        Set<ConstraintViolation<ThemeRequest>> result = validator.validate(
                new ThemeRequest(name, "설명", "썸네일"));

        // then
        assertThat(result).extracting(ConstraintViolation::getMessage)
                .containsExactly("name은 255자를 넘을 수 없습니다.");
    }

    @Test
    void 설명이_255자를_초과하면_예외() {
        // given
        String description = "a".repeat(256);

        // when
        Set<ConstraintViolation<ThemeRequest>> result = validator.validate(
                new ThemeRequest("테마이름", description, "썸네일"));

        // then
        assertThat(result).extracting(ConstraintViolation::getMessage)
                .containsExactly("description은 255자를 넘을 수 없습니다.");
    }

    @Test
    void 썸네일_경로가_255자를_초과하면_예외() {
        // given
        String thumbnail = "a".repeat(256);

        // when
        Set<ConstraintViolation<ThemeRequest>> result = validator.validate(
                new ThemeRequest("테마이름", "설명", thumbnail));

        // then
        assertThat(result).extracting(ConstraintViolation::getMessage)
                .containsExactly("thumbnail은 255자를 넘을 수 없습니다.");
    }

    @ParameterizedTest
    @MethodSource(value = "themeParam")
    void 정상_생성_테스트(String description, String thumbnail) {
        // when
        ThemeRequest result = new ThemeRequest("테마이름", description, thumbnail);

        // then
        assertThat(validator.validate(result)).isEmpty();
    }

    private static Stream<Arguments> themeParam() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of("설명", "썸네일"),
                Arguments.of("", " "));
    }
}
