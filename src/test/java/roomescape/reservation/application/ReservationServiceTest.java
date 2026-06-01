package roomescape.reservation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.ConflictException;
import roomescape.common.exception.ForbiddenException;
import roomescape.common.exception.InactiveException;
import roomescape.common.exception.NotFoundException;
import roomescape.reservation.domain.fixture.ReservationFixture;
import roomescape.theme.domain.fixture.ThemeFixture;
import roomescape.reservation.domain.fake.FakeReservationRepository;
import roomescape.time.domain.fake.FakeReservationTimeRepository;
import roomescape.theme.domain.fake.FakeThemeRepository;
import roomescape.reservation.application.ReservationService;
import roomescape.reservation.application.dto.ReservationChangeCommand;
import roomescape.reservation.application.dto.ReservationCreateCommand;
import roomescape.reservation.application.dto.ReservationInfo;
import roomescape.reservation.application.dto.ReservationPendingInfo;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.Status;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.ReservationTimeRepository;

class ReservationServiceTest {

    private Clock clock;
    private ReservationRepository reservationRepository;
    private ReservationTimeRepository reservationTimeRepository;
    private ThemeRepository themeRepository;
    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        this.clock = ReservationFixture.FIXED_CLOCK;
        this.reservationRepository = new FakeReservationRepository();
        this.reservationTimeRepository = new FakeReservationTimeRepository();
        this.themeRepository = new FakeThemeRepository();
        this.reservationService = new ReservationService(clock, reservationRepository, reservationTimeRepository,
                themeRepository);
    }

    @Test
    void 새로운_예약을_정상적으로_등록한다() {
        // given
        ReservationTime time = reservationTimeRepository.save(ReservationTime.create(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(ThemeFixture.createDefaultTheme());
        LocalDate reservationDate = LocalDate.now(clock).plusDays(1);
        ReservationCreateCommand command = new ReservationCreateCommand("바니", reservationDate, time.getId(),
                theme.getId());

        // when
        ReservationInfo response = reservationService.create(command);

        // then
        assertThat(response).extracting(ReservationInfo::id, ReservationInfo::name, ReservationInfo::date,
                        ReservationInfo::status)
                .containsExactly(1L, "바니", reservationDate, Status.RESERVED);
    }

    @Test
    void 존재하지_않는_테마_정보로_예약하면_예외가_발생한다() {
        // given
        ReservationTime time = reservationTimeRepository.save(ReservationTime.create(LocalTime.of(10, 0)));
        ReservationCreateCommand command = new ReservationCreateCommand("바니", LocalDate.now(clock).plusDays(1),
                time.getId(), 1L);

        // when & then
        assertThatThrownBy(() -> reservationService.create(command)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void 존재하지_않는_시간_정보로_예약하면_예외가_발생한다() {
        // given
        Theme theme = themeRepository.save(ThemeFixture.createDefaultTheme());
        ReservationCreateCommand command = new ReservationCreateCommand("바니", LocalDate.now(clock).plusDays(1),
                1L, theme.getId());

        // when & then
        assertThatThrownBy(() -> reservationService.create(command)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void 비활성화된_테마로_예약하면_예외가_발생한다() {
        // given
        ReservationTime time = reservationTimeRepository.save(ReservationTime.create(LocalTime.of(10, 0)));
        Theme inactiveTheme = themeRepository.save(ThemeFixture.createDefaultTheme().deactivate());
        ReservationCreateCommand command = new ReservationCreateCommand("바니", LocalDate.now(clock).plusDays(1),
                time.getId(), inactiveTheme.getId());

        // when & then
        assertThatThrownBy(() -> reservationService.create(command)).isInstanceOf(InactiveException.class);
    }

    @Test
    void 비활성화된_시간대로_예약하면_예외가_발생한다() {
        // given
        ReservationTime inactiveTime = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.of(10, 0)).deactivate());
        Theme theme = themeRepository.save(ThemeFixture.createDefaultTheme());
        ReservationCreateCommand command = new ReservationCreateCommand("바니", LocalDate.now(clock).plusDays(1),
                inactiveTime.getId(), theme.getId());

        // when & then
        assertThatThrownBy(() -> reservationService.create(command)).isInstanceOf(InactiveException.class);
    }

    @Test
    void 같은_이름으로_같은_날짜_시간_테마에_예약하면_예외가_발생한다() {
        // given
        ReservationTime time = reservationTimeRepository.save(ReservationTime.create(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(ThemeFixture.createDefaultTheme());
        Reservation reservation = reservationRepository.save(ReservationFixture.createDefaultReservation("바니", theme, time));
        ReservationCreateCommand command = new ReservationCreateCommand("바니", reservation.getDate(), time.getId(),
                theme.getId());

        // when & then
        assertThatThrownBy(() -> reservationService.create(command)).isInstanceOf(ConflictException.class);
    }

    @Test
    void 이미_예약된_일시와_테마에_다른_이름으로_예약하면_대기_예약이_된다() {
        // given
        ReservationTime time = reservationTimeRepository.save(ReservationTime.create(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(ThemeFixture.createDefaultTheme());
        Reservation reservation = reservationRepository.save(ReservationFixture.createDefaultReservation("바니", theme, time));
        ReservationCreateCommand command = new ReservationCreateCommand("포비", reservation.getDate(), time.getId(),
                theme.getId());

        // when
        ReservationInfo response = reservationService.create(command);

        // then
        assertThat(response.status()).isEqualTo(Status.WAITING);
    }

    @Test
    void 예약자_이름으로_예약_내역을_조회한다() {
        // given
        reservationRepository.save(ReservationFixture.createDefaultReservation("바니"));
        reservationRepository.save(ReservationFixture.createDefaultReservation("포비"));

        // when
        List<ReservationPendingInfo> responses = reservationService.getReservationsByName("바니");

        // then
        assertThat(responses).hasSize(1).extracting(ReservationPendingInfo::name).containsExactly("바니");
    }

    @Test
    void 예약자_이름으로_예약을_취소한다() {
        // given
        Reservation reservation = reservationRepository.save(ReservationFixture.createDefaultReservation("바니"));

        // when & then
        assertThatCode(() -> reservationService.cancel(reservation.getId(), "바니")).doesNotThrowAnyException();
    }

    @Test
    void 예약자_정보가_일치하지_않는_예약을_취소하면_예외가_발생한다() {
        // given
        Reservation reservation = reservationRepository.save(ReservationFixture.createDefaultReservation("바니"));

        // when & then
        assertThatThrownBy(() -> reservationService.cancel(reservation.getId(), "포비")).isInstanceOf(
                ForbiddenException.class);
    }

    @Test
    void 존재하지_않는_예약을_취소하면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> reservationService.cancel(1L, "바니")).isInstanceOf(NotFoundException.class);
    }

    @Test
    void 예약을_취소하면_가장_먼저_등록된_대기_예약이_승인된다() {
        // given
        ReservationTime time = reservationTimeRepository.save(ReservationTime.create(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(ThemeFixture.createDefaultTheme());
        Reservation reserved = reservationRepository.save(ReservationFixture.createDefaultReservation("바니", theme, time));
        Reservation waiting = reservationRepository.save(ReservationFixture.createWaitingReservation("포비", theme, time));

        // when
        reservationService.cancel(reserved.getId(), "바니");

        // then
        assertThat(reservationRepository.getById(waiting.getId()).getStatus()).isEqualTo(Status.RESERVED);
    }

    @Test
    void 예약_정보를_수정한다() {
        // given
        ReservationTime time = reservationTimeRepository.save(ReservationTime.create(LocalTime.of(10, 0)));
        ReservationTime changedTime = reservationTimeRepository.save(ReservationTime.create(LocalTime.of(11, 0)));
        Theme theme = themeRepository.save(ThemeFixture.createDefaultTheme());
        Reservation reservation = reservationRepository.save(ReservationFixture.createDefaultReservation("바니", theme, time));
        ReservationChangeCommand command = new ReservationChangeCommand("바니", changedTime.getId(), theme.getId(),
                reservation.getDate());

        // when
        ReservationInfo response = reservationService.modify(reservation.getId(), command);

        // then
        assertThat(response.time().id()).isEqualTo(changedTime.getId());
    }

    @Test
    void 존재하지_않는_예약을_수정하면_예외가_발생한다() {
        // given
        ReservationTime time = reservationTimeRepository.save(ReservationTime.create(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(ThemeFixture.createDefaultTheme());
        ReservationChangeCommand command = new ReservationChangeCommand("바니", time.getId(), theme.getId(),
                LocalDate.now(clock).plusDays(1));

        // when & then
        assertThatThrownBy(() -> reservationService.modify(1L, command)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void 존재하지_않는_시간_정보로_예약을_수정하면_예외가_발생한다() {
        // given
        Theme theme = themeRepository.save(ThemeFixture.createDefaultTheme());
        Reservation reservation = reservationRepository.save(ReservationFixture.createDefaultReservation("바니"));
        ReservationChangeCommand command = new ReservationChangeCommand("바니", 1L, theme.getId(),
                reservation.getDate());

        // when & then
        assertThatThrownBy(() -> reservationService.modify(reservation.getId(), command))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void 존재하지_않는_테마_정보로_예약을_수정하면_예외가_발생한다() {
        // given
        ReservationTime time = reservationTimeRepository.save(ReservationTime.create(LocalTime.of(10, 0)));
        Reservation reservation = reservationRepository.save(ReservationFixture.createDefaultReservation("바니"));
        ReservationChangeCommand command = new ReservationChangeCommand("바니", time.getId(), 1L,
                reservation.getDate());

        // when & then
        assertThatThrownBy(() -> reservationService.modify(reservation.getId(), command))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void 이미_취소된_예약을_수정하면_예외가_발생한다() {
        // given
        ReservationTime time = reservationTimeRepository.save(ReservationTime.create(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(ThemeFixture.createDefaultTheme());
        Reservation reservation = reservationRepository.save(
                ReservationFixture.createDefaultReservation("바니", theme, time).cancel());
        ReservationChangeCommand command = new ReservationChangeCommand("바니", time.getId(), theme.getId(),
                reservation.getDate());

        // when & then
        assertThatThrownBy(() -> reservationService.modify(reservation.getId(), command))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void 수정하려는_날짜_시간_테마에_같은_이름의_예약이_있으면_예외가_발생한다() {
        // given
        ReservationTime time = reservationTimeRepository.save(ReservationTime.create(LocalTime.of(10, 0)));
        ReservationTime changedTime = reservationTimeRepository.save(ReservationTime.create(LocalTime.of(11, 0)));
        Theme theme = themeRepository.save(ThemeFixture.createDefaultTheme());
        Reservation reservation = reservationRepository.save(ReservationFixture.createDefaultReservation("바니", theme, time));
        Reservation duplicated = reservationRepository.save(
                ReservationFixture.createDefaultReservation("바니", theme, changedTime));
        ReservationChangeCommand command = new ReservationChangeCommand("바니", changedTime.getId(), theme.getId(),
                duplicated.getDate());

        // when & then
        assertThatThrownBy(() -> reservationService.modify(reservation.getId(), command))
                .isInstanceOf(ConflictException.class);
    }
}
