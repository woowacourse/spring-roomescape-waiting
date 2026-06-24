package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationAndWaiting;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.theme.Theme;
import roomescape.domain.timeslot.TimeSlot;
import roomescape.exception.DuplicateException;
import roomescape.exception.NotOwnerException;
import roomescape.exception.PastTimeException;
import roomescape.repository.FakeReservationRepository;
import roomescape.repository.FakeReservationSlotRepository;
import roomescape.repository.FakeThemeRepository;
import roomescape.repository.FakeTimeSlotRepository;

class ReservationServiceTest {

    private static final LocalDateTime REQUEST_TIME = LocalDateTime.now();

    private ReservationService reservationService;
    private FakeReservationRepository reservationRepository;
    private FakeReservationSlotRepository reservationSlotRepository;
    private FakeTimeSlotRepository timeSlotRepository;
    private FakeThemeRepository themeRepository;

    private TimeSlot savedTimeSlot;
    private Theme savedTheme;

    @BeforeEach
    void setUp() {
        timeSlotRepository = new FakeTimeSlotRepository();
        reservationRepository = new FakeReservationRepository();
        reservationSlotRepository = new FakeReservationSlotRepository();
        themeRepository = new FakeThemeRepository();

        reservationService = new ReservationService(reservationRepository, timeSlotRepository, themeRepository,
                reservationSlotRepository);

        savedTimeSlot = timeSlotRepository.save(new TimeSlot(LocalTime.of(10, 0)));
        savedTheme = themeRepository.save(new Theme("이름", "설명", "test.com", 50000L));
    }

    @Test
    @DisplayName("원시값을 받아 연관된 객체를 조회하여 조립한 뒤 예약을 생성한다.")
    void 예약_저장() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        Reservation reservation = reservationService.saveReservation("브라운", futureDate, savedTimeSlot.getId(),
                savedTheme.getId(), REQUEST_TIME);
        assertThat(reservation.getTimeSlot().getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    @DisplayName("이미 예약된 슬롯에 예약을 생성하면 대기로 저장된다.")
    void 중복_예약은_대기로_저장() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        reservationService.saveReservation("브라운", futureDate, savedTimeSlot.getId(), savedTheme.getId(), REQUEST_TIME);

        Reservation waiting = reservationService.saveReservation("네오", futureDate, savedTimeSlot.getId(),
                savedTheme.getId(), REQUEST_TIME);

        assertThat(waiting.getStatus()).isEqualTo(ReservationStatus.WAITING);
    }

    @Test
    @DisplayName("같은 사용자가 같은 슬롯에 중복으로 예약하거나 대기할 수 없다.")
    void 같은_사용자_같은_슬롯_중복_예외_발생() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        reservationService.saveReservation("브라운", futureDate, savedTimeSlot.getId(), savedTheme.getId(), REQUEST_TIME);

