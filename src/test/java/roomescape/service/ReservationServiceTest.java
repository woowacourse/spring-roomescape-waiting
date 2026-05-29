package roomescape.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.dto.reservation.ReservationRequest;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.dto.reservationtime.ReservationTimeRequest;
import roomescape.dto.theme.ThemeRequest;
import roomescape.exception.ExpiredDateTimeException;
import roomescape.exception.ReservationAlreadyExistException;
import roomescape.exception.ResourceNotFoundException;
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

        assertThat(saved.id()).isNotNull();
        assertThat(saved.name()).isEqualTo("브라운");
    }

    @Test
    void 과거_날짜로_예약시_예외가_발생한다() {
        Long timeId = reservationTimeUpdatingDao.insert(new ReservationTimeRequest(LocalTime.of(10, 0)));
        Long themeId = themeUpdatingDao.insert(new ThemeRequest("명탐정의 부재", "탐험", "http://example.com"));
        ReservationRequest request = new ReservationRequest("브라운", LocalDate.now().minusDays(1), timeId, themeId);

        assertThatThrownBy(() -> reservationService.create(request))
                .isInstanceOf(ExpiredDateTimeException.class);
    }

    @Test
    void 과거_시간으로_예약시_예외가_발생한다() {
        Long timeId = reservationTimeUpdatingDao.insert(new ReservationTimeRequest(LocalTime.now().minusHours(1)));
        Long themeId = themeUpdatingDao.insert(new ThemeRequest("명탐점의 부재", "탐험", "http://example.com"));
        ReservationRequest reservationReq = new ReservationRequest("브라운", LocalDate.now(), timeId, themeId);

        assertThatThrownBy(() -> reservationService.create(reservationReq))
                .isInstanceOf(ExpiredDateTimeException.class);
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

    @Test
    void 예약_날짜_및_시간_변경() {
        Long timeId = reservationTimeUpdatingDao.insert(new ReservationTimeRequest(LocalTime.of(10, 0)));
        Long themeId = themeUpdatingDao.insert(new ThemeRequest("테마", "설명", "http://example.com"));
        ReservationResponse created = reservationService.create(new ReservationRequest("브라운", LocalDate.now().plusDays(1), timeId, themeId));

        Long newTimeId = reservationTimeUpdatingDao.insert(new ReservationTimeRequest(LocalTime.of(11, 0)));
        ReservationRequest newReservationReq = new ReservationRequest("브라운", LocalDate.now().plusDays(2), newTimeId, themeId);
        ReservationResponse updated = reservationService.update(created.id(), newReservationReq);

        assertThat(updated.date()).isEqualTo(LocalDate.now().plusDays(2));
    }

    @Test
    void 과거_날짜로_변경시_예외가_발생한다() {
        Long timeId = reservationTimeUpdatingDao.insert(new ReservationTimeRequest(LocalTime.of(10, 0)));
        Long themeId = themeUpdatingDao.insert(new ThemeRequest("명탐정의 부재", "탐험", "http://example.com"));
        ReservationResponse created = reservationService.create(new ReservationRequest("브라운", LocalDate.now().plusDays(1), timeId, themeId));

        Long newTimeId = reservationTimeUpdatingDao.insert(new ReservationTimeRequest(LocalTime.of(11, 0)));
        ReservationRequest newReservationReq = new ReservationRequest("브라운", LocalDate.now().minusDays(1), newTimeId, themeId);


        assertThatThrownBy(() -> reservationService.update(created.id(), newReservationReq))
                .isInstanceOf(ExpiredDateTimeException.class);
    }

    @Test
    void 이미_예약된_시간으로_변경시_예외가_발생한다() {
        //두 개의 시간 생성
        Long timeId1 = reservationTimeUpdatingDao.insert(new ReservationTimeRequest(LocalTime.of(10, 0)));
        Long timeId2 = reservationTimeUpdatingDao.insert(new ReservationTimeRequest(LocalTime.of(11, 0)));
        Long themeId = themeUpdatingDao.insert(new ThemeRequest("명탐정의 부재", "탐험", "http://example.com"));

        //두 개의 예약 생성
        ReservationResponse created = reservationService.create(new ReservationRequest("브라운", LocalDate.now().plusDays(1), timeId1, themeId));
        reservationService.create(new ReservationRequest("네오", LocalDate.now().plusDays(1), timeId2, themeId));

        //중복 예약 시도시 예외 발생해야함
        ReservationRequest updated = new ReservationRequest("브라운", LocalDate.now().plusDays(1), timeId2, themeId);
        assertThatThrownBy(() -> reservationService.update(created.id(), updated))
                .isInstanceOf(ReservationAlreadyExistException.class);
    }

    @Test
    void 존재하지_않는_예약_변경시_예외가_발생한다() {
        Long timeId = reservationTimeUpdatingDao.insert(new ReservationTimeRequest(LocalTime.of(10, 0)));
        Long themeId = themeUpdatingDao.insert(new ThemeRequest("명탐정의 부재", "탐험", "http://example.com"));
        ReservationRequest request = new ReservationRequest("브라운", LocalDate.now().plusDays(1), timeId, themeId);

        assertThatThrownBy(() -> reservationService.update(999L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void 예약_단건_조회_성공() {
        Long timeId = reservationTimeUpdatingDao.insert(new ReservationTimeRequest(LocalTime.of(10, 0)));
        Long themeId = themeUpdatingDao.insert(new ThemeRequest("테마", "설명", "http://example.com"));
        ReservationResponse created = reservationService.create(new ReservationRequest("브라운", LocalDate.now().plusDays(1), timeId, themeId));

        ReservationResponse found = reservationService.read(created.id());

        assertThat(found.id()).isEqualTo(created.id());
        assertThat(found.name()).isEqualTo("브라운");
    }

    @Test
    void 존재하지_않는_예약_단건_조회시_예외가_발생한다() {
        assertThatThrownBy(() -> reservationService.read(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void 예약_삭제_성공() {
        Long timeId = reservationTimeUpdatingDao.insert(new ReservationTimeRequest(LocalTime.of(10, 0)));
        Long themeId = themeUpdatingDao.insert(new ThemeRequest("테마", "설명", "http://example.com"));
        ReservationResponse created = reservationService.create(new ReservationRequest("브라운", LocalDate.now().plusDays(1), timeId, themeId));

        reservationService.delete(created.id());

        assertThat(reservationService.readAll()).isEmpty();
    }
}
