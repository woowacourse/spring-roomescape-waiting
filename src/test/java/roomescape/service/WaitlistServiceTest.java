package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.domain.exception.DomainErrorCode.UNAUTHORIZED_RESERVATION;
import static roomescape.domain.exception.DomainErrorCode.WAITLIST_NOT_FOUND;

import java.time.LocalDate;
import java.time.LocalTime;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWithStatus;
import roomescape.domain.Theme;
import roomescape.domain.exception.DomainErrorCode;
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

        reservationService.applyReservation(request);
        ReservationWithStatus savedWaitlist = reservationService.applyReservation(waitlistRequest);

        waitlistService.cancelMyWaitlist(savedWaitlist.getId(), "브리");

        assertThatRoomEscapeExceptionCode(
                () -> waitlistService.getWaitlist(savedWaitlist.getId()),
                WAITLIST_NOT_FOUND
        );
    }

    @Test
    void id가_존재하지_않으면_예외() {
        assertThatRoomEscapeExceptionCode(
                () -> waitlistService.cancelMyWaitlist(1L, "브라운"),
                WAITLIST_NOT_FOUND
        );
    }

    @Test
    void 본인의_대기가_아니면_취소할_수_없다() {
        ReservationTime reservationTime = createReservationTime(TEN);
        Theme theme = createTheme();

        reservationService.applyReservation(new ReservationRequest(
                "브리",
                FUTURE_SECOND_DATE,
                reservationTime.getId(),
                theme.getId()
        ));

        ReservationWithStatus savedWaitlist = reservationService.applyReservation(new ReservationRequest(
                "브라운",
                FUTURE_SECOND_DATE,
                reservationTime.getId(),
                theme.getId()
        ));

        String other = "브리";
        assertThatRoomEscapeExceptionCode(
                () -> waitlistService.cancelMyWaitlist(savedWaitlist.getId(), other),
                UNAUTHORIZED_RESERVATION
        );
    }

    private void assertThatRoomEscapeExceptionCode(ThrowingCallable callable, DomainErrorCode expectedCode) {
        assertThatThrownBy(callable)
                .isInstanceOfSatisfying(RoomEscapeException.class,
                        exception -> assertThat(exception.code()).isEqualTo(expectedCode));
    }

    private ReservationTime createReservationTime(LocalTime time) {
        ReservationTime reservationTime = new ReservationTime(time);
        return timeRepository.save(reservationTime);
    }

    private Theme createTheme() {
        Theme theme = new Theme("방탈출 제목", "방탈출 설명", "thumbnail.png");
        return themeRepository.save(theme);
    }
}
