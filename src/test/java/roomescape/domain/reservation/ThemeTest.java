package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ThemeTest {

    @Test
    @DisplayName("이름,설명,섬네일을 통해 테마를 생성한다.")
    void create_domain_with_name_and_description_and_thumbnail() {
        assertThatCode(() -> new Theme(null, "레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.",
                new Thumbnail("https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg")))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("정적 팩토리 메소드로도 생성 가능하다.")
    void create_with_factory_method() {
        assertThatCode(() -> Theme.of(null, "레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"))
                .doesNotThrowAnyException();
    }
}
