package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWithStatus;
import roomescape.domain.Theme;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationUpdateRequest;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitlistRepository;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ReservationServiceTransactionTest {

    private static final LocalDate FUTURE_SECOND_DATE = LocalDate.now().plusDays(2);
    private static final LocalTime TEN = LocalTime.of(10, 0);

    @Autowired
    private ReservationService reservationService;

    @MockitoSpyBean
    private ReservationRepository reservationRepository;

    @MockitoSpyBean
    private WaitlistRepository waitlistRepository;

    @Autowired
    private ReservationTimeRepository timeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Test
    void 대기_승격_중_예약_저장에_실패하면_예약_취소도_롤백된다() {
        ReservationTime reservationTime = createReservationTime(TEN);
        Theme theme = createTheme();
        String name = "브라운";
        String waitingName = "브리";

        ReservationWithStatus reservation = reservationService.applyReservation(
                createReservationRequest(name, FUTURE_SECOND_DATE, reservationTime, theme));
        ReservationWithStatus waitlist = reservationService.applyReservation(
                createReservationRequest(waitingName, FUTURE_SECOND_DATE, reservationTime, theme));

        AtomicBoolean failPromotion = new AtomicBoolean(false);

        doAnswer(invocation -> {
            if (failPromotion.get()) {
                throw new RuntimeException();
            }
            return invocation.callRealMethod();
        }).when(reservationRepository).save(any(Reservation.class));

        failPromotion.set(true);

        assertThatThrownBy(() ->
                reservationService.cancelMyReservationAndPromoteWaitlist(reservation.getId(), name))
                .isInstanceOf(RuntimeException.class);
        assertThat(reservationService.getReservation(reservation.getId()).getName())
                .isEqualTo(name);
        assertThat(reservationService.getWaitlist(waitlist.getId()).getName())
                .isEqualTo(waitingName);
        assertThat(reservationService.getReservations())
                .extracting(Reservation::getName)
                .doesNotContain(waitingName);
    }

    @Test
    void 대기_승격_후_대기_취소가_실패하면_예약취소_및_대기승격도_롤백된다() {
        ReservationTime reservationTime = createReservationTime(TEN);
        Theme theme = createTheme();
        String name = "브라운";
        String waitingName = "브리";

        ReservationWithStatus reservation = reservationService.applyReservation(
                createReservationRequest(name, FUTURE_SECOND_DATE, reservationTime, theme));
        ReservationWithStatus waitlist = reservationService.applyReservation(
                createReservationRequest(waitingName, FUTURE_SECOND_DATE, reservationTime, theme));

        AtomicBoolean failPromotion = new AtomicBoolean(false);

        doAnswer(invocation -> {
            if (failPromotion.get()) {
                throw new RuntimeException();
            }
            return invocation.callRealMethod();
        }).when(waitlistRepository).deleteById(any(Long.class));

        failPromotion.set(true);

        assertThatThrownBy(() ->
                reservationService.cancelMyReservationAndPromoteWaitlist(reservation.getId(), name))
                .isInstanceOf(RuntimeException.class);
        assertThat(reservationService.getReservation(reservation.getId()).getName())
                .isEqualTo(name);
        assertThat(reservationService.getWaitlist(waitlist.getId()).getName())
                .isEqualTo(waitingName);
        assertThat(reservationService.getReservations())
                .extracting(Reservation::getName)
                .doesNotContain(waitingName);
    }

    @Test
    void 대기_승격_중_예약_저장에_실패하면_예약_변경도_롤백된다() {
        ReservationTime reservationTime = createReservationTime(TEN);
        Theme theme = createTheme();
        String name = "브라운";
        String waitingName = "브리";

        ReservationWithStatus reservation = reservationService.applyReservation(
                createReservationRequest(name, FUTURE_SECOND_DATE, reservationTime, theme));
        ReservationWithStatus waitlist = reservationService.applyReservation(
                createReservationRequest(waitingName, FUTURE_SECOND_DATE, reservationTime, theme));

        AtomicBoolean failPromotion = new AtomicBoolean(false);

        doAnswer(invocation -> {
            if (failPromotion.get()) {
                throw new RuntimeException();
            }
            return invocation.callRealMethod();
        }).when(reservationRepository).save(any(Reservation.class));

        failPromotion.set(true);

        ReservationTime updateTime = createReservationTime(LocalTime.of(12, 0));
        LocalDate updateDate = FUTURE_SECOND_DATE.plusDays(1);
        ReservationUpdateRequest updateRequest = new ReservationUpdateRequest(
                updateDate,
                updateTime.getId()
        );

        assertThatThrownBy(() ->
                reservationService.updateMyReservationAndPromoteWaitlist(reservation.getId(), name, updateRequest))
                .isInstanceOf(RuntimeException.class);
        assertThat(reservationService.getReservation(reservation.getId()))
                .extracting(
                        Reservation::getDate,
                        r -> r.getTime().getId(),
                        r -> r.getTime().getStartAt()
                ).containsExactly(FUTURE_SECOND_DATE, reservationTime.getId(), reservationTime.getStartAt())
                .doesNotContain(updateDate, updateTime.getId(), updateTime.getStartAt());
        assertThat(reservationService.getWaitlist(waitlist.getId()).getName())
                .isEqualTo(waitingName);
        assertThat(reservationService.getReservations())
                .extracting(Reservation::getName)
                .doesNotContain(waitingName);
    }

    @Test
    void 대기_승격_후_대기_취소가_실패하면_예약변경_및_대기승격도_롤백된다() {
        ReservationTime reservationTime = createReservationTime(TEN);
        Theme theme = createTheme();
        String name = "브라운";
        String waitingName = "브리";

        ReservationWithStatus reservation = reservationService.applyReservation(
                createReservationRequest(name, FUTURE_SECOND_DATE, reservationTime, theme));
        ReservationWithStatus waitlist = reservationService.applyReservation(
                createReservationRequest(waitingName, FUTURE_SECOND_DATE, reservationTime, theme));

        AtomicBoolean failPromotion = new AtomicBoolean(false);

        doAnswer(invocation -> {
            if (failPromotion.get()) {
                throw new RuntimeException();
            }
            return invocation.callRealMethod();
        }).when(waitlistRepository).deleteById(any(Long.class));

        failPromotion.set(true);

        ReservationTime updateTime = createReservationTime(LocalTime.of(12, 0));
        LocalDate updateDate = FUTURE_SECOND_DATE.plusDays(1);
        ReservationUpdateRequest updateRequest = new ReservationUpdateRequest(
                updateDate,
                updateTime.getId()
        );
        assertThatThrownBy(() ->
                reservationService.updateMyReservationAndPromoteWaitlist(reservation.getId(), name, updateRequest))
                .isInstanceOf(RuntimeException.class);
        assertThat(reservationService.getReservation(reservation.getId()))
                .extracting(
                        Reservation::getDate,
                        r -> r.getTime().getId(),
                        r -> r.getTime().getStartAt()
                ).containsExactly(FUTURE_SECOND_DATE, reservationTime.getId(), reservationTime.getStartAt())
                .doesNotContain(updateDate, updateTime.getId(), updateTime.getStartAt());
        assertThat(reservationService.getWaitlist(waitlist.getId()).getName())
                .isEqualTo(waitingName);
        assertThat(reservationService.getReservations())
                .extracting(Reservation::getName)
                .doesNotContain(waitingName);
    }

    private ReservationTime createReservationTime(LocalTime time) {
        ReservationTime reservationTime = new ReservationTime(time);
        return timeRepository.save(reservationTime);
    }

    private Theme createTheme() {
        Theme theme = new Theme("방탈출 제목", "방탈출 설명", "thumbnail.png");
        return themeRepository.save(theme);
    }

    private ReservationRequest createReservationRequest(
            String name,
            LocalDate date,
            ReservationTime time,
            Theme theme
    ) {
        return new ReservationRequest(
                name,
                date,
                time.getId(),
                theme.getId()
        );
    }

}
