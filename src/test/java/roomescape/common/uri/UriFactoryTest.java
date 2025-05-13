package roomescape.common.uri;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;
import roomescape.common.utils.UriFactory;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class UriFactoryTest {

    @Test
    @DisplayName("BasePath와 PathSegment가 올바르게 조립된다")
    void buildValidPath1() throws URISyntaxException {
        // when
        final URI uri = UriFactory.buildPath("/gang-san", "bye", "saturday", "hi", "sunday");

        // then
        assertThat(uri).isEqualTo(new URI("/gang-san/bye/saturday/hi/sunday"));
    }

    @Test
    @DisplayName("BasePath가 / 로만 구성되어도 올바르게 조립된다")
    void buildValidPath2() throws URISyntaxException {
        // when
        final URI uri = UriFactory.buildPath("/", "bye", "saturday", "hi", "sunday");

        // then
        assertThat(uri).isEqualTo(new URI("/bye/saturday/hi/sunday"));
    }

    @Test
    @DisplayName("BasePath는 /가 2개 이상 있을 수 있다")
    void buildValidPath3() throws URISyntaxException {
        // when & then
        final URI uri = UriFactory.buildPath("/gang-san/second", "bye", "saturday", "hi", "sunday");

        assertThat(uri).isEqualTo(new URI("/gang-san/second/bye/saturday/hi/sunday"));
    }

    @Test
    @DisplayName("BasePath가 null일 수 없습니다")
    void buildInvalidPath1() {
        // when & then
        assertThatThrownBy(() -> UriFactory.buildPath(null, "bye", "saturday", "hi", "sunday"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("basePath는 최소한 / 가 필요합니다.");
    }

    @Test
    @DisplayName("BasePath가 비어있을 수 없습니다")
    void buildInvalidPath2() {
        // when & then
        assertThatThrownBy(() -> UriFactory.buildPath("", "bye", "saturday", "hi", "sunday"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("basePath는 최소한 / 가 필요합니다.");
    }

    @Test
    @DisplayName("각 PathSegment는 /를 포함할 수 없다")
    void buildInvalidPath3() {
        // when & then
        assertThatThrownBy(() -> UriFactory.buildPath("/gang-san", "/bye", "saturday", "/hi", "sunday"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("pathSegment는 /를 포함할 수 없습니다.");
    }

    @Test
    @DisplayName("각 PathSegment는 공백일 수 없다")
    void buildInvalidPath4() {
        // when & then
        assertThatThrownBy(() -> UriFactory.buildPath("/gang-san", "", "saturday", "", "sunday"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("pathSegment는 null이거나 빈 문자열일 수 없습니다.");
    }

    @Test
    @DisplayName("BasePath는 /로 시작해야 한다")
    void buildInvalidPath5() {
        // when & then
        assertThatThrownBy(() -> UriFactory.buildPath("gang-san/", "bye", "saturday", "hi", "sunday"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("basePath는 /로 시작해야 합니다.");

        assertThatThrownBy(() -> UriFactory.buildPath("gang/san", "bye", "saturday", "hi", "sunday"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("basePath는 /로 시작해야 합니다.");

        assertThatThrownBy(() -> UriFactory.buildPath("gang-san", "bye", "saturday", "hi", "sunday"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("basePath는 /로 시작해야 합니다.");
    }
}
