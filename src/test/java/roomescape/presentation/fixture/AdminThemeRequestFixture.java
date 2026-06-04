package roomescape.presentation.fixture;

import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;
import roomescape.theme.presentation.dto.ThemeRequest;

public final class AdminThemeRequestFixture {

    private AdminThemeRequestFixture() {
    }

    public static Stream<Arguments> themeFailRequestFixture() {
        return Stream.of(
                Arguments.of(new ThemeRequest(null, "http://image.png", "설명"), "[name] 이름은 필수입니다."),
                Arguments.of(new ThemeRequest("", "http://image.png", "설명"), "[name] 이름은 필수입니다."),
                Arguments.of(new ThemeRequest("테마", null, "설명"), "[thumbnailImageUrl] 섬네일 이미지가 없습니다."),
                Arguments.of(new ThemeRequest("테마", "", "설명"), "[thumbnailImageUrl] 섬네일 이미지가 없습니다."),
                Arguments.of(new ThemeRequest("테마", "http://image.png", null), "[description] 설명을 작성해주세요."),
                Arguments.of(new ThemeRequest("테마", "http://image.png", ""), "[description] 설명을 작성해주세요.")
        );
    }
}
