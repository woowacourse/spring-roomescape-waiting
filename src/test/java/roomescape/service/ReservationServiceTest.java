package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.domain.fixture.ReservationFixture.createDefaultReservationWithName;
import static roomescape.domain.fixture.ReservationFixture.createWithNameAndDate;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import roomescape.domain.DuplicateEntityException;
import roomescape.domain.EntityNotFoundException;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.fixture.ReservationTimeFixture;
import roomescape.domain.fixture.ThemeFixture;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.service.command.ReservationCommand;
import roomescape.service.fake.FakeReservationRepository;
import roomescape.service.fake.FakeReservationTimeRepository;
import roomescape.service.fake.FakeThemeRepository;
import roomescape.service.result.ReservationResult;
import roomescape.service.result.ReservationTimeResult;

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
        ReservationResult result = reservationService.reserve(command);

        // then: 등록된 예약 정보가 입력값과 일치함
        ReservationTimeResult timeResult = ReservationTimeResult.from(time);
        assertThat(result)
                .extracting(
                        ReservationResult::id,
                        ReservationResult::name,
                        ReservationResult::date,
                        ReservationResult::time
                )
                .containsExactly(1L, "이프", reservationDate, timeResult);
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
        ReservationCommand command = new ReservationCommand(null, LocalDate.now(), 1L, 1L);

        // when & then
        assertThatThrownBy(() -> reservationService.change(saved.getId(), command))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("존재하지 않는 시간 정보입니다.");
    }

    @Test
    void 동일한_예약_정보로_변경하면_기존_예약을_반환한다() {
        // given
        Reservation reservation = createDefaultReservationWithName("이프");
        reservationTimeRepository.save(reservation.getTime());
        Reservation saved = reservationRepository.save(reservation);

        ReservationCommand command = new ReservationCommand(null, reservation.getDate(), 1L, 1L);

        // when
        ReservationResult result = reservationService.change(saved.getId(), command);

        // then
        assertThat(result.id()).isEqualTo(saved.getId());
        assertThat(result.date()).isEqualTo(saved.getDate());
        assertThat(result.time().id()).isEqualTo(saved.getTime().getId());
    }

    @Test
    void 다른_예약이_있는_시간으로_변경하면_중복_예외가_발생한다() {
        // given
        ReservationTime time = reservationTimeRepository.save(ReservationTimeFixture.createDefault());
        themeRepository.save(ThemeFixture.createDefaultTheme());

        Reservation first = reservationRepository.save(createDefaultReservationWithName("이프"));
        Reservation second = reservationRepository.save(createWithNameAndDate("두둠", first.getDate().plusDays(1)));

        ReservationCommand command = new ReservationCommand(null, second.getDate(), 1L, time.getId());

        // when & then
        assertThatThrownBy(() -> reservationService.change(first.getId(), command))
                .isInstanceOf(DuplicateEntityException.class)
                .hasMessageContaining("이미 예약 된 날짜입니다.");
    }

    @Test
    void 기존_예약_정보에서_예약_시간을_변경할_수_있다() {
        // given
        Reservation reservation = createDefaultReservationWithName("이프");
        reservationTimeRepository.save(reservation.getTime());
        Reservation saved = reservationRepository.save(reservation);

        LocalDate nextDate = reservation.getDate().plusDays(1);
        ReservationCommand command = new ReservationCommand(null, nextDate, 1L, 1L);

        // when
        ReservationResult result = reservationService.change(saved.getId(), command);

        // then
        assertThat(result.date()).isEqualTo(nextDate);
        assertThat(result.time().id()).isEqualTo(1L);
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
    void 같은_날짜와_같은_시간에_특정_테마로_예약을_시도하면_중복_예외가_발생한다() {
        // given: 1번 이미 10시 예약이 하나 존재함
        themeRepository.save(ThemeFixture.createDefaultTheme());
        reservationTimeRepository.save(ReservationTimeFixture.createDefault());

        Reservation existingReservation = createDefaultReservationWithName("기존 예약자");
        reservationRepository.save(existingReservation);

        LocalDate date = existingReservation.getDate();

        ReservationCommand command = new ReservationCommand("새예약자", date, 1L, 1L);

        // when & then: DuplicateEntityException 발생 확인
        assertThatThrownBy(() -> reservationService.reserve(command))
                .isInstanceOf(DuplicateEntityException.class)
                .hasMessageContaining("이미 예약 된 날짜입니다.");
    }

    @Test
    void 모든_예약_목록을_조회한다() {
        // given: 2개의 예약이 저장되어 있음
        reservationRepository.save(createDefaultReservationWithName("이프"));
        reservationRepository.save(createDefaultReservationWithName("바니"));

        // when: 전체 조회를 요청함
        List<ReservationResult> results = reservationService.getAllReservations();

        // then: 2개의 결과가 반환됨
        assertThat(results).hasSize(2);
    }

    @Test
    void 식별자를_이용해_예약을_취소한다() {
        // given: 취소할 예약이 저장되어 있음
        Reservation saved = reservationRepository.save(createDefaultReservationWithName("웨지"));

        // when: 삭제 요청
        reservationService.cancelReservation(saved.getId());

        // then: 조회 시 목록이 비어있음
        assertThat(reservationService.getAllReservations()).isEmpty();
    }
}
