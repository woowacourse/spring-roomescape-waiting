package roomescape.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import roomescape.domain.*;
import roomescape.domain.exception.DomainConflictException;
import roomescape.service.exception.BusinessConflictException;
import roomescape.service.exception.BusinessException;
import roomescape.service.exception.ErrorCode;
import roomescape.service.exception.ResourceNotFoundException;
import roomescape.service.fake.FakeReservationRepository;
import roomescape.service.fake.FakeReservationTimeRepository;
import roomescape.service.fake.FakeThemeRepository;
import roomescape.service.fake.FakeWaitingRepository;

import java.time.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationServiceTest {

    private FakeReservationRepository reservationRepository;
    private FakeReservationTimeRepository reservationTimeRepository;
    private FakeThemeRepository themeRepository;
    private FakeWaitingRepository waitingRepository;
    private ReservationService reservationService;

    private ReservationTime time;
    private ReservationTime anotherTime;
    private Theme theme;
    private final LocalDate futureDate = LocalDate.of(2026, 5, 10);
    private final LocalDate anotherFutureDate = LocalDate.of(2026, 5, 11);
    private final LocalDate pastDate = LocalDate.of(2026, 4, 1);

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-05-01T00:00:00Z"), ZoneOffset.UTC);
        reservationRepository = new FakeReservationRepository();
        reservationTimeRepository = new FakeReservationTimeRepository();
        themeRepository = new FakeThemeRepository();
        waitingRepository = new FakeWaitingRepository();

        time = new ReservationTime(1L, LocalTime.of(10, 0));
        anotherTime = new ReservationTime(2L, LocalTime.of(12, 0));
        theme = new Theme(1L, "공포방", "무서운방입니다.", "image-url");
        reservationTimeRepository.add(time);
        reservationTimeRepository.add(anotherTime);
        themeRepository.add(theme);

        reservationService = new ReservationService(
                reservationRepository, reservationTimeRepository, themeRepository, waitingRepository, fixedClock);
    }

    @Test
    void 예약_날짜가_과거인_경우_도메인_충돌_예외가_발생한다() {
        assertThatThrownBy(() -> reservationService.createReservation("레서", pastDate, 1L, 1L))
                .isInstanceOf(DomainConflictException.class)
                .hasMessage("지난 시간으로는 예약할 수 없습니다.");

        assertThat(reservationRepository.findAll()).isEmpty();
    }

    @Test
    void 이미_예약된_예약_시간인_경우_예외가_발생한다() {
        reservationRepository.save(new Reservation(null, "브라운", new Schedule(futureDate, time, theme)));

        assertThatThrownBy(() -> reservationService.createReservation("레서", futureDate, 1L, 1L))
                .isInstanceOf(BusinessConflictException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.DUPLICATE_RESERVATION);

        assertThat(reservationRepository.findAll())
                .extracting(Reservation::getName)
                .containsExactly("브라운");
    }

    @Test
    void 존재하지_않는_시간으로_예약을_생성하면_예외가_발생하고_예약을_저장하지_않는다() {
        assertThatThrownBy(() -> reservationService.createReservation("브라운", futureDate, 999L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.RESERVATION_TIME_NOT_FOUND);

        assertThat(reservationRepository.findAll()).isEmpty();
    }

    @Test
    void 존재하지_않는_테마로_예약을_생성하면_예외가_발생하고_예약을_저장하지_않는다() {
        assertThatThrownBy(() -> reservationService.createReservation("브라운", futureDate, 1L, 999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.THEME_NOT_FOUND);

        assertThat(reservationRepository.findAll()).isEmpty();
    }

    @Test
    void 예약을_생성하면_저장된다() {
        Reservation created = reservationService.createReservation("레서", futureDate, 1L, 1L);

        assertThat(created.getName()).isEqualTo("레서");
        assertThat(reservationRepository.findAll())
                .extracting(Reservation::getName)
                .containsExactly("레서");
    }

    @Test
    void 존재하지_않는_예약을_변경하면_예외가_발생한다() {
        assertThatThrownBy(() -> reservationService.updateReservation(999L, "브라운", anotherFutureDate, 2L))
                .isInstanceOf(ResourceNotFoundException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.RESERVATION_NOT_FOUND);
    }

    @Test
    void 본인의_예약이_아닌_경우_변경하면_도메인_충돌_예외가_발생한다() {
        Reservation saved = reservationRepository.save(
                new Reservation(null, "브라운", new Schedule(futureDate, time, theme)));

        assertThatThrownBy(() -> reservationService.updateReservation(saved.getId(), "어셔", anotherFutureDate, 2L))
                .isInstanceOf(DomainConflictException.class)
                .hasMessage("본인의 예약만 수정할 수 있습니다.");

        Reservation unchanged = reservationRepository.findById(saved.getId()).orElseThrow();
        assertThat(unchanged.getSchedule().getDate()).isEqualTo(futureDate);
        assertThat(unchanged.getSchedule().getTime().getId()).isEqualTo(1L);
    }

    @Test
    void 존재하지_않는_시간으로_예약을_변경하면_예외가_발생한다() {
        Reservation saved = reservationRepository.save(
                new Reservation(null, "브라운", new Schedule(futureDate, time, theme)));

        assertThatThrownBy(() -> reservationService.updateReservation(saved.getId(), "브라운", anotherFutureDate, 999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.RESERVATION_TIME_NOT_FOUND);

        Reservation unchanged = reservationRepository.findById(saved.getId()).orElseThrow();
        assertThat(unchanged.getSchedule().getDate()).isEqualTo(futureDate);
    }

    @Test
    void 이미_예약된_날짜와_시간으로_예약을_변경하면_예외가_발생한다() {
        Reservation target = reservationRepository.save(
                new Reservation(null, "브라운", new Schedule(futureDate, time, theme)));
        reservationRepository.save(
                new Reservation(null, "어셔", new Schedule(anotherFutureDate, anotherTime, theme)));

        assertThatThrownBy(() -> reservationService.updateReservation(target.getId(), "브라운", anotherFutureDate, 2L))
                .isInstanceOf(BusinessConflictException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.DUPLICATE_RESERVATION);

        Reservation unchanged = reservationRepository.findById(target.getId()).orElseThrow();
        assertThat(unchanged.getSchedule().getDate()).isEqualTo(futureDate);
        assertThat(unchanged.getSchedule().getTime().getId()).isEqualTo(1L);
    }

    @Test
    void 존재하지_않는_예약을_삭제하면_예외가_발생한다() {
        assertThatThrownBy(() -> reservationService.deleteUserReservation(999L, "레서"))
                .isInstanceOf(ResourceNotFoundException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.RESERVATION_NOT_FOUND);
    }

    @Test
    void 본인의_예약이_아닌_예약을_삭제하면_도메인_충돌_예외가_발생한다() {
        Reservation saved = reservationRepository.save(
                new Reservation(null, "브라운", new Schedule(futureDate, time, theme)));

        assertThatThrownBy(() -> reservationService.deleteUserReservation(saved.getId(), "레서"))
                .isInstanceOf(DomainConflictException.class)
                .hasMessage("본인의 예약만 수정할 수 있습니다.");

        assertThat(reservationRepository.findAll()).hasSize(1);
    }

    @Test
    void 예약을_삭제하면_첫_번째_대기자가_예약으로_확정되고_대기에서_제거된다() {
        Schedule schedule = new Schedule(futureDate, time, theme);
        Reservation reservation = reservationRepository.save(new Reservation(null, "브라운", schedule));
        waitingRepository.save(new Waiting(null, "레서", schedule));
        waitingRepository.save(new Waiting(null, "밍구", schedule));

        reservationService.deleteUserReservation(reservation.getId(), "브라운");

        assertThat(reservationRepository.findAll())
                .extracting(Reservation::getName)
                .containsExactly("레서");
        assertThat(waitingRepository.findAll())
                .extracting(Waiting::getName)
                .containsExactly("밍구");
    }

    @Test
    void 예약을_삭제했지만_대기자가_없으면_아무도_승격되지_않는다() {
        Schedule schedule = new Schedule(futureDate, time, theme);
        Reservation reservation = reservationRepository.save(new Reservation(null, "브라운", schedule));

        reservationService.deleteUserReservation(reservation.getId(), "브라운");

        assertThat(reservationRepository.findAll()).isEmpty();
        assertThat(waitingRepository.findAll()).isEmpty();
    }
}
