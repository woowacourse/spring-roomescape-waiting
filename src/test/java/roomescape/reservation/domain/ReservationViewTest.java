package roomescape.reservation.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.validate.InvalidInputException;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeDescription;
import roomescape.theme.domain.ThemeName;
import roomescape.theme.domain.ThemeThumbnail;
import roomescape.time.domain.ReservationTime;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationViewTest {

    @Test
    @DisplayName("테마 이름이 null이면 예외가 발생한다")
    void getId() {
        ReservationView reservationView = new ReservationView(
                null,
                1L,
                ReservationDate.from(LocalDate.now()),
                ReservationTime.withoutId(LocalTime.of(11, 0)),
                Theme.withoutId(
                        ThemeName.from("테마 이름"),
                        ThemeDescription.from("테마 설명"),
                        ThemeThumbnail.from("https://example.com/image.jpg")
                ),
                ReservationStatus.CONFIRMED,
                1
        );
        assertThatThrownBy(reservationView::getId)
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("Validation failed [while checking null]: ReservationView.compositeId");
    }
}
