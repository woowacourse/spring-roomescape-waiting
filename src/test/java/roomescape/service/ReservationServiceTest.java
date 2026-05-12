package roomescape.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.domain.reservation.ReservationRequest;
import roomescape.domain.reservation.ReservationResponse;
import roomescape.domain.reservationtime.ReservationTimeRequest;
import roomescape.domain.theme.ThemeRequest;
import roomescape.exception.InvalidReservationException;
import roomescape.exception.ReservationAlreadyExistException;
import roomescape.exception.ReservationTimeNotFoundException;
import roomescape.exception.ThemeNotFoundException;
import roomescape.repository.ReservationQueryingDao;
import roomescape.repository.ReservationTimeQueryingDao;
import roomescape.repository.ReservationTimeUpdatingDao;
import roomescape.repository.ReservationUpdatingDao;
import roomescape.repository.ThemeQueryingDao;
import roomescape.repository.ThemeUpdatingDao;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@Import({ReservationService.class,
        ReservationQueryingDao.class, ReservationUpdatingDao.class,
        ReservationTimeQueryingDao.class, ReservationTimeUpdatingDao.class,
        ThemeQueryingDao.class, ThemeUpdatingDao.class})
class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationTimeUpdatingDao reservationTimeUpdatingDao;

    @Autowired
    private ThemeUpdatingDao themeUpdatingDao;

    @Test
    void 예약_생성_성공() {
        Long timeId = reservationTimeUpdatingDao.insert(new ReservationTimeRequest(LocalTime.of(10, 0)));
        Long themeId = themeUpdatingDao.insert(new ThemeRequest("명탐정의 부재", "탐험", "http://example.com"));
        ReservationRequest request = new ReservationRequest("브라운", LocalDate.now().plusDays(1), timeId, themeId);

        ReservationResponse saved = reservationService.create(request);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("브라운");
    }

    @Test
    void 과거_날짜로_예약시_예외가_발생한다() {
        Long timeId = reservationTimeUpdatingDao.insert(new ReservationTimeRequest(LocalTime.of(10, 0)));
        Long themeId = themeUpdatingDao.insert(new ThemeRequest("명탐정의 부재", "탐험", "http://example.com"));
        ReservationRequest request = new ReservationRequest("브라운", LocalDate.now().minusDays(1), timeId, themeId);

        assertThatThrownBy(() -> reservationService.create(request))
                .isInstanceOf(InvalidReservationException.class);
    }

    @Test
    void 중복된_테마_날짜_시간으로_예약하면_예외가_발생한다() {
        Long timeId = reservationTimeUpdatingDao.insert(new ReservationTimeRequest(LocalTime.of(10, 0)));
        Long themeId = themeUpdatingDao.insert(new ThemeRequest("명탐정의 부재", "탐험", "http://example.com"));
        ReservationRequest request = new ReservationRequest("브라운", LocalDate.now().plusDays(1), timeId, themeId);

        reservationService.create(request);

        assertThatThrownBy(() -> reservationService.create(request))
                .isInstanceOf(ReservationAlreadyExistException.class);
    }

    @Test
    void 존재하지_않는_시간으로_예약시_예외가_발생한다() {
        Long themeId = themeUpdatingDao.insert(new ThemeRequest("명탐정의 부재", "탐험", "http://example.com"));
        ReservationRequest request = new ReservationRequest("브라운", LocalDate.now().plusDays(1), 999L, themeId);

        assertThatThrownBy(() -> reservationService.create(request))
                .isInstanceOf(ReservationTimeNotFoundException.class);
    }

    @Test
    void 존재하지_않는_테마로_예약시_예외가_발생한다() {
        Long timeId = reservationTimeUpdatingDao.insert(new ReservationTimeRequest(LocalTime.of(10, 0)));
        ReservationRequest request = new ReservationRequest("브라운", LocalDate.now().plusDays(1), timeId, 999L);

        assertThatThrownBy(() -> reservationService.create(request))
                .isInstanceOf(ThemeNotFoundException.class);
    }

    @Test
    void 전체_예약_조회() {
        Long timeId = reservationTimeUpdatingDao.insert(new ReservationTimeRequest(LocalTime.of(10, 0)));
        Long themeId = themeUpdatingDao.insert(new ThemeRequest("테마", "설명", "http://example.com"));
        reservationService.create(new ReservationRequest("브라운", LocalDate.now().plusDays(1), timeId, themeId));
        reservationService.create(new ReservationRequest("네오", LocalDate.now().plusDays(2), timeId, themeId));

        List<ReservationResponse> result = reservationService.readAll();

        assertThat(result).hasSize(2);
    }
}
