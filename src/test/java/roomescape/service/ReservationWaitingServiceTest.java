package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservatinWaiting.ReservationWaiting;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.dto.reservationWaiting.ReservationWaitingRequest;
import roomescape.dto.reservationWaiting.ReservationWaitingResponse;
import roomescape.exception.InvalidInputException;
import roomescape.exception.ReservationTimeNotFoundException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.exception.ThemeNotFoundException;
import roomescape.fake.FakeReservationQueryingDao;
import roomescape.fake.FakeReservationTimeQueryingDao;
import roomescape.fake.FakeReservationWaitingDao;
import roomescape.fake.FakeThemeQueryingDao;

class ReservationWaitingServiceTest {

    private FakeReservationWaitingDao waitingDao;
    private FakeReservationQueryingDao reservationQueryingDao;
    private FakeReservationTimeQueryingDao reservationTimeQueryingDao;
    private FakeThemeQueryingDao themeQueryingDao;
    private ReservationWaitingService service;

    private static final LocalDate tomorrow = LocalDate.now().plusDays(1);
    private static final ReservationTime reservationTime = new ReservationTime(1L, LocalTime.parse("10:00"));
    private static final Theme theme = new Theme(2L, "test", "설명", "url");

    @BeforeEach
    void setUp() {
        waitingDao = new FakeReservationWaitingDao();
        reservationQueryingDao = new FakeReservationQueryingDao();
        reservationTimeQueryingDao = new FakeReservationTimeQueryingDao();
        themeQueryingDao = new FakeThemeQueryingDao();

        reservationTimeQueryingDao.save(reservationTime);
        themeQueryingDao.save(theme);

        service = new ReservationWaitingService(waitingDao, reservationQueryingDao, reservationTimeQueryingDao, themeQueryingDao);
    }

    @Test
    void 예약_대기열이_정상_생성된다() {
        Reservation reservation = Reservation.restore(1L, "다른사람", tomorrow, reservationTime, theme, LocalDateTime.now());
        reservationQueryingDao.save(reservation);

        ReservationWaitingRequest request = new ReservationWaitingRequest("테스트", tomorrow, 1L, 2L);
        ReservationWaitingResponse response = service.create(request);

        assertThat(response.name()).isEqualTo("테스트");
        assertThat(response.sequence()).isEqualTo(1L);
    }

    @Test
    void 잘못된_시간_id를_넣은_경우_예외가_발생한다() {
        ReservationWaitingRequest request = new ReservationWaitingRequest("test", tomorrow, 999L, 2L);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(ReservationTimeNotFoundException.class);
    }

    @Test
    void 잘못된_테마_id를_넣은_경우_예외가_발생한다() {
        ReservationWaitingRequest request = new ReservationWaitingRequest("test", tomorrow, 1L, 999L);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(ThemeNotFoundException.class);
    }

    @Test
    void 예약이_존재하지_않는데_대기열에_추가를_시도하면_예외가_발생한다() {
        // 예약이 없는 상태에서 대기 시도
        ReservationWaitingRequest request = new ReservationWaitingRequest("테스트", tomorrow, 1L, 2L);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void 예약이_존재하는데_같은_이름으로_대기열에_추가를_시도하면_예외가_발생한다() {
        Reservation reservation = Reservation.restore(1L, "테스트", tomorrow, reservationTime, theme, LocalDateTime.now());
        reservationQueryingDao.save(reservation);

        ReservationWaitingRequest request = new ReservationWaitingRequest("테스트", tomorrow, 1L, 2L);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void 중복_예약_대기열_생성_시도하면_예외가_발생한다() {
        Reservation reservation = Reservation.restore(1L, "다른사람", tomorrow, reservationTime, theme, LocalDateTime.now());
        reservationQueryingDao.save(reservation);

        ReservationWaiting existing = ReservationWaiting.restore(1L, "테스트", tomorrow, reservationTime, theme, 1L, LocalDateTime.now());
        waitingDao.create(existing);

        ReservationWaitingRequest request = new ReservationWaitingRequest("테스트", tomorrow, 1L, 2L);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void 예약_대기열이_정상_삭제된다() {
        ReservationWaiting waiting = ReservationWaiting.restore(1L, "테스트", tomorrow, reservationTime, theme, 1L, LocalDateTime.now());
        waitingDao.create(waiting);

        assertThatCode(() -> service.delete(1L)).doesNotThrowAnyException();
    }

    @Test
    void 존재하지_않는_예약_대기열_삭제를_시도하면_예외가_발생한다() {
        assertThatThrownBy(() -> service.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
