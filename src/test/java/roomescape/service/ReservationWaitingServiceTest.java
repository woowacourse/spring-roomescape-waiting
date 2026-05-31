package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationWaiting.ReservationWaiting;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.dto.reservationWaiting.ReservationWaitingRequest;
import roomescape.dto.reservationWaiting.ReservationWaitingResponse;
import roomescape.exception.InvalidInputException;
import roomescape.exception.ResourceNotFoundException;
import java.util.List;
import roomescape.fake.FakeReservationQueryingDao;
import roomescape.fake.FakeReservationWaitingDao;

class ReservationWaitingServiceTest {

    private FakeReservationWaitingDao waitingDao;
    private FakeReservationQueryingDao reservationQueryingDao;
    private ReservationWaitingService service;

    private static final LocalDate tomorrow = LocalDate.now().plusDays(1);
    private static final ReservationTime reservationTime = new ReservationTime(1L, LocalTime.parse("10:00"));
    private static final Theme theme = new Theme(2L, "test", "설명", "url");

    @BeforeEach
    void setUp() {
        waitingDao = new FakeReservationWaitingDao();
        reservationQueryingDao = new FakeReservationQueryingDao();

        service = new ReservationWaitingService(waitingDao, reservationQueryingDao);
    }

    @Test
    void 예약_대기열이_정상_생성된다() {
        Reservation reservation = Reservation.restore(1L, "다른사람", tomorrow, reservationTime, theme, LocalDateTime.now(), "test-version");
        reservationQueryingDao.save(reservation);

        ReservationWaitingRequest request = new ReservationWaitingRequest("테스트", tomorrow, 1L, 2L);
        ReservationWaitingResponse response = service.create(request);

        assertThat(response.name()).isEqualTo("테스트");
        assertThat(response.sequence()).isEqualTo(1L);
    }

    @Test
    void 예약이_존재하지_않는데_대기열에_추가를_시도하면_예외가_발생한다() {
        ReservationWaitingRequest request = new ReservationWaitingRequest("테스트", tomorrow, 1L, 2L);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void 중복_예약_대기열_생성_시도하면_예외가_발생한다() {
        Reservation reservation = Reservation.restore(1L, "다른사람", tomorrow, reservationTime, theme, LocalDateTime.now(), "test-version");
        reservationQueryingDao.save(reservation);

        ReservationWaiting existing = ReservationWaiting.restore(1L, "테스트", reservation, 1L, LocalDateTime.now());
        waitingDao.create(existing);

        ReservationWaitingRequest request = new ReservationWaitingRequest("테스트", tomorrow, 1L, 2L);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void 예약_대기열이_정상_삭제된다() {
        Reservation reservation = Reservation.restore(1L, "다른사람", tomorrow, reservationTime, theme, LocalDateTime.now(), "test-version");
        ReservationWaiting waiting = ReservationWaiting.restore(1L, "테스트", reservation, 1L, LocalDateTime.now());
        waitingDao.create(waiting);

        assertThatCode(() -> service.delete(1L)).doesNotThrowAnyException();
    }

    @Test
    void 예약자_이름으로_대기_등록_시도하면_예외가_발생한다() {
        Reservation reservation = Reservation.restore(1L, "테스트", tomorrow, reservationTime, theme, LocalDateTime.now(), "test-version");
        reservationQueryingDao.save(reservation);

        ReservationWaitingRequest request = new ReservationWaitingRequest("테스트", tomorrow, 1L, 2L);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void 전체_대기열을_조회한다() {
        Reservation reservation = Reservation.restore(1L, "다른사람", tomorrow, reservationTime, theme, LocalDateTime.now(), "test-version");
        waitingDao.create(ReservationWaiting.restore(1L, "테스트", reservation, 1L, LocalDateTime.now()));
        waitingDao.create(ReservationWaiting.restore(2L, "브라운", reservation, 2L, LocalDateTime.now()));

        List<ReservationWaitingResponse> result = service.readAll();

        assertThat(result).hasSize(2);
    }

    @Test
    void 이름으로_대기열을_조회한다() {
        Reservation reservation = Reservation.restore(1L, "다른사람", tomorrow, reservationTime, theme, LocalDateTime.now(), "test-version");
        waitingDao.create(ReservationWaiting.restore(1L, "테스트", reservation, 1L, LocalDateTime.now()));
        waitingDao.create(ReservationWaiting.restore(2L, "브라운", reservation, 2L, LocalDateTime.now()));

        List<ReservationWaitingResponse> result = service.readByName("테스트");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("테스트");
    }

    @Test
    void 지난_예약에_대기열_등록_시도하면_예외가_발생한다() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        Reservation expiredReservation = Reservation.restore(1L, "다른사람", yesterday, reservationTime, theme, LocalDateTime.now(), "test-version");
        reservationQueryingDao.save(expiredReservation);

        ReservationWaitingRequest request = new ReservationWaitingRequest("테스트", yesterday, 1L, 2L);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void 존재하지_않는_대기열_삭제_시_예외없이_무시된다() {
        assertThatCode(() -> service.delete(999L)).doesNotThrowAnyException();
    }

    @Test
    void 대기열이_없을_때_전체_조회하면_빈_리스트가_반환된다() {
        List<ReservationWaitingResponse> result = service.readAll();

        assertThat(result).isEmpty();
    }

    @Test
    void 이름으로_조회_시_일치하는_항목이_없으면_빈_리스트가_반환된다() {
        Reservation reservation = Reservation.restore(1L, "다른사람", tomorrow, reservationTime, theme, LocalDateTime.now(), "test-version");
        waitingDao.create(ReservationWaiting.restore(1L, "테스트", reservation, 1L, LocalDateTime.now()));

        List<ReservationWaitingResponse> result = service.readByName("없는사람");

        assertThat(result).isEmpty();
    }

    @Test
    void 서로_다른_예약의_대기_순번은_독립적으로_계산된다() {
        Reservation reservation1 = Reservation.restore(1L, "예약자A", tomorrow, reservationTime, theme, LocalDateTime.now(), "test-version");
        Reservation reservation2 = Reservation.restore(2L, "예약자B", tomorrow.plusDays(1), reservationTime, theme, LocalDateTime.now(), "test-version");
        reservationQueryingDao.save(reservation1);
        reservationQueryingDao.save(reservation2);

        waitingDao.create(ReservationWaiting.restore(1L, "대기1", reservation1, null, LocalDateTime.now()));
        waitingDao.create(ReservationWaiting.restore(2L, "대기2", reservation1, null, LocalDateTime.now().plusSeconds(1)));
        waitingDao.create(ReservationWaiting.restore(3L, "대기3", reservation2, null, LocalDateTime.now()));

        List<ReservationWaitingResponse> reservation1Waitings = service.readAll().stream()
                .filter(r -> r.date().equals(tomorrow))
                .toList();
        List<ReservationWaitingResponse> reservation2Waitings = service.readAll().stream()
                .filter(r -> r.date().equals(tomorrow.plusDays(1)))
                .toList();

        assertThat(reservation1Waitings).hasSize(2);
        assertThat(reservation2Waitings).hasSize(1);
        assertThat(reservation2Waitings.get(0).sequence()).isEqualTo(1L);
    }

}
