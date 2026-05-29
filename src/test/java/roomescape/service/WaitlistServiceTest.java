package roomescape.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWithStatus;
import roomescape.domain.Theme;
import roomescape.domain.exception.RoomEscapeException;
import roomescape.dto.ReservationRequest;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@SpringBootTest
@Transactional
public class WaitlistServiceTest {
    private static final LocalDate FUTURE_SECOND_DATE = LocalDate.now().plusDays(2);
    private static final LocalTime TEN = LocalTime.of(10, 0);

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationTimeRepository timeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private WaitlistService waitlistService;

    @Test
    void 예약_대기를_삭제한다() {
        ReservationTime reservationTime = createReservationTime(TEN);
        Theme theme = createTheme();

        ReservationRequest request = new ReservationRequest(
                "브라운",
                FUTURE_SECOND_DATE,
                reservationTime.getId(),
                theme.getId()
        );

        ReservationRequest waitlistRequest = new ReservationRequest(
                "브리",
                FUTURE_SECOND_DATE,
                reservationTime.getId(),
                theme.getId()
        );

        reservationService.reserveOrWait(request);
        ReservationWithStatus savedWaitlist = reservationService.reserveOrWait(waitlistRequest);

        waitlistService.cancelMyWaitlist(savedWaitlist.getId(), "브리");

        assertThatThrownBy(() -> waitlistService.getWaitlist(savedWaitlist.getId()))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void id가_존재하지_않으면_예외() {
        assertThatThrownBy(() -> waitlistService.cancelMyWaitlist(1L, "브라운"))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 본인의_대기가_아니면_취소할_수_없다() {
        ReservationTime reservationTime = createReservationTime(TEN);
        Theme theme = createTheme();

        ReservationRequest reservationRequest = new ReservationRequest(
                "브라운",
                FUTURE_SECOND_DATE,
                reservationTime.getId(),
                theme.getId()
        );

        ReservationRequest waitlistRequest = new ReservationRequest(
                "브리",
                FUTURE_SECOND_DATE,
                reservationTime.getId(),
                theme.getId()
        );

        reservationService.reserveOrWait(reservationRequest);
        ReservationWithStatus waitingReservation = reservationService.reserveOrWait(waitlistRequest);

        assertThatThrownBy(() -> waitlistService.cancelMyWaitlist(waitingReservation.getId(), "브라운"))
                .isInstanceOf(RoomEscapeException.class);
    }

    private ReservationTime createReservationTime(LocalTime time) {
        ReservationTime reservationTime = new ReservationTime(time);
        Long id = timeRepository.save(reservationTime);
        return new ReservationTime(id, reservationTime.getStartAt());
    }

    private Theme createTheme() {
        Theme theme = new Theme("방탈출 제목", "방탈출 설명", "thumbnail.png");
        Long id = themeRepository.save(theme);
        return new Theme(
                id,
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnailImageUrl()
        );
    }
}
