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
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Theme;
import roomescape.dto.command.CreateReservationCommand;
import roomescape.dto.command.UpdateReservationCommand;
import roomescape.dto.response.MyReservationResponse;
import roomescape.dto.response.ReservationResponse;
import roomescape.dto.response.ReservationStatus;
import roomescape.domain.Order;
import roomescape.repository.MemberRepository;
import roomescape.repository.OrderRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ReservationWaitingRepository;
import roomescape.repository.ThemeRepository;

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
    private MemberRepository memberRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationWaitingRepository waitingDao;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        databaseInitializer.clear();
    }

    @Test
    void 예약을_추가한다() {
        Member member = saveMember("브라운");
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        CreateReservationCommand command = new CreateReservationCommand(
                member.getId(), LocalDate.now().plusDays(1), time.getId(), theme.getId(), 10000L
        );

        ReservationResponse response = reservationService.createReservation(command, LocalDateTime.now());

        assertThat(response)
                .extracting(ReservationResponse::name, ReservationResponse::date)
                .containsExactly("브라운", LocalDate.now().plusDays(1));
    }

    @Test
    void 존재하지_않는_시간으로_예약하면_404를_반환한다() {
        Member member = saveMember("브라운");
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        CreateReservationCommand command = new CreateReservationCommand(
                member.getId(), LocalDate.now().plusDays(1), 999L, theme.getId(), 10000L
        );

        assertThatThrownBy(() -> reservationService.createReservation(command, LocalDateTime.now()))
                .isInstanceOf(RoomEscapeException.class)
                .satisfies(exception -> assertThat(((RoomEscapeException) exception).getErrorCode().getHttpStatus())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void 존재하지_않는_테마로_예약하면_404를_반환한다() {
        Member member = saveMember("브라운");
        ReservationTime time = saveTime(10, 0);
        CreateReservationCommand command = new CreateReservationCommand(
                member.getId(), LocalDate.now().plusDays(1), time.getId(), 999L, 10000L
        );

        assertThatThrownBy(() -> reservationService.createReservation(command, LocalDateTime.now()))
                .isInstanceOf(RoomEscapeException.class)
                .satisfies(exception -> assertThat(((RoomEscapeException) exception).getErrorCode().getHttpStatus())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void 중복_예약을_하면_409를_반환한다() {
        Member member1 = saveMember("브라운");
        Member member2 = saveMember("로지");
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        LocalDate date = LocalDate.now().plusDays(1);
        saveReservation(member1, date, time, theme);

        CreateReservationCommand command = new CreateReservationCommand(
                member2.getId(), date, time.getId(), theme.getId(), 10000L
        );

        assertThatThrownBy(() -> reservationService.createReservation(command, LocalDateTime.now()))
                .isInstanceOf(RoomEscapeException.class)
                .satisfies(exception -> assertThat(((RoomEscapeException) exception).getErrorCode().getHttpStatus())
                        .isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void 지나간_날짜로_예약하면_422를_반환한다() {
        Member member = saveMember("브라운");
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        CreateReservationCommand command = new CreateReservationCommand(
                member.getId(), LocalDate.now().minusDays(1), time.getId(), theme.getId(), 10000L
        );

        assertThatThrownBy(() -> reservationService.createReservation(command, LocalDateTime.now()))
                .isInstanceOf(RoomEscapeException.class)
                .satisfies(exception -> assertThat(((RoomEscapeException) exception).getErrorCode().getHttpStatus())
                        .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY));
    }

    @Test
    void 전체_예약을_조회한다() {
        Member brown = saveMember("브라운");
        Member roji = saveMember("로지");
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        saveReservation(brown, LocalDate.of(2026, 5, 5), time, theme);
        saveReservation(roji, LocalDate.of(2026, 5, 6), time, theme);

        List<ReservationResponse> responses = reservationService.getAllReservations();

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(ReservationResponse::name).containsExactly("브라운", "로지");
    }

    @Test
    void 내_예약과_대기를_함께_조회한다() {
        Member brown = saveMember("브라운");
        ReservationTime time = saveTime(10, 0);
        Theme theme1 = saveTheme("방탈출1", "설명1", "https://thumbnail1.com");
        Theme theme2 = saveTheme("방탈출2", "설명2", "https://thumbnail2.com");
        saveReservation(brown, LocalDate.of(2026, 5, 10), time, theme1);
        saveReservationWaiting(brown, LocalDate.of(2026, 5, 10), time, theme2);

        List<MyReservationResponse> responses = reservationService.getMyReservations(brown.getId());

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(MyReservationResponse::status)
                .containsExactly(ReservationStatus.PENDING_PAYMENT, ReservationStatus.WAITING);
    }

    @Test
    void 내_예약과_대기가_날짜_순으로_정렬된다() {
        Member brown = saveMember("브라운");
        ReservationTime time = saveTime(10, 0);
        Theme theme1 = saveTheme("방탈출1", "설명1", "https://thumbnail1.com");
        Theme theme2 = saveTheme("방탈출2", "설명2", "https://thumbnail2.com");
        saveReservation(brown, LocalDate.of(2026, 5, 10), time, theme1);
        saveReservationWaiting(brown, LocalDate.of(2026, 5, 5), time, theme2);

        List<MyReservationResponse> responses = reservationService.getMyReservations(brown.getId());

        assertThat(responses).extracting(MyReservationResponse::date)
                .containsExactly(LocalDate.of(2026, 5, 5), LocalDate.of(2026, 5, 10));
    }

    @Test
    void 예약_날짜_시간을_변경한다() {
        Member member = saveMember("브라운");
        ReservationTime time1 = saveTime(10, 0);
        ReservationTime time2 = saveTime(11, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        Reservation saved = saveReservation(member, LocalDate.now().plusDays(1), time1, theme);
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
        Member member = saveMember("브라운");
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        Reservation saved = saveReservation(member, LocalDate.now().plusDays(1), time, theme);
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
        Member member1 = saveMember("브라운");
        Member member2 = saveMember("로지");
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        LocalDate date = LocalDate.now().plusDays(1);
        saveReservation(member1, date, time, theme);
        Reservation saved = saveReservation(member2, LocalDate.now().plusDays(2), time, theme);
        UpdateReservationCommand command = new UpdateReservationCommand(date, time.getId());

        assertThatThrownBy(() -> reservationService.update(saved.getId(), command, LocalDateTime.now()))
                .isInstanceOf(RoomEscapeException.class)
                .satisfies(exception -> assertThat(((RoomEscapeException) exception).getErrorCode().getHttpStatus())
                        .isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void 예약을_삭제한다() {
        Member member = saveMember("브라운");
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        Reservation saved = saveReservation(member, LocalDate.now().plusDays(1), time, theme);

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
        Member brown = saveMember("브라운");
        Member roji = saveMember("로지");
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        LocalDate date = LocalDate.now().plusDays(1);
        Reservation reservation = saveReservation(brown, date, time, theme);
        waitingDao.save(ReservationWaiting.createWithoutId(roji, LocalDateTime.now(),
                new ReservationSlot(date, time, theme)));

        reservationService.delete(reservation.getId());

        assertThat(reservationRepository.findByMember_Id(roji.getId())).hasSize(1);
        assertThat(waitingDao.findAll()).isEmpty();
    }

    @Test
    void 예약_취소_시_대기자가_승격되면_Order가_생성된다() {
        Member brown = saveMember("브라운");
        Member roji = saveMember("로지");
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        LocalDate date = LocalDate.now().plusDays(1);
        Reservation reservation = saveReservation(brown, date, time, theme);
        orderRepository.save(Order.createWithoutId("order-original", 15000L, reservation));
        waitingDao.save(ReservationWaiting.createWithoutId(roji, LocalDateTime.now(),
                new ReservationSlot(date, time, theme)));

        reservationService.delete(reservation.getId());

        Reservation promoted = reservationRepository.findByMember_Id(roji.getId()).getFirst();
        Order promotedOrder = orderRepository.findByReservation_Id(promoted.getId()).orElseThrow();
        assertThat(promotedOrder.getAmount()).isEqualTo(15000L);
        assertThat(promotedOrder.getOrderId()).isNotBlank();
        assertThat(promotedOrder.getIdempotencyKey()).isNotBlank();
    }

    @Test
    void 예약_취소_시_대기자가_없으면_예약만_삭제된다() {
        Member member = saveMember("브라운");
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        Reservation reservation = saveReservation(member, LocalDate.now().plusDays(1), time, theme);

        reservationService.delete(reservation.getId());

        assertThat(reservationRepository.findAll()).isEmpty();
        assertThat(waitingDao.findAll()).isEmpty();
    }

    @Test
    void 예약_취소_시_첫번째_대기자가_승격되어_남은_대기자_순번이_재정렬된다() {
        Member brown = saveMember("브라운");
        Member roji = saveMember("로지");
        Member max = saveMember("맥스");
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        LocalDate date = LocalDate.now().plusDays(1);
        ReservationSlot slot = new ReservationSlot(date, time, theme);
        Reservation reservation = saveReservation(brown, date, time, theme);

        waitingDao.save(ReservationWaiting.createWithoutId(roji, LocalDateTime.now(), slot));
        ReservationWaiting maxsWaiting = waitingDao.save(
                ReservationWaiting.createWithoutId(max, LocalDateTime.now().plusSeconds(1), slot));

        reservationService.delete(reservation.getId());

        assertThat(reservationRepository.findByMember_Id(roji.getId())).hasSize(1);
        assertThat(waitingDao.countOrder(slot, maxsWaiting.getId())).isEqualTo(1);
    }

    private Member saveMember(String name) {
        return memberRepository.save(Member.createWithoutId(name));
    }

    private ReservationTime saveTime(int hour, int minute) {
        return reservationTimeRepository.save(ReservationTime.createWithoutId(LocalTime.of(hour, minute)));
    }

    private Theme saveTheme(String name, String description, String thumbnail) {
        return themeRepository.save(Theme.createWithoutId(name, description, thumbnail));
    }

    private Reservation saveReservation(Member member, LocalDate date, ReservationTime time, Theme theme) {
        return reservationRepository.save(Reservation.createWithoutId(member, new ReservationSlot(date, time, theme)));
    }

    private ReservationWaiting saveReservationWaiting(Member member, LocalDate date, ReservationTime time, Theme theme) {
        return waitingDao.save(ReservationWaiting.createWithoutId(member, LocalDateTime.now(),
                new ReservationSlot(date, time, theme)));
    }
}
