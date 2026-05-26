package roomescape.theme.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.global.exception.RoomEscapeException;

class ThemeTest {

    @DisplayName("테마 이름이 비어있을 때 예외 발생을 테스트합니다.")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void validate_name(String name) {
        assertThatThrownBy(() -> Theme.builder()
                .name(name)
                .description("공포 테마 설명")
                .thumbnailImgUrl("http://img.url")
                .build())
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("테마 이름은 비어있을 수 없습니다.");
    }

    @DisplayName("테마 설명이 비어있을 때 예외 발생을 테스트합니다.")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void validate_description(String description) {
        assertThatThrownBy(() -> Theme.builder()
                .name("공포 테마")
                .description(description)
                .thumbnailImgUrl("http://img.url")
                .build())
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("테마 설명은 비어있을 수 없습니다.");
    }

    @DisplayName("썸네일 이미지 URL이 비어있을 때 예외 발생을 테스트합니다.")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void validate_thumbnail_img_url(String thumbnailImgUrl) {
        assertThatThrownBy(() -> Theme.builder()
                .name("공포 테마")
                .description("공포 테마 설명")
                .thumbnailImgUrl(thumbnailImgUrl)
                .build())
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("썸네일 이미지 URL은 비어있을 수 없습니다.");
    }
}
