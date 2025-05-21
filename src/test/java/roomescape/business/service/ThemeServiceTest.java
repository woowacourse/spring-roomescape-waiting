package roomescape.business.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.business.dto.ThemeDto;
import roomescape.business.model.entity.Theme;
import roomescape.business.model.repository.Reservations;
import roomescape.business.model.repository.Themes;
import roomescape.business.model.vo.Id;
import roomescape.exception.business.NotFoundException;
import roomescape.exception.business.RelatedEntityExistException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ThemeServiceTest {

    @Mock
    private Themes themes;

    @Mock
    private Reservations reservations;

    @InjectMocks
    private ThemeService sut;

    @Test
    void 테마를_추가하고_반환한다() {
        // given
        String name = "주홍색 연구";
        String description = "셜록 홈즈의 첫 번째 사건";
        String thumbnail = "thumbnail.jpg";

        // when
        ThemeDto result = sut.addAndGet(name, description, thumbnail);

        // then
        assertThat(result).isNotNull();
        assertThat(result.name().value()).isEqualTo(name);
        assertThat(result.description()).isEqualTo(description);
        assertThat(result.thumbnail()).isEqualTo(thumbnail);
        verify(themes).save(any(Theme.class));
    }

    @Test
    void 테마를_삭제할_수_있다() {
        // given
        Id themeId = Id.create("theme-id");

        when(reservations.existByThemeId(themeId)).thenReturn(false);
        when(themes.existById(themeId)).thenReturn(true);

        // when
        sut.delete(themeId.value());

        // then
        verify(reservations).existByThemeId(themeId);
        verify(themes).existById(themeId);
        verify(themes).deleteById(themeId);
    }

    @Test
    void 존재하지_않는_테마_삭제_시_예외가_발생한다() {
        // given
        Id themeId = Id.create("non-existing-id");

        when(reservations.existByThemeId(themeId)).thenReturn(false);
        when(themes.existById(themeId)).thenReturn(false);

        // when, then
        assertThatThrownBy(() -> sut.delete(themeId.value()))
                .isInstanceOf(NotFoundException.class);

        verify(reservations).existByThemeId(themeId);
        verify(themes).existById(themeId);
        verify(themes, never()).deleteById(themeId);
    }

    @Test
    void 예약이_연결된_테마_삭제_시_예외가_발생한다() {
        // given
        Id themeId = Id.create("theme-with-reservations");

        when(reservations.existByThemeId(themeId)).thenReturn(true);

        // when, then
        assertThatThrownBy(() -> sut.delete(themeId.value()))
                .isInstanceOf(RelatedEntityExistException.class);

        verify(reservations).existByThemeId(themeId);
        verify(themes, never()).existById(themeId);
        verify(themes, never()).deleteById(themeId);
    }
}
