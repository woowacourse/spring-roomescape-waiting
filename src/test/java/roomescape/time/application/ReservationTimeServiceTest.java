package roomescape.time.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.ConflictException;
import roomescape.common.exception.DuplicateException;
import roomescape.common.exception.NotFoundException;
import roomescape.reservation.domain.fixture.ReservationFixture;
import roomescape.theme.domain.fixture.ThemeFixture;
import roomescape.reservation.domain.fake.FakeReservationRepository;
import roomescape.time.domain.fake.FakeReservationTimeRepository;
import roomescape.theme.domain.fake.FakeThemeRepository;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;
import roomescape.time.application.ReservationTimeService;
import roomescape.time.application.dto.AvailableReservationTimeFindCommand;
import roomescape.time.application.dto.AvailableReservationTimeInfo;
import roomescape.time.application.dto.ReservationTimeCommand;
import roomescape.time.application.dto.ReservationTimeInfo;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.ReservationTimeRepository;

class ReservationTimeServiceTest {

    private ReservationTimeRepository reservationTimeRepository;
    private ReservationRepository reservationRepository;
    private ThemeRepository themeRepository;
    private ReservationTimeService reservationTimeService;

    @BeforeEach
    void setUp() {
        this.reservationTimeRepository = new FakeReservationTimeRepository();
        this.reservationRepository = new FakeReservationRepository();
        this.themeRepository = new FakeThemeRepository();
        this.reservationTimeService = new ReservationTimeService(reservationTimeRepository, reservationRepository,
                themeRepository);
    }

    @Test
    void 새로운_예약_시간을_정상적으로_등록한다() {
        // given
        ReservationTimeCommand command = new ReservationTimeCommand(LocalTime.of(10, 0));

        // when
        ReservationTimeInfo response = reservationTimeService.create(command);

        // then
        assertThat(response).extracting(ReservationTimeInfo::id, ReservationTimeInfo::startAt)
                .containsExactly(1L, LocalTime.of(10, 0));
    }

    @Test
    void 이미_존재하는_활성_시간을_등록하면_예외가_발생한다() {
        // given
        reservationTimeRepository.save(ReservationTime.create(LocalTime.of(10, 0)));
        ReservationTimeCommand command = new ReservationTimeCommand(LocalTime.of(10, 0));

        // when & then
        assertThatThrownBy(() -> reservationTimeService.create(command)).isInstanceOf(DuplicateException.class);
    }

    @Test
    void 전체_예약_시간을_조회한다() {
        // given
        reservationTimeRepository.save(ReservationTime.create(LocalTime.of(11, 0)));
        reservationTimeRepository.save(ReservationTime.create(LocalTime.of(10, 0)));

        // when
        List<ReservationTimeInfo> responses = reservationTimeService.getReservationTimes();

        // then
        assertThat(responses).extracting(ReservationTimeInfo::startAt)
                .containsExactly(LocalTime.of(10, 0), LocalTime.of(11, 0));
    }

    @Test
    void 예약이_없는_시간대를_비활성화한다() {
        // given
        ReservationTime time = reservationTimeRepository.save(ReservationTime.create(LocalTime.of(10, 0)));

        // when
        reservationTimeService.deactivate(time.getId());

        // then
        assertThat(reservationTimeRepository.getById(time.getId()).isActive()).isFalse();
    }

    @Test
    void 존재하지_않는_시간대를_비활성화하면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> reservationTimeService.deactivate(1L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void 예약이_존재하는_시간대를_비활성화하면_예외가_발생한다() {
        // given
        ReservationTime time = reservationTimeRepository.save(ReservationTime.create(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(ThemeFixture.createDefaultTheme());
        reservationRepository.save(ReservationFixture.createDefaultReservation("바니", theme, time));

        // when & then
        assertThatThrownBy(() -> reservationTimeService.deactivate(time.getId())).isInstanceOf(ConflictException.class);
    }

    @Test
    void 대기_예약만_존재하는_시간대는_비활성화할_수_있다() {
        // given
        ReservationTime time = reservationTimeRepository.save(ReservationTime.create(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(ThemeFixture.createDefaultTheme());
        reservationRepository.save(ReservationFixture.createWaitingReservation("바니", theme, time));

        // when
        reservationTimeService.deactivate(time.getId());

        // then
        assertThat(reservationTimeRepository.getById(time.getId()).isActive()).isFalse();
    }

    @Test
    void 테마와_날짜로_예약_가능한_시간을_조회한다() {
        // given
        Theme theme = themeRepository.save(ThemeFixture.createDefaultTheme());
        ReservationTime reservedTime = reservationTimeRepository.save(ReservationTime.create(LocalTime.of(10, 0)));
        ReservationTime availableTime = reservationTimeRepository.save(ReservationTime.create(LocalTime.of(11, 0)));
        reservationRepository.save(ReservationFixture.createDefaultReservation("바니", theme, reservedTime));
        AvailableReservationTimeFindCommand command = new AvailableReservationTimeFindCommand(theme.getId(),
                LocalDate.now(ReservationFixture.FIXED_CLOCK).plusDays(1));

        // when
        AvailableReservationTimeInfo response = reservationTimeService.getAvailableReservationTime(command);

        // then
        assertThat(response.times()).extracting(ReservationTimeInfo::id).containsExactly(availableTime.getId());
    }

    @Test
    void 존재하지_않는_테마로_예약_가능한_시간을_조회하면_예외가_발생한다() {
        // given
        AvailableReservationTimeFindCommand command = new AvailableReservationTimeFindCommand(1L,
                LocalDate.now(ReservationFixture.FIXED_CLOCK).plusDays(1));

        // when & then
        assertThatThrownBy(() -> reservationTimeService.getAvailableReservationTime(command))
                .isInstanceOf(NotFoundException.class);
    }
}
