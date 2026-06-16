package roomescape.controller.admin.fixture;

import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;
import roomescape.controller.admin.dto.request.AdminThemeRequest;

public class AdminThemeApiRequestFixture {

    public static Stream<Arguments> themeFailRequestFixture() {
        return Stream.of(
                // 테마 이름 검증
                Arguments.of(
                        new AdminThemeRequest(null, "설명", "http://image.png", 30000L),
                        "이름은 필수 값입니다."
                ),
                Arguments.of(
                        new AdminThemeRequest("", "설명", "http://image.png", 30000L),
                        "이름은 필수 값입니다."
                ),
                // 테마 설명 검증
                Arguments.of(
                        new AdminThemeRequest("테마", null, "http://image.png", 30000L),
                        "설명은 필수 값입니다."
                ),
                Arguments.of(
                        new AdminThemeRequest("테마", " ", "http://image.png", 30000L),
                        "설명은 필수 값입니다."
                ),
                // 썸네일 URL 검증
                Arguments.of(
                        new AdminThemeRequest("테마", "설명", null, 30000L),
                        "썸네일 이미지는 필수 값입니다."
                ),
                Arguments.of(
                        new AdminThemeRequest("테마", "설명", "", 30000L),
                        "썸네일 이미지는 필수 값입니다."
                ),
                // 금액 검증
                Arguments.of(
                        new AdminThemeRequest("테마", "설명", "http://image.png", null),
                        "금액은 필수 값입니다."
                ),
                Arguments.of(
                        new AdminThemeRequest("테마", "설명", "http://image.png", 0L),
                        "금액은 양수여야 합니다."
                )
        );
    }
}
