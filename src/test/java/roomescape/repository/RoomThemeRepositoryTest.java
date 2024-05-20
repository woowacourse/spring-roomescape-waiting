package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.TestFixture.ROOM_THEME1;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.domain.RoomTheme;
import roomescape.exception.NotFoundException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RoomThemeRepositoryTest {

    @Autowired
    private RoomThemeRepository roomThemeRepository;

    @BeforeEach
    void setUp() {
        List<RoomTheme> roomThemes = roomThemeRepository.findAll();
        for (RoomTheme roomTheme : roomThemes) {
            roomThemeRepository.deleteById(roomTheme.getId());
        }
    }

    @DisplayName("테마를 저장한다.")
    @Test
    void save() {
        // given
        RoomTheme roomTheme = ROOM_THEME1;
        // when
        RoomTheme savedRoomTheme = roomThemeRepository.save(roomTheme);
        // then
        assertAll(
                () -> assertThat(savedRoomTheme.getName()).isEqualTo(roomTheme.getName()),
                () -> assertThat(savedRoomTheme.getDescription()).isEqualTo(
                        roomTheme.getDescription()),
                () -> assertThat(savedRoomTheme.getThumbnail()).isEqualTo(roomTheme.getThumbnail())
        );
    }

    @DisplayName("저장된 모든 테마를 보여준다.")
    @Test
    void findAll() {
        // given & when
        List<RoomTheme> roomThemes = roomThemeRepository.findAll();
        // then
        assertThat(roomThemes).isEmpty();
    }

    @DisplayName("테마를 삭제한다.")
    @Test
    void deleteTheme() {
        // given
        RoomTheme roomTheme = ROOM_THEME1;
        RoomTheme savedRoomTheme = roomThemeRepository.save(roomTheme);
        // when
        roomThemeRepository.deleteById(savedRoomTheme.getId());
        // then
        assertThat(roomThemeRepository.findAll()).isEmpty();
    }

    @DisplayName("해당 id의 테마를 보여준다.")
    @Test
    void findById() {
        // given
        RoomTheme roomTheme = ROOM_THEME1;
        RoomTheme savedRoomTheme = roomThemeRepository.save(roomTheme);
        // when
        RoomTheme findRoomTheme = roomThemeRepository.findById(savedRoomTheme.getId())
                .orElseThrow(() -> new NotFoundException("테마를 찾을 수 없습니다."));
        // then
        assertAll(
                () -> assertThat(findRoomTheme.getId()).isEqualTo(savedRoomTheme.getId()),
                () -> assertThat(findRoomTheme.getName()).isEqualTo(savedRoomTheme.getName()),
                () -> assertThat(findRoomTheme.getDescription()).isEqualTo(
                        savedRoomTheme.getDescription()),
                () -> assertThat(findRoomTheme.getThumbnail()).isEqualTo(
                        savedRoomTheme.getThumbnail())
        );
    }
}
