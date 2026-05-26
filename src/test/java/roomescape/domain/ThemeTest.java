package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.exception.InvalidDomainException;

class ThemeTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void 테마_이름이_비어있으면_예외(String name) {
        assertThatThrownBy(() -> new Theme(null, name, "설명", "https://thumbnail.url"))
                .isInstanceOf(InvalidDomainException.class)
                .hasMessage("테마 이름은 비어있을 수 없습니다.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void 테마_설명이_비어있으면_예외(String description) {
        assertThatThrownBy(() -> new Theme(null, "테마", description, "https://thumbnail.url"))
                .isInstanceOf(InvalidDomainException.class)
                .hasMessage("테마 설명은 비어있을 수 없습니다.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void 테마_썸네일_URL이_비어있으면_예외(String thumbnailImageUrl) {
        assertThatThrownBy(() -> new Theme(null, "테마", "설명", thumbnailImageUrl))
                .isInstanceOf(InvalidDomainException.class)
                .hasMessage("테마 썸네일 이미지 URL은 비어있을 수 없습니다.");
    }
}