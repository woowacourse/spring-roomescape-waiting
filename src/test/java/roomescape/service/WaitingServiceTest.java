package roomescape.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import roomescape.domain.*;
import roomescape.domain.exception.DomainConflictException;
import roomescape.service.dto.WaitingResult;
import roomescape.service.exception.BusinessConflictException;
import roomescape.service.exception.ResourceNotFoundException;
import roomescape.service.fake.FakeReservationRepository;
import roomescape.service.fake.FakeReservationTimeRepository;
import roomescape.service.fake.FakeThemeRepository;
import roomescape.service.fake.FakeWaitingRepository;

import java.time.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WaitingServiceTest {

    private FakeWaitingRepository waitingRepository;
    private FakeReservationRepository reservationRepository;
    private FakeReservationTimeRepository reservationTimeRepository;
    private FakeThemeRepository themeRepository;
    private WaitingService waitingService;

    private ReservationTime time;
    private Theme theme;
    private final LocalDate futureDate = LocalDate.of(2026, 5, 10);

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-05-01T00:00:00Z"), ZoneOffset.UTC);
        waitingRepository = new FakeWaitingRepository();
        reservationRepository = new FakeReservationRepository();
        reservationTimeRepository = new FakeReservationTimeRepository();
        themeRepository = new FakeThemeRepository();

        time = new ReservationTime(1L, LocalTime.of(10, 0));
        theme = new Theme(1L, "공포방", "무서운방입니다.", "image-url");
        reservationTimeRepository.add(time);
        themeRepository.add(theme);

        waitingService = new WaitingService(
                reservationTimeRepository, themeRepository, waitingRepository, reservationRepository, fixedClock);
    }

    @Test
    void 예약_대기를_생성하고_대기_순번을_반환한다() {
        reservationRepository.save(new Reservation(null, "예약자", new Schedule(futureDate, time, theme)));
        waitingRepository.save(new Waiting(null, "선행자", new Schedule(futureDate, time, theme)));

        WaitingResult result = waitingService.createWaiting("레서", futureDate, 1L, 1L);

        assertThat(result.waiting().getName()).isEqualTo("레서");
        assertThat(result.order()).isEqualTo(2L);
        assertThat(waitingRepository.findAll())
                .extracting(Waiting::getName)
                .containsExactly("선행자", "레서");
    }

    @Test
    void 존재하지_않는_시간_id로_예약_대기를_생성하면_예외가_발생한다() {
        assertThatThrownBy(() -> waitingService.createWaiting("레서", futureDate, 999L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThat(waitingRepository.findAll()).isEmpty();
    }

    @Test
    void 존재하지_않는_테마_id로_예약_대기를_생성하면_예외가_발생한다() {
        assertThatThrownBy(() -> waitingService.createWaiting("레서", futureDate, 1L, 999L))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThat(waitingRepository.findAll()).isEmpty();
    }

    @Test
    void 이미_지난_날짜로_예약_대기를_생성하면_예외가_발생한다() {
        LocalDate pastDate = LocalDate.of(2026, 4, 1);

        assertThatThrownBy(() -> waitingService.createWaiting("레서", pastDate, 1L, 1L))
                .isInstanceOf(DomainConflictException.class);
        assertThat(waitingRepository.findAll()).isEmpty();
    }

    @Test
    void 동일한_일정에_이미_대기_중이면_예외가_발생한다() {
        waitingRepository.save(new Waiting(null, "레서", new Schedule(futureDate, time, theme)));

        assertThatThrownBy(() -> waitingService.createWaiting("레서", futureDate, 1L, 1L))
                .isInstanceOf(BusinessConflictException.class);
        assertThat(waitingRepository.findAll()).hasSize(1);
    }

    @Test
    void 예약이_존재하지_않는_일정에_예약_대기를_생성하면_예외가_발생한다() {
        assertThatThrownBy(() -> waitingService.createWaiting("레서", futureDate, 1L, 1L))
                .isInstanceOf(BusinessConflictException.class);
        assertThat(waitingRepository.findAll()).isEmpty();
    }

    @Test
    void 동일한_일정에_본인의_예약이_이미_있으면_예외가_발생한다() {
        reservationRepository.save(new Reservation(null, "레서", new Schedule(futureDate, time, theme)));

        assertThatThrownBy(() -> waitingService.createWaiting("레서", futureDate, 1L, 1L))
                .isInstanceOf(BusinessConflictException.class);
        assertThat(waitingRepository.findAll()).isEmpty();
    }

    @Test
    void 존재하지_않는_예약_대기_id인_경우_예외가_발생한다() {
        assertThatThrownBy(() -> waitingService.deleteWaiting(999L, "레서"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void 예약_대기의_소유자가_아닌_경우_예외가_발생하고_삭제되지_않는다() {
        Waiting saved = waitingRepository.save(new Waiting(null, "레서", new Schedule(futureDate, time, theme)));

        assertThatThrownBy(() -> waitingService.deleteWaiting(saved.getId(), "밍구"))
                .isInstanceOf(DomainConflictException.class);
        assertThat(waitingRepository.findAll()).hasSize(1);
    }

    @Test
    void 예약_대기의_소유자인_경우_삭제한다() {
        Waiting saved = waitingRepository.save(new Waiting(null, "레서", new Schedule(futureDate, time, theme)));

        waitingService.deleteWaiting(saved.getId(), "레서");

        assertThat(waitingRepository.findAll()).isEmpty();
    }
}