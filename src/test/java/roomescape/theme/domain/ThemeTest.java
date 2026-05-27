package roomescape.theme.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.theme.exception.ThemeErrorInformation.*;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.theme.exception.ThemeException;

class ThemeTest {

    private final String name = "공포";
    private final String description = "테마 설명";
    private final String defaultThumbnailUrl = "DEFAULT_THUMBNAIL_URL";

    @Nested
    @DisplayName("create 메서드는")
    class CreateTest {


        @Test
        @DisplayName("객체를 생성한다")
        void 성공() {
            // when
            Theme theme = Theme.create(name, description, defaultThumbnailUrl);

            // then
            Assertions.assertThat(theme)
                .returns(null, Theme::getId)
                .returns(name, Theme::getName)
                .returns(description, Theme::getDescription)
                .returns(true, Theme::isActive);
        }


        @Test
        @DisplayName("이름이 null이면 예외가 발생한다")
        void 실패1() {
            // given
            String nullName = null;

            // when & then
            assertThatThrownBy(() -> Theme.create(nullName, description, defaultThumbnailUrl))
                .isInstanceOf(ThemeException.class)
                .hasMessage(NAME_IS_NULL.getMessage());
        }


        @Test
        @DisplayName("이름이 비어있으면 예외가 발생한다")
        void 실패2() {
            // given
            String emptyName = "";

            // when & then
            assertThatThrownBy(() -> Theme.create(emptyName, description, defaultThumbnailUrl))
                .isInstanceOf(ThemeException.class)
                .hasMessage(NAME_IS_NULL.getMessage());
        }


        @Test
        @DisplayName("설명이 null이면 예외가 발생한다")
        void 실패3() {
            // given
            String nullDescription = null;

            // when & then
            assertThatThrownBy(() -> Theme.create(name, nullDescription, defaultThumbnailUrl))
                .isInstanceOf(ThemeException.class)
                .hasMessage(DESCRIPTION_IS_NULL.getMessage());
        }


        @Test
        @DisplayName("설명이 비어있으면 예외가 발생한다")
        void 실패4() {
            // given
            String emptyDescription = "";

            // when & then
            assertThatThrownBy(() -> Theme.create(name, emptyDescription, defaultThumbnailUrl))
                .isInstanceOf(ThemeException.class)
                .hasMessage(DESCRIPTION_IS_NULL.getMessage());
        }


        @Test
        @DisplayName("썸네일 url이 null이면 예외가 발생한다")
        void 실패5() {
            // given
            String nullThumbnailUrl = null;

            // when & then
            assertThatThrownBy(() -> Theme.create(name, description, nullThumbnailUrl))
                .isInstanceOf(ThemeException.class)
                .hasMessage(THUMBNAIL_URL_IS_NULL.getMessage());
        }


        @Test
        @DisplayName("썸네일 url이 비어있어도 생성이 가능하다")
        void 성공3() {
            // given
            String emptyThumbnailUrl = "";

            // when
            Theme theme = Theme.create(name, description, emptyThumbnailUrl);

            //then
            assertThat(defaultThumbnailUrl)
                .isEqualTo(theme.getThumbnailUrl());
        }
    }

    @Nested
    @DisplayName("load 메서드는")
    class LoadTest {


        @Test
        @DisplayName("객체를 생성한다")
        void 성공() {
            // given
            Long loadValidId = 1L;
            boolean loadStatus = false;

            // when
            Assertions.assertThatCode(
                    () -> Theme.load(loadValidId, name, description, description, loadStatus))
                .doesNotThrowAnyException();
        }


        @Test
        @DisplayName("id가 Null이면 예외가 발생한다")
        void 실패() {
            // given
            Long nullId = null;
            boolean loadStatus = false;

            // when
            Assertions.assertThatThrownBy(
                    () -> Theme.load(nullId, name, description, description, loadStatus))
                .isInstanceOf(ThemeException.class)
                .hasMessage(ID_IS_NULL.getMessage());
        }
    }
}
