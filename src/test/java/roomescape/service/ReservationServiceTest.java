package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import roomescape.DatabaseInitializer;
import roomescape.common.exception.RoomEscapeException;
import roomescape.repository.ReservationRepository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;
import roomescape.dto.command.CreateReservationCommand;
import roomescape.dto.command.UpdateReservationCommand;
import roomescape.dto.response.MyReservationResponse;
import roomescape.dto.response.ReservationResponse;
import roomescape.dto.response.ReservationStatus;
import roomescape.repository.ReservationWaitingRepository;
import roomescape.domain.ReservationWaiting;
import roomescape.repository.ReservationTimeRepository;
import roomescape.domain.ReservationTime;
import roomescape.repository.ThemeRepository;
import roomescape.domain.Theme;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class ReservationServiceTest {

    @Autowired
    private DatabaseInitializer databaseInitializer;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationWaitingRepository waitingDao;

    @BeforeEach
    void setUp() {
        databaseInitializer.clear();
    }

    @Test
    void 예약을_추가한다() {
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        CreateReservationCommand command = new CreateReservationCommand(
                "브라운", LocalDate.now().plusDays(1), time.getId(), theme.getId()
        );

        ReservationResponse response = reservationService.createReservation(command, LocalDateTime.now());

        assertThat(response)
                .extracting(ReservationResponse::name, ReservationResponse::date)
                .containsExactly("브라운", LocalDate.now().plusDays(1));
    }

    @Test
    void 존재하지_않는_시간으로_예약하면_404를_반환한다() {
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        CreateReservationCommand command = new CreateReservationCommand(
                "브라운", LocalDate.now().plusDays(1), 999L, theme.getId()
        );

        assertThatThrownBy(() -> reservationService.createReservation(command, LocalDateTime.now()))
                .isInstanceOf(RoomEscapeException.class)
                .satisfies(exception -> assertThat(((RoomEscapeException) exception).getErrorCode().getHttpStatus())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void 존재하지_않는_테마로_예약하면_404를_반환한다() {
        ReservationTime time = saveTime(10, 0);
        CreateReservationCommand command = new CreateReservationCommand(
                "브라운", LocalDate.now().plusDays(1), time.getId(), 999L
        );

        assertThatThrownBy(() -> reservationService.createReservation(command, LocalDateTime.now()))
                .isInstanceOf(RoomEscapeException.class)
                .satisfies(exception -> assertThat(((RoomEscapeException) exception).getErrorCode().getHttpStatus())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void 중복_예약을_하면_409를_반환한다() {
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        LocalDate date = LocalDate.now().plusDays(1);
        saveReservation("브라운", date, time, theme);

        CreateReservationCommand command = new CreateReservationCommand(
                "브라운", date, time.getId(), theme.getId()
        );

        assertThatThrownBy(() -> reservationService.createReservation(command, LocalDateTime.now()))
                .isInstanceOf(RoomEscapeException.class)
                .satisfies(exception -> assertThat(((RoomEscapeException) exception).getErrorCode().getHttpStatus())
                        .isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void 지나간_날짜로_예약하면_422를_반환한다() {
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        CreateReservationCommand command = new CreateReservationCommand(
                "브라운", LocalDate.now().minusDays(1), time.getId(), theme.getId()
        );

        assertThatThrownBy(() -> reservationService.createReservation(command, LocalDateTime.now()))
                .isInstanceOf(RoomEscapeException.class)
                .satisfies(exception -> assertThat(((RoomEscapeException) exception).getErrorCode().getHttpStatus())
                        .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY));
    }

    @Test
    void 전체_예약을_조회한다() {
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        saveReservation("브라운", LocalDate.of(2026, 5, 5), time, theme);
        saveReservation("로지", LocalDate.of(2026, 5, 6), time, theme);

        List<ReservationResponse> responses = reservationService.getAllReservations();

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(ReservationResponse::name).containsExactly("브라운", "로지");
    }

    @Test
    void 내_예약과_대기를_함께_조회한다() {
        ReservationTime time = saveTime(10, 0);
        Theme theme1 = saveTheme("방탈출1", "설명1", "https://thumbnail1.com");
        Theme theme2 = saveTheme("방탈출2", "설명2", "https://thumbnail2.com");
        saveReservation("브라운", LocalDate.of(2026, 5, 10), time, theme1);
        saveReservationWaiting("브라운", LocalDate.of(2026, 5, 10), time, theme2);

        List<MyReservationResponse> responses = reservationService.getMyReservations("브라운");

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(MyReservationResponse::status).containsExactly(ReservationStatus.RESERVED, ReservationStatus.WAITING);
    }

    @Test
    void 내_예약과_대기가_날짜_순으로_정렬된다() {
        ReservationTime time = saveTime(10, 0);
        Theme theme1 = saveTheme("방탈출1", "설명1", "https://thumbnail1.com");
        Theme theme2 = saveTheme("방탈출2", "설명2", "https://thumbnail2.com");
        saveReservation("브라운", LocalDate.of(2026, 5, 10), time, theme1);
        saveReservationWaiting("브라운", LocalDate.of(2026, 5, 5), time, theme2);

        List<MyReservationResponse> responses = reservationService.getMyReservations("브라운");

        assertThat(responses).extracting(MyReservationResponse::date).containsExactly(LocalDate.of(2026, 5, 5), LocalDate.of(2026, 5, 10));
    }

    @Test
    void 예약_날짜_시간을_변경한다() {
        ReservationTime time1 = saveTime(10, 0);
        ReservationTime time2 = saveTime(11, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        Reservation saved = saveReservation("브라운", LocalDate.now().plusDays(1), time1, theme);
        UpdateReservationCommand command = new UpdateReservationCommand(
                LocalDate.now().plusDays(2), time2.getId()
        );

        ReservationResponse response = reservationService.update(saved.getId(), command, LocalDateTime.now());

        assertThat(response.date()).isEqualTo(LocalDate.now().plusDays(2));
    }

    @Test
    void 존재하지_않는_예약을_변경하면_404를_반환한다() {
        ReservationTime time = saveTime(10, 0);
        UpdateReservationCommand command = new UpdateReservationCommand(
                LocalDate.now().plusDays(1), time.getId()
        );

        assertThatThrownBy(() -> reservationService.update(999L, command, LocalDateTime.now()))
                .isInstanceOf(RoomEscapeException.class)
                .satisfies(exception -> assertThat(((RoomEscapeException) exception).getErrorCode().getHttpStatus())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void 지나간_날짜로_변경하면_422를_반환한다() {
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        Reservation saved = saveReservation("브라운", LocalDate.now().plusDays(1), time, theme);
        UpdateReservationCommand command = new UpdateReservationCommand(
                LocalDate.now().minusDays(1), time.getId()
        );

        assertThatThrownBy(() -> reservationService.update(saved.getId(), command, LocalDateTime.now()))
                .isInstanceOf(RoomEscapeException.class)
                .satisfies(exception -> assertThat(((RoomEscapeException) exception).getErrorCode().getHttpStatus())
                        .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY));
    }

    @Test
    void 중복된_날짜_시간으로_변경하면_409를_반환한다() {
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        LocalDate date = LocalDate.now().plusDays(1);
        saveReservation("브라운", date, time, theme);
        Reservation saved = saveReservation("로지", LocalDate.now().plusDays(2), time, theme);
        UpdateReservationCommand command = new UpdateReservationCommand(date, time.getId());

        assertThatThrownBy(() -> reservationService.update(saved.getId(), command, LocalDateTime.now()))
                .isInstanceOf(RoomEscapeException.class)
                .satisfies(exception -> assertThat(((RoomEscapeException) exception).getErrorCode().getHttpStatus())
                        .isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void 예약을_삭제한다() {
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        Reservation saved = saveReservation("브라운", LocalDate.now().plusDays(1), time, theme);

        assertThatNoException().isThrownBy(() -> reservationService.delete(saved.getId()));
    }

    @Test
    void 존재하지_않는_예약을_삭제하면_404를_반환한다() {
        assertThatThrownBy(() -> reservationService.delete(999L))
                .isInstanceOf(RoomEscapeException.class)
                .satisfies(exception -> assertThat(((RoomEscapeException) exception).getErrorCode().getHttpStatus())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void 예약_취소_시_대기자가_자동으로_예약으로_승격된다() {
        // given
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        LocalDate date = LocalDate.now().plusDays(1);
        Reservation reservation = saveReservation("브라운", date, time, theme);
        waitingDao.save(ReservationWaiting.createWithoutId("로지", LocalDateTime.now(),
                new ReservationSlot(date, time, theme)));

        // when
        reservationService.delete(reservation.getId());

        // then
        assertThat(reservationRepository.findByName("로지")).hasSize(1);
        assertThat(waitingDao.findAll()).isEmpty();
    }

    @Test
    void 예약_취소_시_대기자가_없으면_예약만_삭제된다() {
        // given
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        Reservation reservation = saveReservation("브라운", LocalDate.now().plusDays(1), time, theme);

        // when
        reservationService.delete(reservation.getId());

        // then
        assertThat(reservationRepository.findAll()).isEmpty();
        assertThat(waitingDao.findAll()).isEmpty();
    }

    @Test
    void 예약_취소_시_첫번째_대기자가_승격되어_남은_대기자_순번이_재정렬된다() {
        // given
        // 예약 1개
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        LocalDate date = LocalDate.now().plusDays(1);
        ReservationSlot slot = new ReservationSlot(date, time, theme);
        Reservation reservation = saveReservation("브라운", date, time, theme);

        // 대기 2명 (로지 1번, 맥스 2번)
        waitingDao.save(ReservationWaiting.createWithoutId("로지", LocalDateTime.now(), slot));
        ReservationWaiting maxsWaiting = waitingDao.save(
                ReservationWaiting.createWithoutId("맥스", LocalDateTime.now().plusSeconds(1), slot));

        // when
        // 예약 취소 및 대기 1번 자동 승격
        reservationService.delete(reservation.getId());

        // then
        // 맥스 1번 대기로 재정렬
        assertThat(reservationRepository.findByName("로지")).hasSize(1);
        assertThat(waitingDao.countOrder(slot, maxsWaiting.getId())).isEqualTo(1);
    }

    private ReservationTime saveTime(int hour, int minute) {
        return reservationTimeRepository.save(ReservationTime.createWithoutId(LocalTime.of(hour, minute)));
    }

    private Theme saveTheme(String name, String description, String thumbnail) {
        return themeRepository.save(Theme.createWithoutId(name, description, thumbnail));
    }

    private Reservation saveReservation(String name, LocalDate date, ReservationTime time, Theme theme) {
        return reservationRepository.save(Reservation.createWithoutId(name, new ReservationSlot(date, time, theme)));
    }

    private ReservationWaiting saveReservationWaiting(String name, LocalDate date, ReservationTime time, Theme theme) {
        return waitingDao.save(ReservationWaiting.createWithoutId(name, LocalDateTime.now(),
                new ReservationSlot(date, time, theme)));
    }
}
