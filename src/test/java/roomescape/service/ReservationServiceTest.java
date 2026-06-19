package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;
import static roomescape.domain.fixture.ReservationFixture.createDefaultReservationWithName;
import static roomescape.domain.fixture.ReservationFixture.createWithNameAndDate;
import static roomescape.domain.fixture.ReservationFixture.reservedReservationId;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import roomescape.application.service.ReservationService;
import roomescape.application.service.command.ReservationChangeCommand;
import roomescape.application.service.command.ReservationCommand;
import roomescape.application.service.result.ReservationSlotResult;
import roomescape.application.service.result.ReservationTimeResult;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationActiveStatus;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.fixture.ReservationTimeFixture;
import roomescape.domain.fixture.ThemeFixture;
import roomescape.exception.DuplicateEntityException;
import roomescape.exception.EntityNotFoundException;
import roomescape.persistence.ReservationSlotRepository;
import roomescape.persistence.ReservationTimeRepository;
import roomescape.persistence.dto.ReservationCondition;
import roomescape.service.fake.FakeReservationSlotRepository;
import roomescape.service.fake.FakeReservationTimeRepository;
import roomescape.service.fake.FakeThemeRepository;
import roomescape.service.fixture.ReservationServiceFixture;

class ReservationServiceTest {

    private ReservationSlotRepository reservationRepository;
    private ReservationTimeRepository reservationTimeRepository;
    private FakeThemeRepository themeRepository;
    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        this.reservationRepository = new FakeReservationSlotRepository();
        this.reservationTimeRepository = new FakeReservationTimeRepository();
        this.themeRepository = new FakeThemeRepository();
        this.reservationService = new ReservationService(reservationRepository, reservationTimeRepository, themeRepository);
    }

    @Test
    void 새로운_예약을_정상적으로_등록한다() {
        // given: 예약 시간이 먼저 등록되어 있음
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(ThemeFixture.createDefaultTheme());
        LocalDate reservationDate = LocalDate.now().plusDays(1);
        ReservationCommand command = new ReservationCommand("이프", reservationDate, theme.getId(), time.getId());

        // when: 예약 진행
        ReservationSlotResult result = reservationService.reserve(command);

        // then: 등록된 예약 정보가 입력값과 일치함
        ReservationTimeResult timeResult = ReservationTimeResult.from(time);
        assertThat(result)
                .extracting(
                        ReservationSlotResult::slotId,
                        ReservationSlotResult::date,
                        ReservationSlotResult::time
                )
                .containsExactly(1L, reservationDate, timeResult);
        assertThat(result.reservation())
                .extracting("id", "name", "status")
                .containsExactly(1L, "이프", "RESERVED");
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
        ReservationSlot saved = reservationRepository.save(createDefaultReservationWithName("이프"));
        ReservationChangeCommand command = ReservationServiceFixture.createChangeCommand(LocalDate.now(), 1L);

        // when & then
        assertThatThrownBy(() -> reservationService.change(reservedReservationId(saved), command))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("존재하지 않는 시간 정보입니다.");
    }

    @Test
    void 동일한_예약_정보로_변경하면_기존_예약을_반환한다() {
        // given
        ReservationSlot slot = createDefaultReservationWithName("이프");
        reservationTimeRepository.save(slot.getTime());
        ReservationSlot saved = reservationRepository.save(slot);

        ReservationChangeCommand command = ReservationServiceFixture.createChangeCommand(slot.getDate(), 1L);

        // when
        ReservationSlotResult result = reservationService.change(reservedReservationId(saved), command);

        // then
        assertThat(result.slotId()).isEqualTo(saved.getId());
        assertThat(result.date()).isEqualTo(saved.getDate());
        assertThat(result.time().id()).isEqualTo(saved.getTime().getId());
        assertThat(result.reservation().id()).isEqualTo(reservedReservationId(saved));
    }

    @Test
    void 다른_예약이_있는_시간으로_변경하면_중복_예외가_발생한다() {
        // given
        ReservationTime time = reservationTimeRepository.save(ReservationTimeFixture.createDefault());
        themeRepository.save(ThemeFixture.createDefaultTheme());

        ReservationSlot first = reservationRepository.save(createDefaultReservationWithName("이프"));
        ReservationSlot second = reservationRepository.save(createWithNameAndDate("두둠", first.getDate().plusDays(1)));

        ReservationChangeCommand command = ReservationServiceFixture.createChangeCommand(second.getDate(), time.getId());

        // when & then
        assertThatThrownBy(() -> reservationService.change(reservedReservationId(first), command))
                .isInstanceOf(DuplicateEntityException.class)
                .hasMessageContaining("이미 예약 된 날짜입니다.");
    }

    @Test
    void 변경하려는_시간에_같은_이름의_대기가_있으면_중복_예외가_발생한다() {
        // given
        ReservationTime time = reservationTimeRepository.save(ReservationTimeFixture.createDefault());
        themeRepository.save(ThemeFixture.createDefaultTheme());

        LocalDate currentDate = LocalDate.now().plusDays(2);
        LocalDate targetDate = currentDate.plusDays(1);

        ReservationSlot current = reservationRepository.save(createWithNameAndDate("이프", currentDate));
        reservationService.reserve(new ReservationCommand("찰리", targetDate, 1L, time.getId()));
        reservationService.addWaiting(new ReservationCommand("이프", targetDate, 1L, time.getId()));

        ReservationChangeCommand command = ReservationServiceFixture.createChangeCommand(targetDate, time.getId());

        // when & then
        assertThatThrownBy(() -> reservationService.change(reservedReservationId(current), command))
                .isInstanceOf(DuplicateEntityException.class)
                .hasMessageContaining("이미 예약 또는 대기가 존재합니다.");
    }

    @Test
    void 기존_예약_정보에서_예약_시간을_변경할_수_있다() {
        // given
        ReservationSlot slot = createDefaultReservationWithName("이프");
        reservationTimeRepository.save(slot.getTime());
        ReservationSlot saved = reservationRepository.save(slot);

        LocalDate nextDate = slot.getDate().plusDays(1);
        ReservationChangeCommand command = ReservationServiceFixture.createChangeCommand(nextDate, 1L);

        // when
        ReservationSlotResult result = reservationService.change(reservedReservationId(saved), command);

        // then
        assertThat(result.date()).isEqualTo(nextDate);
        assertThat(result.time().id()).isEqualTo(1L);
    }

    @Test
    void 예약_변경_시_기존_예약의_첫_번째_대기가_예약으로_승격된다() {
        // given
        ReservationSlot slot = createDefaultReservationWithName("이프");
        slot.joinWaitingList("찰리");

        ReservationTime time = slot.getTime();
        reservationTimeRepository.save(time);
        ReservationSlot saved = reservationRepository.save(slot);

        LocalDate nextDate = slot.getDate().plusDays(1);
        ReservationChangeCommand command = ReservationServiceFixture.createChangeCommand(nextDate, time.getId());

        // when
        reservationService.change(reservedReservationId(saved), command);

        // then
        ReservationCondition condition = new ReservationCondition(slot.getDate(), slot.getTheme().getId(), slot.getTime().getId());
        ReservationSlot current = reservationRepository.findByDateAndThemeAndTimeForUpdate(condition).orElseThrow();
        assertThat(current.getReservations())
                .extracting(Reservation::getName, Reservation::getStatus, Reservation::getActiveStatus)
                .containsExactly(
                        tuple("이프", ReservationStatus.RESERVED, ReservationActiveStatus.CANCELED),
                        tuple("찰리", ReservationStatus.RESERVED, ReservationActiveStatus.ACTIVE)
                );
    }

    @Test
    void 비활성화된_테마_정보로_등록_했을_떄_예약하면_예외가_발생한다() {
        // given: 테마 ID가 등록되지 않음
        themeRepository.save(ThemeFixture.createdInactive());
        ReservationCommand command = new ReservationCommand("이프", LocalDate.now().plusDays(1), 1L, 1L);

        // when & then: EntityNotFoundException 발생 확인
        assertThatThrownBy(() -> reservationService.reserve(command))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("존재하지 않는 테마 정보입니다.");
    }

    @Test
    void 존재하지_않는_테마_정보로_등록_했을_떄_예약하면_예외가_발생한다() {
        // given: 테마 ID가 등록되지 않음
        ReservationCommand command = new ReservationCommand("이프", LocalDate.now().plusDays(1), 1L, 1L);

        // when & then: EntityNotFoundException 발생 확인
        assertThatThrownBy(() -> reservationService.reserve(command))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("존재하지 않는 테마 정보입니다.");
    }

    @Test
    void 존재하지_않는_시간_정보로_등록_했을_떄_예약하면_예외가_발생한다() {
        // given: 테마 ID는 등록되고 시간 ID가 등록되지 않음
        ReservationCommand command = new ReservationCommand("이프", LocalDate.now().plusDays(1), 1L, 1L);
        themeRepository.save(ThemeFixture.createDefaultTheme());

        // when & then: EntityNotFoundException 발생 확인
        assertThatThrownBy(() -> reservationService.reserve(command))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("존재하지 않는 시간 정보입니다.");
    }

    @Test
    void 비활성화_된_시간_정보로_등록_했을_떄_예약하면_예외가_발생한다() {
        // given: 테마 ID는 등록되고 시간 ID가 등록되지 않음
        ReservationCommand command = new ReservationCommand("이프", LocalDate.now().plusDays(1), 1L, 1L);
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
        themeRepository.save(ThemeFixture.createDefaultTheme());
        reservationTimeRepository.save(ReservationTimeFixture.createDefault());

        ReservationSlot existingReservation = createDefaultReservationWithName("기존 예약자");
        reservationRepository.save(existingReservation);

        LocalDate date = existingReservation.getDate();
        ReservationCommand command = new ReservationCommand("새예약자", date, 1L, 1L);

        // when & then
        assertThatThrownBy(() -> reservationService.reserve(command))
                .isInstanceOf(DuplicateEntityException.class)
                .hasMessageContaining("이미 예약 된 날짜입니다.");
    }

    @Test
    void 대기_신청_시_같은_슬롯에_다른_이름으로_신청하면_대기로_등록된다() {
        // given
        themeRepository.save(ThemeFixture.createDefaultTheme());
        reservationTimeRepository.save(ReservationTimeFixture.createDefault());

        ReservationSlot existingReservation = createDefaultReservationWithName("기존 예약자");
        reservationRepository.save(existingReservation);

        LocalDate date = existingReservation.getDate();
        ReservationCommand command = new ReservationCommand("새예약자", date, 1L, 1L);

        // when
        ReservationSlotResult result = reservationService.addWaiting(command);

        // then
        assertThat(result.reservation().name()).isEqualTo("새예약자");
        assertThat(result.reservation().status()).isEqualTo("WAITING");
    }

    @Test
    void 대기_신청_시_슬롯이_비어있으면_예약으로_승격된다() {
        // given
        themeRepository.save(ThemeFixture.createDefaultTheme());
        reservationTimeRepository.save(ReservationTimeFixture.createDefault());

        LocalDate date = LocalDate.now().plusDays(1);
        ReservationCommand command = new ReservationCommand("이프", date, 1L, 1L);

        // when
        ReservationSlotResult result = reservationService.addWaiting(command);

        // then: 슬롯이 비어있으므로 RESERVED로 승격
        assertThat(result.reservation().name()).isEqualTo("이프");
        assertThat(result.reservation().status()).isEqualTo("RESERVED");
    }

    @Test
    void 식별자를_이용해_예약을_취소한다() {
        // given: 취소할 예약이 저장되어 있음
        ReservationSlot saved = reservationRepository.save(createDefaultReservationWithName("웨지"));
        long reservationId = reservedReservationId(saved);

        // when: 삭제 요청
        reservationService.cancelReservation(reservationId);

        // then: 예약 엔트리가 취소 상태로 변경됨
        assertThat(reservationRepository.findByReservationIdForUpdate(reservationId).orElseThrow()
                .getReservations())
                .singleElement()
                .extracting(Reservation::getStatus, Reservation::getActiveStatus)
                .containsExactly(ReservationStatus.RESERVED, ReservationActiveStatus.CANCELED);
    }

}
