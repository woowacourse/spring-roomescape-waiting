package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import roomescape.config.TestClockConfig;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWithStatus;
import roomescape.domain.Theme;
import roomescape.domain.Waitlist;
import roomescape.dto.ReservationRequest;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitlistRepository;

@SpringBootTest
@Import(TestClockConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationPromotionRollbackTest {

    private static final LocalDate FIXED_TODAY = TestClockConfig.FIXED_NOW.toLocalDate();
    private static final LocalDate FUTURE_SECOND_DATE = FIXED_TODAY.plusDays(2);
    private static final LocalTime TEN = LocalTime.of(10, 0);

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @MockitoSpyBean
    private WaitlistRepository waitlistRepository;

    @Autowired
    private ReservationTimeRepository timeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Test
    void 대기_승격_중_대기_삭제가_실패하면_예약_삭제와_승격_저장이_롤백된다() {
        ReservationTime tenClock = createReservationTime(TEN);
        Theme theme = createTheme();

        ReservationRequest brownRequest = new ReservationRequest(
            "브라운",
            FUTURE_SECOND_DATE,
            tenClock.getId(),
            theme.getId()
        );
        ReservationRequest neoRequest = new ReservationRequest(
            "네오",
            FUTURE_SECOND_DATE,
            tenClock.getId(),
            theme.getId()
        );

        ReservationWithStatus savedReservation = reservationService.reserveOrWait(brownRequest);
        reservationService.reserveOrWait(neoRequest);
        Long slotId = reservationRepository.findById(savedReservation.getId()).orElseThrow().getSlot().getId();

        Waitlist promotedWaitlist = waitlistRepository.findBySlotId(slotId).getFirst();

        doThrow(new RuntimeException("대기 삭제 실패"))
            .when(waitlistRepository)
            .deleteById(promotedWaitlist.getId());

        assertThatThrownBy(() -> reservationService.cancelMyReservation(savedReservation.getId(), "브라운"))
            .isInstanceOf(RuntimeException.class);

        assertThat(reservationRepository.findById(savedReservation.getId())).isPresent();
        assertThat(waitlistRepository.findById(promotedWaitlist.getId())).isPresent();
        assertThat(reservationRepository.findByName("네오")).isEmpty();
    }

    private ReservationTime createReservationTime(LocalTime time) {
        ReservationTime reservationTime = new ReservationTime(time);
        Long id = timeRepository.save(reservationTime).getId();
        return new ReservationTime(id, reservationTime.getStartAt());
    }

    private Theme createTheme() {
        Theme theme = new Theme("방탈출 제목", "방탈출 설명", "thumbnail.png");
        Long id = themeRepository.save(theme).getId();
        return new Theme(
            id,
            theme.getName(),
            theme.getDescription(),
            theme.getThumbnailImageUrl()
        );
    }
}
