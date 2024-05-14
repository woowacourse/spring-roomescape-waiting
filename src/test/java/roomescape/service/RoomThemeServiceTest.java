package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.domain.RoomTheme;
import roomescape.repository.RoomThemeRepository;
import roomescape.service.dto.request.RoomThemeCreateRequest;
import roomescape.service.dto.response.RoomThemeResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RoomThemeServiceTest {

    @Autowired
    private RoomThemeService roomThemeService;
    @Autowired
    private RoomThemeRepository roomThemeRepository;

    @BeforeEach
    void setUp() {
        List<RoomTheme> roomThemes = roomThemeRepository.findAll();
        for (RoomTheme roomTheme : roomThemes) {
            roomThemeRepository.deleteById(roomTheme.getId());
        }
    }

    @DisplayName("테마 저장")
    @Test
    void save() {
        // given
        RoomThemeCreateRequest roomThemeCreateRequest = new RoomThemeCreateRequest("레벨2 탈출",
                "우테코 레벨2",
                "https://i.pinimg.com/236x/6e");
        // when
        RoomThemeResponse roomThemeResponse = roomThemeService.save(roomThemeCreateRequest);
        // then
        assertAll(
                () -> assertThat(roomThemeResponse.name()).isEqualTo(roomThemeCreateRequest.name()),
                () -> assertThat(roomThemeResponse.description()).isEqualTo(
                        roomThemeCreateRequest.description()),
                () -> assertThat(roomThemeResponse.thumbnail()).isEqualTo(
                        roomThemeCreateRequest.thumbnail())
        );
    }

    @DisplayName("모든 테마 조회")
    @Test
    void findAll() {
        // given & when
        List<RoomThemeResponse> roomThemeResponses = roomThemeService.findAll();
        // then
        assertThat(roomThemeResponses).isEmpty();
    }

    @DisplayName("테마 삭제")
    @Test
    void delete() {
        // given
        RoomThemeCreateRequest roomThemeCreateRequest = new RoomThemeCreateRequest("레벨2 탈출",
                "우테코 레벨2",
                "https://i.pinimg.com/236x/6e");
        RoomThemeResponse roomThemeResponse = roomThemeService.save(roomThemeCreateRequest);
        // when
        roomThemeService.deleteById(roomThemeResponse.id());
        // then
        assertThat(roomThemeService.findAll()).isEmpty();
    }
}