        assertThatThrownBy(
                () -> reservationService.saveReservation("브라운", futureDate, savedTimeSlot.getId(), savedTheme.getId(), REQUEST_TIME))
                .isInstanceOf(DuplicateException.class)
                .hasMessage("이미 예약 또는 대기 중인 시간입니다. 다른 날짜 혹은 테마를 선택해주세요.");
    }

    @Test
    @DisplayName("존재하는 예약을 식별자를 통해 삭제하면 목록에서 사라진다.")
    void 예약_삭제() {
        LocalDate futureDate = LocalDate.now().plusDays(2);
        Reservation reservation = reservationService.saveReservation("브라운", futureDate, savedTimeSlot.getId(),
                savedTheme.getId(), REQUEST_TIME);
        reservationService.removeReservation(reservation.getId(), "브라운", REQUEST_TIME);
        assertThat(reservationService.findAllReservations()).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 예약을 삭제해도 예외가 발생하지 않는다.")
    void 존재하지_않는_예약_삭제() {
        assertThatCode(() -> reservationService.removeReservation(999L, "브라운", REQUEST_TIME))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("모든 예약 목록을 조회하여 반환한다.")
    void 전체_예약_조회() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        reservationService.saveReservation("브라운", futureDate, savedTimeSlot.getId(), savedTheme.getId(), REQUEST_TIME);
        List<Reservation> reservations = reservationService.findAllReservations();
        assertThat(reservations).hasSize(1);
    }

    @Test
    @DisplayName("식별자를 통해 특정 예약 객체를 조회한다.")
    void 식별자로_예약_조회() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        Reservation savedReservation = reservationService.saveReservation("브라운", futureDate, savedTimeSlot.getId(),
                savedTheme.getId(), REQUEST_TIME);
        Reservation foundReservation = reservationService.getReservationById(savedReservation.getId());
        assertThat(foundReservation.getName()).isEqualTo("브라운");
    }

    @Test
    @DisplayName("지나간 날짜에 대한 예약 생성은 불가능하다.")
    void 지난_날짜_예약_생성_예외_발생() {
        LocalDate pastDate = LocalDate.now().minusDays(1);

        assertThatThrownBy(
                () -> reservationService.saveReservation("브라운", pastDate, savedTimeSlot.getId(), savedTheme.getId(), REQUEST_TIME))
                .isInstanceOf(PastTimeException.class)
                .hasMessage("지난 날짜/시간으로 예약하실 수 없습니다.");
    }

    @Test
    @DisplayName("지나간 시간에 대한 예약 생성은 불가능하다.")
    void 지난_시간_예약_생성_예외_발생() {
        LocalDate today = LocalDate.now();
        LocalTime pastTime = LocalTime.of(0, 0);
        TimeSlot pastTimeSlot = timeSlotRepository.save(new TimeSlot(pastTime));

        assertThatThrownBy(
                () -> reservationService.saveReservation("브라운", today, pastTimeSlot.getId(), savedTheme.getId(), REQUEST_TIME))
                .isInstanceOf(PastTimeException.class)
                .hasMessage("지난 날짜/시간으로 예약하실 수 없습니다.");
    }

    @Test
    @DisplayName("이미 지난 예약을 삭제하려고 시도하면 예외가 발생한다.")
    void 지난_예약_삭제_예외_발생() {
        LocalDate pastDate = LocalDate.now().minusDays(1);
        Reservation pastReservation = reservationRepository.save(
                createReservation("브라운", pastDate, pastDate.minusDays(1).atStartOfDay())
        );

        assertThatThrownBy(() -> reservationService.removeReservation(pastReservation.getId(), "브라운", REQUEST_TIME))
                .isInstanceOf(PastTimeException.class)
                .hasMessage("예약 시작 24시간 전까지만 예약을 삭제할 수 있습니다.");
    }

    @Test
    @DisplayName("이미 지난 예약을 수정하려고 시도하면 예외가 발생한다.")
    void 지난_예약_수정_예외_발생() {
        LocalDate pastDate = LocalDate.now().minusDays(1);
        Reservation pastReservation = reservationRepository.save(
                createReservation("브라운", pastDate, pastDate.minusDays(1).atStartOfDay())
        );

        assertThatThrownBy(() -> reservationService.updateReservation(
                pastReservation.getId(), "브라운", LocalDate.now().plusDays(1), savedTimeSlot.getId(), REQUEST_TIME
        )).isInstanceOf(PastTimeException.class)
                .hasMessage("이미 지난 예약은 수정할 수 없습니다.");
    }

    @Test
    @DisplayName("다른 사용자의 예약을 삭제하려고 시도하면 예외가 발생한다.")
    void 다른_사용자_예약_삭제_예외_발생() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        Reservation savedReservation = reservationRepository.save(
                createReservation("브라운", futureDate, LocalDateTime.now())
        );

        assertThatThrownBy(() -> reservationService.removeReservation(savedReservation.getId(), "네오", REQUEST_TIME))
                .isInstanceOf(NotOwnerException.class)
                .hasMessage("본인의 예약만 제어할 수 있습니다.");
    }

    @Test
    @DisplayName("예약을 이미 예약된 다른 시간으로 수정하면 대기로 변경된다.")
    void 중복_시간_예약_수정시_대기로_변경() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        Reservation target = reservationService.saveReservation("브라운", futureDate, savedTimeSlot.getId(),
                savedTheme.getId(), REQUEST_TIME);
        TimeSlot otherTime = timeSlotRepository.save(new TimeSlot(LocalTime.of(13, 0)));
        reservationService.saveReservation("네오", futureDate, otherTime.getId(), savedTheme.getId(), REQUEST_TIME);

        reservationService.updateReservation(target.getId(), "브라운", futureDate, otherTime.getId(), REQUEST_TIME);

        Reservation updatedReservation = reservationService.getReservationById(target.getId());
        assertThat(updatedReservation.getTimeSlot()).isEqualTo(otherTime);
        assertThat(updatedReservation.getStatus()).isEqualTo(ReservationStatus.WAITING);
    }

    @Test
    @DisplayName("대기를 예약이 없는 시간으로 수정하면 예약으로 변경된다.")
    void 대기_예약을_빈_시간으로_수정시_예약으로_변경() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        reservationService.saveReservation("브라운", futureDate, savedTimeSlot.getId(),
                savedTheme.getId(), REQUEST_TIME);
        Reservation waiting = reservationService.saveReservation("네오", futureDate, savedTimeSlot.getId(),
                savedTheme.getId(), REQUEST_TIME);
        TimeSlot emptyTime = timeSlotRepository.save(new TimeSlot(LocalTime.of(13, 0)));

        reservationService.updateReservation(waiting.getId(), "네오", futureDate, emptyTime.getId(), REQUEST_TIME);

        Reservation updatedReservation = reservationService.getReservationById(waiting.getId());
        assertThat(updatedReservation.getTimeSlot()).isEqualTo(emptyTime);
        assertThat(updatedReservation.getStatus()).isEqualTo(ReservationStatus.RESERVED);
    }

    @Test
    @DisplayName("자기 자신의 예약 날짜와 시간을 그대로 유지하면 수정하지 않는다.")
    void 같은_날짜_시간_예약_수정_검증() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        Reservation target = reservationService.saveReservation("브라운", futureDate, savedTimeSlot.getId(),
                savedTheme.getId(), REQUEST_TIME);

        reservationService.updateReservation(target.getId(), "브라운", futureDate, savedTimeSlot.getId(), REQUEST_TIME);

        Reservation reservation = reservationService.getReservationById(target.getId());
        assertThat(reservation.getName()).isEqualTo("브라운");
        assertThat(reservation.getDate()).isEqualTo(futureDate);
        assertThat(reservation.getTimeSlot()).isEqualTo(savedTimeSlot);
    }

    @Test
    @DisplayName("예약을 취소하면 같은 슬롯의 첫 번째 대기가 예약으로 승급된다.")
    void 예약_취소시_첫번째_대기_승급() {
        LocalDate futureDate = LocalDate.now().plusDays(2);
        Reservation reservation = reservationService.saveReservation(
                "브라운", futureDate, savedTimeSlot.getId(), savedTheme.getId(), REQUEST_TIME
        );
        Reservation firstWaiting = reservationService.saveReservation(
                "네오", futureDate, savedTimeSlot.getId(), savedTheme.getId(), REQUEST_TIME
        );

        reservationService.removeReservation(reservation.getId(), "브라운", REQUEST_TIME);

        Reservation promoted = reservationService.getReservationById(firstWaiting.getId());
        assertThat(promoted.getStatus()).isEqualTo(ReservationStatus.RESERVED);
    }

    @Test
    @DisplayName("두번째 대기 순번 예약 취소 시 대기 순번은 1번을 반환한다.")
    void 예약_취소_후_두번째_대기_순번_1번_반() {
        LocalDate futureDate = LocalDate.now().plusDays(2);
        Reservation reservation = reservationService.saveReservation(
                "브라운", futureDate, savedTimeSlot.getId(), savedTheme.getId(), REQUEST_TIME
        );
        reservationService.saveReservation(
                "네오", futureDate, savedTimeSlot.getId(), savedTheme.getId(), REQUEST_TIME
        );
        Reservation secondWaiting = reservationService.saveReservation(
                "대길", futureDate, savedTimeSlot.getId(), savedTheme.getId(), REQUEST_TIME
        );

        reservationService.removeReservation(reservation.getId(), "브라운", REQUEST_TIME);

        List<ReservationAndWaiting> result = reservationService.findReservationAndWaitingByName("대길");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo(secondWaiting.getId());
        assertThat(result.getFirst().isReserved()).isFalse();
        assertThat(result.getFirst().waitingIndex()).isZero();
    }

    private Reservation createReservation(String name, LocalDate date, LocalDateTime createdAt) {
        ReservationSlot slot = reservationSlotRepository.save(new ReservationSlot(date, savedTimeSlot, savedTheme));
        return new Reservation(null, name, slot, createdAt, ReservationStatus.RESERVED);
    }
}
