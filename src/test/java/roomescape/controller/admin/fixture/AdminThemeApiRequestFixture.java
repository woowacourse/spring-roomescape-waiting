package roomescape.controller.admin.fixture;

import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;
import roomescape.controller.admin.api.dto.request.AdminThemeRequest;

public class AdminThemeApiRequestFixture {

    public static Stream<Arguments> themeFailRequestFixture() {
        return Stream.of(
                // 테마 이름 검증
                Arguments.of(
                        new AdminThemeRequest(null, "설명", "http://image.png"),
                        "이름은 필수 값입니다."
                ),
                Arguments.of(
                        new AdminThemeRequest("", "설명", "http://image.png"),
                        "이름은 필수 값입니다."
                ),
                // 테마 설명 검증
                Arguments.of(
                        new AdminThemeRequest("테마", null, "http://image.png"),
                        "설명은 필수 값입니다."
                ),
                Arguments.of(
                        new AdminThemeRequest("테마", " ", "http://image.png"),
                        "설명은 필수 값입니다."
                ),
                // 썸네일 URL 검증
                Arguments.of(
                        new AdminThemeRequest("테마", "설명", null),
                        "썸네일 이미지는 필수 값입니다."
                ),
                Arguments.of(
                        new AdminThemeRequest("테마", "설명", ""),
                        "썸네일 이미지는 필수 값입니다."
                )
        );
    }
}
