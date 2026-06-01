package roomescape.reservation.application;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.customException.BusinessException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.fake.FakeReservationRepository;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

class ThemeReferenceAdapterTest {

    private ReservationRepository reservationRepository;
    private ThemeReferenceAdapter referenceAdapter;

    @BeforeEach
    void setUp() {
        reservationRepository = new FakeReservationRepository();
        referenceAdapter = new ThemeReferenceAdapter(reservationRepository);
    }

    @Test
    @DisplayName("테마를 참조하는 예약이 없으면 예외가 발생하지 않는다")
    void validateThemeNotReferenced_success() {
        // when & then
        assertThatCode(() -> referenceAdapter.validateThemeNotReferenced(1L))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("테마를 참조하는 예약이 있으면 예외가 발생한다")
    void validateThemeNotReferenced_fail_with_referenced_theme() {
        // given
        ReservationTime time = ReservationTime.createRow(1L, LocalTime.of(10, 0));
        Theme theme = Theme.createRow(1L, "공포", "설명", "https://good.com");
        reservationRepository.save(Reservation.create("인직", LocalDate.now().plusDays(1), time, theme));

        // when & then
        assertThatThrownBy(() -> referenceAdapter.validateThemeNotReferenced(theme.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("예약이 연결된 테마는 삭제할 수 없습니다.");
    }
}
