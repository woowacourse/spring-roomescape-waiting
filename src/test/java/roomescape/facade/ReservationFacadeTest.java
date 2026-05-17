package roomescape.facade;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.ReservationRequest;
import roomescape.exception.BusinessRuleViolationException;
import roomescape.exception.ConflictException;
import roomescape.service.ReservationTimeService;
import roomescape.service.ThemeService;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationFacadeTest {

    @Autowired
    private ReservationFacade reservationFacade;

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Autowired
    private ThemeService themeService;

    @Test
    void 사용중인_시간_삭제시_BusinessRuleViolationException이_발생한다() {
        ReservationTime time = reservationTimeService.addTime(new ReservationTime(null, LocalTime.of(10, 0)));
        Theme theme = themeService.addTheme(new Theme(null, "공포", "무서운 테마", "https://example.com/horror.jpg"));
        reservationFacade.addReservation(new ReservationRequest(
                "브라운", LocalDate.of(2026, 8, 5), time.getId(), theme.getId()));

        assertThatThrownBy(() -> reservationFacade.deleteTime(time.getId()))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void 사용중인_테마_삭제시_BusinessRuleViolationException이_발생한다() {
        ReservationTime time = reservationTimeService.addTime(new ReservationTime(null, LocalTime.of(10, 0)));
        Theme theme = themeService.addTheme(new Theme(null, "공포", "무서운 테마", "https://example.com/horror.jpg"));
        reservationFacade.addReservation(new ReservationRequest(
                "브라운", LocalDate.of(2026, 8, 5), time.getId(), theme.getId()));

        assertThatThrownBy(() -> reservationFacade.deleteTheme(theme.getId()))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void 같은_날짜_시간_테마에_중복_예약시_예외가_발생한다() {
        ReservationTime time = reservationTimeService.addTime(new ReservationTime(null, LocalTime.of(10, 0)));
        Theme theme = themeService.addTheme(new Theme(null, "공포", "무서운 테마", "https://example.com/horror.jpg"));
        ReservationRequest request = new ReservationRequest("브라운", LocalDate.of(2026, 8, 5), time.getId(), theme.getId());

        reservationFacade.addReservation(request);

        assertThatThrownBy(() -> reservationFacade.addReservation(request))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void 지난_날짜로_예약시_예외가_발생한다() {
        ReservationTime time = reservationTimeService.addTime(new ReservationTime(null, LocalTime.of(10, 0)));
        Theme theme = themeService.addTheme(new Theme(null, "공포", "무서운 테마", "https://example.com/horror.jpg"));
        LocalDate yesterday = LocalDate.now().minusDays(1);

        ReservationRequest request = new ReservationRequest("브라운", yesterday, time.getId(), theme.getId());

        assertThatThrownBy(() -> reservationFacade.addReservation(request))
                .isInstanceOf(BusinessRuleViolationException.class);
    }
}
