package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.domain.fixture.ReservationFixture.createDefaultReservationWithName;
import static roomescape.domain.fixture.ReservationFixture.createWithNameAndDate;
import static roomescape.support.TestDateTimes.FIXED;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationEntry;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.fixture.ReservationTimeFixture;
import roomescape.domain.fixture.ThemeFixture;
import roomescape.exception.DuplicateEntityException;
import roomescape.exception.EntityNotFoundException;
import roomescape.exception.RoomEscapeException;
import roomescape.query.ReservationQueryRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.service.command.ReservationChangeCommand;
import roomescape.service.command.ReservationCommand;
import roomescape.service.fake.FakeReservationRepository;
import roomescape.service.fake.FakeReservationTimeRepository;
import roomescape.service.fake.FakeThemeRepository;
import roomescape.service.fixture.ReservationServiceFixture;
import roomescape.service.result.ReservationResult;
import roomescape.service.result.ReservationTimeResult;
import roomescape.support.TestDateTimes;

class ReservationServiceTest {

    private ReservationRepository reservationRepository;
    private ReservationTimeRepository reservationTimeRepository;
    private FakeThemeRepository themeRepository;
    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        this.reservationRepository = new FakeReservationRepository();
        this.reservationTimeRepository = new FakeReservationTimeRepository();
        this.themeRepository = new FakeThemeRepository();
        this.reservationService = new ReservationService(
                reservationRepository,
                reservationTimeRepository,
                themeRepository,
                Mockito.mock(ReservationQueryRepository.class),
                TestDateTimes.fixedClock()
        );
    }

    @Test
    void 새로운_예약을_정상적으로_등록한다() {
        // given: 예약 시간이 먼저 등록되어 있음
        ReservationTime time = reservationTimeRepository.save(ReservationTime.create(TestDateTimes.defaultTime()));
        Theme theme = themeRepository.save(ThemeFixture.createDefaultTheme());
        LocalDate reservationDate = FIXED.toLocalDate().plusDays(1);
        ReservationCommand command = ReservationServiceFixture.createReserveCommand("이프", reservationDate,
                theme.getId(), time.getId());

        // when: 예약 진행
        ReservationResult result = reservationService.reserve(command);

        // then: 등록된 예약 정보가 입력값과 일치함
        ReservationTimeResult timeResult = ReservationTimeResult.from(time);
        assertThat(result)
                .extracting(
                        ReservationResult::reservationId,
                        ReservationResult::date,
                        ReservationResult::time
                )
                .containsExactly(1L, reservationDate, timeResult);
        assertThat(result.entry())
                .extracting("id", "name", "status")
                .containsExactly(1L, "이프", "RESERVED");
    }

    @Test
    void 결제_금액이_테마_금액과_일치하지_않으면_예외가_발생한다() {
        // given: 테마 금액은 30000원인데 다른 금액으로 예약을 시도함
        ReservationTime time = reservationTimeRepository.save(ReservationTime.create(TestDateTimes.defaultTime()));
        Theme theme = themeRepository.save(ThemeFixture.createDefaultTheme());
        LocalDate reservationDate = FIXED.toLocalDate().plusDays(1);
        ReservationCommand command = new ReservationCommand("이프", reservationDate, theme.getId(), time.getId(), 9999L);

        // when & then
        assertThatThrownBy(() -> reservationService.reserve(command))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessageContaining("결제 금액이 테마 금액과 일치하지 않습니다.");
    }

    @Test
    void 대기_신청_시_결제_금액이_테마_금액과_일치하지_않으면_예외가_발생한다() {
        // given
        saveDefaultThemeAndTime();
        Reservation existingReservation = createDefaultReservationWithName("기존 예약자");
        reservationRepository.save(existingReservation);

        LocalDate date = existingReservation.getDate();
        ReservationCommand command = new ReservationCommand("새예약자", date, 1L, 1L, 9999L);

        // when & then
        assertThatThrownBy(() -> reservationService.addWaiting(command))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessageContaining("결제 금액이 테마 금액과 일치하지 않습니다.");
    }

    @Test
    void 존재하지_않는_예약_변경_시_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> reservationService.change(1L, null))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("존재하지 않는 예약 정보입니다.");
    }

    @Test
    void 존재하지_않는_시간_정보로_예약_변경_시_예외가_발생한다() {
        // given
        Reservation saved = reservationRepository.save(createDefaultReservationWithName("이프"));
        ReservationChangeCommand command = ReservationServiceFixture.createChangeCommand(FIXED.toLocalDate(), 1L);

        // when & then
        assertThatThrownBy(() -> reservationService.change(reservedEntryId(saved), command))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("존재하지 않는 시간 정보입니다.");
    }

    @Test
    void 동일한_예약_정보로_변경하면_기존_예약을_반환한다() {
        // given
        Reservation reservation = createDefaultReservationWithName("이프");
        reservationTimeRepository.save(reservation.getTime());
        Reservation saved = reservationRepository.save(reservation);

        ReservationChangeCommand command = ReservationServiceFixture.createChangeCommand(reservation.getDate(), 1L);

        // when
        ReservationResult result = reservationService.change(reservedEntryId(saved), command);

        // then
        assertThat(result.reservationId()).isEqualTo(saved.getId());
        assertThat(result.date()).isEqualTo(saved.getDate());
        assertThat(result.time().id()).isEqualTo(saved.getTime().getId());
        assertThat(result.entry().id()).isEqualTo(reservedEntryId(saved));
    }

    @Test
    void 다른_예약이_있는_시간으로_변경하면_대기로_등록된다() {
        // given
        ReservationTime time = reservationTimeRepository.save(ReservationTimeFixture.createDefault());
        themeRepository.save(ThemeFixture.createDefaultTheme());

        Reservation first = reservationRepository.save(createDefaultReservationWithName("이프"));
        Reservation second = reservationRepository.save(createWithNameAndDate("두둠", first.getDate().plusDays(1)));

        ReservationChangeCommand command = ReservationServiceFixture.createChangeCommand(second.getDate(),
                time.getId());

        // when
        ReservationResult result = reservationService.change(reservedEntryId(first), command);

        // then
        assertThat(result.entry().status()).isEqualTo("WAITING");
        assertThat(result.date()).isEqualTo(second.getDate());
    }

    @Test
    void 기존_예약_정보에서_예약_시간을_변경할_수_있다() {
        // given
        Reservation reservation = createDefaultReservationWithName("이프");
        reservationTimeRepository.save(reservation.getTime());
        Reservation saved = reservationRepository.save(reservation);

        LocalDate nextDate = reservation.getDate().plusDays(1);
        ReservationChangeCommand command = ReservationServiceFixture.createChangeCommand(nextDate, 1L);

        // when
        ReservationResult result = reservationService.change(reservedEntryId(saved), command);

        // then
        assertThat(result.date()).isEqualTo(nextDate);
        assertThat(result.time().id()).isEqualTo(1L);
    }

    @Test
    void 비활성화된_테마_정보로_등록_했을_떄_예약하면_예외가_발생한다() {
        // given: 테마 ID가 등록되지 않음
        themeRepository.save(ThemeFixture.createdInactive());
        ReservationCommand command = ReservationServiceFixture.createReserveCommand("이프",
                FIXED.toLocalDate().plusDays(1));

        // when & then: EntityNotFoundException 발생 확인
        assertThatThrownBy(() -> reservationService.reserve(command))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("존재하지 않는 테마 정보입니다.");
    }

    @Test
    void 존재하지_않는_테마_정보로_등록_했을_떄_예약하면_예외가_발생한다() {
        // given: 테마 ID가 등록되지 않음
        ReservationCommand command = ReservationServiceFixture.createReserveCommand("이프",
                FIXED.toLocalDate().plusDays(1));

        // when & then: EntityNotFoundException 발생 확인
        assertThatThrownBy(() -> reservationService.reserve(command))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("존재하지 않는 테마 정보입니다.");
    }

    @Test
    void 존재하지_않는_시간_정보로_등록_했을_떄_예약하면_예외가_발생한다() {
        // given: 테마 ID는 등록되고 시간 ID가 등록되지 않음
        ReservationCommand command = ReservationServiceFixture.createReserveCommand("이프",
                FIXED.toLocalDate().plusDays(1));
        themeRepository.save(ThemeFixture.createDefaultTheme());

        // when & then: EntityNotFoundException 발생 확인
        assertThatThrownBy(() -> reservationService.reserve(command))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("존재하지 않는 시간 정보입니다.");
    }

    @Test
    void 비활성화_된_시간_정보로_등록_했을_떄_예약하면_예외가_발생한다() {
        // given: 테마 ID는 등록되고 시간 ID가 등록되지 않음
        ReservationCommand command = ReservationServiceFixture.createReserveCommand("이프",
                FIXED.toLocalDate().plusDays(1));
        themeRepository.save(ThemeFixture.createDefaultTheme());
        reservationTimeRepository.save(ReservationTimeFixture.createInactive());

        // when & then: EntityNotFoundException 발생 확인
        assertThatThrownBy(() -> reservationService.reserve(command))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("존재하지 않는 시간 정보입니다.");
    }

    @Test
    void 같은_날짜와_같은_시간에_예약을_시도하면_중복_예외가_발생한다() {
        // given: 이미 10시 예약이 존재함
        saveDefaultThemeAndTime();

        Reservation existingReservation = createDefaultReservationWithName("기존 예약자");
        reservationRepository.save(existingReservation);

        LocalDate date = existingReservation.getDate();
        ReservationCommand command = ReservationServiceFixture.createReserveCommand("새예약자", date);

        // when & then
        assertThatThrownBy(() -> reservationService.reserve(command))
                .isInstanceOf(DuplicateEntityException.class)
                .hasMessageContaining("이미 예약 된 날짜입니다.");
    }

    @Test
    void 대기_신청_시_같은_슬롯에_다른_이름으로_신청하면_대기로_등록된다() {
        // given
        saveDefaultThemeAndTime();

        Reservation existingReservation = createDefaultReservationWithName("기존 예약자");
        reservationRepository.save(existingReservation);

        LocalDate date = existingReservation.getDate();
        ReservationCommand command = ReservationServiceFixture.createReserveCommand("새예약자", date);

        // when
        ReservationResult result = reservationService.addWaiting(command);

        // then
        assertThat(result.entry().name()).isEqualTo("새예약자");
        assertThat(result.entry().status()).isEqualTo("WAITING");
    }

    @Test
    void 대기_신청_시_슬롯이_비어있으면_예약으로_승격된다() {
        // given
        saveDefaultThemeAndTime();

        LocalDate date = FIXED.toLocalDate().plusDays(1);
        ReservationCommand command = ReservationServiceFixture.createReserveCommand("이프", date);

        // when
        ReservationResult result = reservationService.addWaiting(command);

        // then: 슬롯이 비어있으므로 RESERVED로 승격
        assertThat(result.entry().name()).isEqualTo("이프");
        assertThat(result.entry().status()).isEqualTo("RESERVED");
    }

    @Test
    void 식별자를_이용해_예약을_취소한다() {
        // given: 취소할 예약이 저장되어 있음
        Reservation saved = reservationRepository.save(createDefaultReservationWithName("웨지"));

        // when: 삭제 요청
        reservationService.cancelReservation(reservedEntryId(saved));

        // then: 예약 엔트리가 취소 상태로 변경됨
        assertThat(reservationRepository.findById(saved.getId()).orElseThrow().getEntries())
                .singleElement()
                .extracting(ReservationEntry::getStatus)
                .isEqualTo(ReservationStatus.DELETED);
    }

    private void saveDefaultThemeAndTime() {
        themeRepository.save(ThemeFixture.createDefaultTheme());
        reservationTimeRepository.save(ReservationTimeFixture.createDefault());
    }

    private long reservedEntryId(Reservation reservation) {
        return reservation.getEntries()
                .stream()
                .filter(ReservationEntry::isReserved)
                .findFirst()
                .orElseThrow()
                .getId();
    }
}
