package roomescape.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(ThemeService.class)
class ThemeServiceTest {

    @Autowired
    private ThemeService service;

    @Test
    @DisplayName("테마를 추가할 수 있다.")
    void register() {
        // given
        var name = "포포는 힘들다";
        var description = "우테코 레벨2를 탈출하는 내용입니다.";
        var thumbnail = "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg";

        // when
        var created = service.register(name, description, thumbnail);

        // then
        var themes = service.findAllThemes();
        assertThat(themes).contains(created);
    }

    @Test
    @DisplayName("테마를 삭제할 수 있다.")
    void removeById() {
        // given
        var name = "포포는 힘들다";
        var description = "우테코 레벨2를 탈출하는 내용입니다.";
        var thumbnail = "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg";
        var created = service.register(name, description, thumbnail);

        // when
        service.removeById(created.id());

        // then
        var themes = service.findAllThemes();
        assertThat(themes).doesNotContain(created);
    }
}
