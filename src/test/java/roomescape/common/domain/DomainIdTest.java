package roomescape.common.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.InvalidInputException;
import roomescape.reservation.domain.ReservationId;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeDescription;
import roomescape.theme.domain.ThemeId;
import roomescape.theme.domain.ThemeName;
import roomescape.theme.domain.ThemeThumbnail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DomainIdTest {

    @Test
    @DisplayName("unassigned된 DomainId에서 getValue를 호출하면 예외가 발생한다")
    void getValueWhenUnassignedThrowsException() {
        // given
        final DomainId id = DomainId.unassigned();

        // when & then
        assertThatThrownBy(id::getValue)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("저장되지 않아 식별할 수 없습니다.");
    }

    @Test
    @DisplayName("unassigned된 DomainId끼리 비교할 수 없다")
    void unassignedDomainIdsAreNotEqual() {
        // given
        final DomainId id1 = DomainId.unassigned();
        final DomainId id2 = DomainId.unassigned();

        // when & then
        assertThatThrownBy(() -> id1.equals(id2))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("저장되지 않아 식별할 수 없습니다.");
    }

    @Test
    @DisplayName("DomainId는 null로 설정할 수 없다")
    void cannotAssignNullToDomainId() {
        // when & then
        assertThatThrownBy(() -> ReservationId.from(null))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("DomainId.value 은(는) null일 수 없습니다.");
    }

    @Test
    @DisplayName("DomainId의 값이 같은 두 객체는 같은 객체로 취급한다")
    void sameIdSameObject() {
        // given
        final Long sameIdValue = 1L;

        final ThemeId sameId1 = ThemeId.from(sameIdValue);
        final ThemeId sameId2 = ThemeId.from(sameIdValue);

        final Theme sameTheme1 = Theme.withId(
                sameId1,
                ThemeName.from("공포"),
                ThemeDescription.from("지구별 방탈출 최고"),
                ThemeThumbnail.from("www.making.com"));

        final Theme sameTheme2 = Theme.withId(
                sameId2,
                ThemeName.from("시러"),
                ThemeDescription.from("지구별 방탈출 으악"),
                ThemeThumbnail.from("www.com"));

        // when & then
        assertThat(sameTheme1).isEqualTo(sameTheme2);
    }
}
