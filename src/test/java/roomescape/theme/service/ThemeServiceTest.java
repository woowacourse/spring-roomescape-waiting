package roomescape.theme.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import roomescape.global.exception.BadRequestException;
import roomescape.global.exception.ConflictException;
import roomescape.global.exception.NotFoundException;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;
import roomescape.theme.exception.ThemeErrorCode;
import roomescape.theme.service.dto.ThemeCommand;
import roomescape.theme.service.dto.ThemeResult;

@ExtendWith(MockitoExtension.class)
class ThemeServiceTest {

    @Mock
    private ThemeRepository themeRepository;

    @InjectMocks
    private ThemeService themeService;

    @Nested
    class save {
        @Test
        @DisplayName("테마를 성공적으로 생성한다.")
        void save_success() {
            // given
            ThemeCommand command = new ThemeCommand("브라운", "설명", "url");
            Theme savedTheme = new Theme(1L, "브라운", "설명", "url");

            given(themeRepository.save(any(Theme.class))).willReturn(savedTheme);

            // when
            ThemeResult result = themeService.save(command);

            // then
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.name()).isEqualTo("브라운");
            assertThat(result.description()).isEqualTo("설명");
            assertThat(result.thumbnailUrl()).isEqualTo("url");
        }

        @Test
        @DisplayName("테마 생성 시, 데이터베이스 제약 조건 위반(이름 중복 등)이 발생하면 예외가 변경된다.")
        void save_dataIntegrityViolationException() {
            // given
            ThemeCommand command = new ThemeCommand("브라운", "설명", "url");

            given(themeRepository.save(any(Theme.class)))
                    .willThrow(new DataIntegrityViolationException("duplicate"));

            // when & then
            assertThatThrownBy(() -> themeService.save(command))
                    .isInstanceOf(ConflictException.class)
                    .hasMessage(ThemeErrorCode.DUPLICATE_THEME.getMessage());
        }
    }

    @Test
    @DisplayName("전체 조회를 하면 레포지토리에 요청을 잘 한다")
    void findAll_success() {
        // given
        given(themeRepository.findAll()).willReturn(List.of());

        // when
        themeService.findAll();

        // then
        then(themeRepository).should().findAll();
    }

    @Nested
    class findById {
        @Test
        @DisplayName("id에 해당하는 테마 요청을 레포지토리 계층에 잘 한다")
        void findById_success() {
            // given
            Theme theme = new Theme(1L, "테마1", "설명1", "url1");
            given(themeRepository.findById(any(Long.class))).willReturn(Optional.of(theme));

            // when
            Theme result = themeService.findById(1L);

            // then
            then(themeRepository).should().findById(1L);
        }

        @Test
        @DisplayName("조회하려는 테마가 데이터 베이스에 없으면 예외가 터뜨린다.")
        void findById_notFound() {
            // given
            given(themeRepository.findById(1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> themeService.findById(1L))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage(ThemeErrorCode.THEME_NOT_FOUND.getMessage());
        }
    }


    @Test
    @DisplayName("테마를 성공적으로 삭제한다.")
    void delete_success() {
        // given
        Theme theme = new Theme(1L, "테마1", "설명1", "url1");
        given(themeRepository.findById(1L)).willReturn(Optional.of(theme));

        // when
        themeService.delete(1L);

        // then
        verify(themeRepository).delete(theme);
    }

    @Test
    @DisplayName("삭제하려는 테마가 없으면 예외가 발생한다.")
    void delete_notFound() {
        // given
        given(themeRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> themeService.delete(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(ThemeErrorCode.THEME_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("테마 삭제 시, 테마가 사용 중이면 예외가 발생한다.")
    void delete_inUse() {
        // given
        Theme theme = new Theme(1L, "테마1", "설명1", "url1");
        given(themeRepository.findById(1L)).willReturn(Optional.of(theme));
        willThrow(new DataIntegrityViolationException("외래키 오류")).given(themeRepository).delete(any(Theme.class));

        // when & then
        assertThatThrownBy(() -> themeService.delete(1L))
                .isInstanceOf(ConflictException.class)
                .hasMessage(ThemeErrorCode.THEME_IN_USE.getMessage());
    }
}
