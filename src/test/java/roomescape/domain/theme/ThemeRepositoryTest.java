package roomescape.domain.theme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static roomescape.TestFixtures.anyTheme;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import roomescape.exception.NotFoundException;

@DataJpaTest
class ThemeRepositoryTest {

    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private TestEntityManager entityManager;

    private Theme savedTheme;

    @BeforeEach
    void setUp() {
        savedTheme = themeRepository.save(anyTheme());
        entityManager.clear();
    }

    @Test
    @DisplayName("아이디에 해당하는 테마를 삭제한다.")
    void deleteByIdWhenFound() {
        var id = savedTheme.id();

        assertAll(
            () -> assertDoesNotThrow(() -> themeRepository.deleteByIdOrElseThrow(id)),
            () -> assertThat(themeRepository.findById(id)).isEmpty()
        );
    }

    @Test
    @DisplayName("테마 삭제 시 해당 아이디의 테마가 없으면 예외가 발생한다.")
    void deleteByIdWhenNotFound() {
        var id = savedTheme.id();

        assertAll(
            () -> assertThrows(NotFoundException.class, () -> themeRepository.deleteByIdOrElseThrow(1234L)),
            () -> assertThat(themeRepository.findById(id)).hasValue(savedTheme)
        );
    }

    @Test
    @DisplayName("아이디에 해당하는 테마를 조회한다.")
    void getById() {
        var id = savedTheme.id();

        var found = themeRepository.getById(id);

        assertThat(found).isEqualTo(savedTheme);
    }

    @Test
    @DisplayName("테마 조회 시 해당 아이디의 테마가 없으면 예외가 발생한다.")
    void getByIdWhenNotFound() {
        assertThatThrownBy(() -> themeRepository.getById(1234L))
            .isInstanceOf(NotFoundException.class);
    }
}
