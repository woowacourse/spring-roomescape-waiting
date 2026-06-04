package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationWaiting.ReservationWaiting;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.slot.Slot;
import roomescape.domain.slot.SlotDomainService;
import roomescape.domain.theme.Theme;
import roomescape.dto.reservation.ReservationRequest;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.dto.reservationtime.ReservationTimeRequest;
import roomescape.dto.theme.ThemeRequest;
import roomescape.exception.ExpiredDateTimeException;
import roomescape.exception.ReservationAlreadyExistException;
import roomescape.exception.ReservationTimeNotFoundException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.exception.ThemeNotFoundException;
import roomescape.repository.ReservationQueryingDao;
import roomescape.repository.ReservationTimeQueryingDao;
import roomescape.repository.ReservationTimeUpdatingDao;
import roomescape.repository.ReservationUpdatingDao;
import roomescape.repository.ReservationWaitingDao;
import roomescape.repository.SlotDao;
import roomescape.repository.ThemeQueryingDao;
import roomescape.repository.ThemeUpdatingDao;

@JdbcTest
@Import({ReservationService.class, SlotDomainService.class, SlotDao.class,
        ReservationQueryingDao.class, ReservationUpdatingDao.class,
        ReservationTimeQueryingDao.class, ReservationTimeUpdatingDao.class,
        ThemeQueryingDao.class, ThemeUpdatingDao.class,
        ReservationWaitingDao.class})
class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationTimeUpdatingDao reservationTimeUpdatingDao;

    @Autowired
    private ThemeUpdatingDao themeUpdatingDao;

    @Autowired
    private SlotDao slotDao;

    @Autowired
    private ReservationUpdatingDao reservationUpdatingDao;

    @Autowired
    private ReservationWaitingDao reservationWaitingDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long timeId;
    private Long themeId;
    private ReservationTime time;
    private Theme theme;

    private void setUpTimeAndTheme() {
        timeId = reservationTimeUpdatingDao.insert(new ReservationTimeRequest(LocalTime.of(10, 0)));
        themeId = themeUpdatingDao.insert(new ThemeRequest("테마", "설명", "http://example.com"));
        time = new ReservationTime(timeId, LocalTime.of(10, 0));
        theme = new Theme(themeId, "테마", "설명", "http://example.com");
    }

    private Slot findSlot(LocalDate date) {
        return slotDao.findByDateAndTimeAndTheme(date, timeId, themeId).orElseThrow();
    }

    @Test
    void 예약_생성_성공() {
        setUpTimeAndTheme();
        ReservationRequest request = new ReservationRequest("브라운", LocalDate.now().plusDays(1), timeId, themeId);

        ReservationResponse saved = reservationService.create(request);

        assertThat(saved.id()).isNotNull();
        assertThat(saved.name()).isEqualTo("브라운");
    }

    @Test
    void 과거_날짜로_예약시_예외가_발생한다() {
        setUpTimeAndTheme();
        ReservationRequest request = new ReservationRequest("브라운", LocalDate.now().minusDays(1), timeId, themeId);

        assertThatThrownBy(() -> reservationService.create(request))
                .isInstanceOf(ExpiredDateTimeException.class);
    }

    @Test
    void 과거_시간으로_예약시_예외가_발생한다() {
        LocalDateTime past = LocalDateTime.now().minusHours(1);
        Long pastTimeId = reservationTimeUpdatingDao.insert(new ReservationTimeRequest(past.toLocalTime()));
        Long localThemeId = themeUpdatingDao.insert(new ThemeRequest("명탐정의 부재", "탐험", "http://example.com"));
        ReservationRequest reservationReq = new ReservationRequest("브라운", past.toLocalDate(), pastTimeId, localThemeId);

        assertThatThrownBy(() -> reservationService.create(reservationReq))
                .isInstanceOf(ExpiredDateTimeException.class);
    }

    @Test
    void 중복된_테마_날짜_시간으로_예약하면_예외가_발생한다() {
        setUpTimeAndTheme();
        ReservationRequest request = new ReservationRequest("브라운", LocalDate.now().plusDays(1), timeId, themeId);

        reservationService.create(request);

        assertThatThrownBy(() -> reservationService.create(request))
                .isInstanceOf(ReservationAlreadyExistException.class);
    }

    @Test
    void 존재하지_않는_시간으로_예약시_예외가_발생한다() {
        Long localThemeId = themeUpdatingDao.insert(new ThemeRequest("명탐정의 부재", "탐험", "http://example.com"));
        ReservationRequest request = new ReservationRequest("브라운", LocalDate.now().plusDays(1), 999L, localThemeId);

        assertThatThrownBy(() -> reservationService.create(request))
                .isInstanceOf(ReservationTimeNotFoundException.class);
    }

    @Test
    void 존재하지_않는_테마로_예약시_예외가_발생한다() {
        Long localTimeId = reservationTimeUpdatingDao.insert(new ReservationTimeRequest(LocalTime.of(10, 0)));
        ReservationRequest request = new ReservationRequest("브라운", LocalDate.now().plusDays(1), localTimeId, 999L);

        assertThatThrownBy(() -> reservationService.create(request))
                .isInstanceOf(ThemeNotFoundException.class);
    }

    @Test
    void 전체_예약_조회() {
        setUpTimeAndTheme();
        reservationService.create(new ReservationRequest("브라운", LocalDate.now().plusDays(1), timeId, themeId));
        reservationService.create(new ReservationRequest("네오", LocalDate.now().plusDays(2), timeId, themeId));

        List<ReservationResponse> result = reservationService.readAll();

        assertThat(result).hasSize(2);
    }

    @Test
    void 예약_날짜_및_시간_변경() {
        setUpTimeAndTheme();
        ReservationResponse created = reservationService.create(new ReservationRequest("브라운", LocalDate.now().plusDays(1), timeId, themeId));

        Long newTimeId = reservationTimeUpdatingDao.insert(new ReservationTimeRequest(LocalTime.of(11, 0)));
        ReservationRequest newReservationReq = new ReservationRequest("브라운", LocalDate.now().plusDays(2), newTimeId, themeId);
        ReservationResponse updated = reservationService.update(created.id(), newReservationReq);

        assertThat(updated.date()).isEqualTo(LocalDate.now().plusDays(2));
    }

    @Test
    void 슬롯_변경_시_id는_유지되고_생성시각은_갱신된다() {
        setUpTimeAndTheme();
        ReservationResponse created = reservationService.create(new ReservationRequest("브라운", LocalDate.now().plusDays(1), timeId, themeId));
        ReservationResponse beforeUpdate = reservationService.read(created.id());

        Long newTimeId = reservationTimeUpdatingDao.insert(new ReservationTimeRequest(LocalTime.of(11, 0)));
        ReservationResponse updated = reservationService.update(created.id(),
                new ReservationRequest("브라운", LocalDate.now().plusDays(2), newTimeId, themeId));

        assertThat(updated.id()).isEqualTo(created.id());
        assertThat(updated.createdAt()).isAfter(beforeUpdate.createdAt());
        assertThat(updated.date()).isEqualTo(LocalDate.now().plusDays(2));
    }

    @Test
    void 과거_날짜로_변경시_예외가_발생한다() {
        setUpTimeAndTheme();
        ReservationResponse created = reservationService.create(new ReservationRequest("브라운", LocalDate.now().plusDays(1), timeId, themeId));

        Long newTimeId = reservationTimeUpdatingDao.insert(new ReservationTimeRequest(LocalTime.of(11, 0)));
        ReservationRequest newReservationReq = new ReservationRequest("브라운", LocalDate.now().minusDays(1), newTimeId, themeId);

        assertThatThrownBy(() -> reservationService.update(created.id(), newReservationReq))
                .isInstanceOf(ExpiredDateTimeException.class);
    }

    @Test
    void 이미_예약된_시간으로_변경시_예외가_발생한다() {
        setUpTimeAndTheme();
        Long timeId2 = reservationTimeUpdatingDao.insert(new ReservationTimeRequest(LocalTime.of(11, 0)));

        ReservationResponse created = reservationService.create(new ReservationRequest("브라운", LocalDate.now().plusDays(1), timeId, themeId));
        reservationService.create(new ReservationRequest("네오", LocalDate.now().plusDays(1), timeId2, themeId));

        ReservationRequest updated = new ReservationRequest("브라운", LocalDate.now().plusDays(1), timeId2, themeId);
        assertThatThrownBy(() -> reservationService.update(created.id(), updated))
                .isInstanceOf(ReservationAlreadyExistException.class);
    }

    @Test
    void 존재하지_않는_예약_변경시_예외가_발생한다() {
        setUpTimeAndTheme();
        ReservationRequest request = new ReservationRequest("브라운", LocalDate.now().plusDays(1), timeId, themeId);

        assertThatThrownBy(() -> reservationService.update(999L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void 예약_단건_조회_성공() {
        setUpTimeAndTheme();
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
        setUpTimeAndTheme();
        ReservationResponse created = reservationService.create(new ReservationRequest("브라운", LocalDate.now().plusDays(1), timeId, themeId));

        reservationService.delete(created.id());

        assertThat(reservationService.readAll()).isEmpty();
    }

    @Test
    void 예약_삭제_시_대기열이_있으면_첫_번째_대기자가_예약자로_승격된다() {
        setUpTimeAndTheme();
        ReservationResponse created = reservationService.create(new ReservationRequest("브라운", LocalDate.now().plusDays(1), timeId, themeId));
        Slot slot = findSlot(created.date());
        reservationWaitingDao.create(ReservationWaiting.create("네오", slot));

        reservationService.delete(created.id());

        List<ReservationResponse> remaining = reservationService.readAll();
        assertThat(remaining).hasSize(1);
        assertThat(remaining.get(0).name()).isEqualTo("네오");
        assertThat(reservationWaitingDao.findAllReservationWaiting()).isEmpty();
    }

    @Test
    void 예약_삭제_시_대기열이_없으면_예약이_삭제된다() {
        setUpTimeAndTheme();
        ReservationResponse created = reservationService.create(new ReservationRequest("브라운", LocalDate.now().plusDays(1), timeId, themeId));

        reservationService.delete(created.id());

        assertThat(reservationService.readAll()).isEmpty();
        assertThat(reservationWaitingDao.findAllReservationWaiting()).isEmpty();
    }

    @Test
    void 이름으로_예약_조회() {
        setUpTimeAndTheme();
        reservationService.create(new ReservationRequest("브라운", LocalDate.now().plusDays(1), timeId, themeId));
        reservationService.create(new ReservationRequest("네오", LocalDate.now().plusDays(2), timeId, themeId));

        List<ReservationResponse> result = reservationService.readByName("브라운");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("브라운");
    }

    @Test
    void 슬롯_변경_없이_이름만_변경하면_예약자가_바뀐다() {
        setUpTimeAndTheme();
        ReservationResponse created = reservationService.create(new ReservationRequest("브라운", LocalDate.now().plusDays(1), timeId, themeId));

        ReservationRequest sameSlotNewName = new ReservationRequest("네오", created.date(), timeId, themeId);
        ReservationResponse updated = reservationService.update(created.id(), sameSlotNewName);

        assertThat(updated.name()).isEqualTo("네오");
        assertThat(updated.date()).isEqualTo(created.date());
        assertThat(updated.id()).isEqualTo(created.id());
    }

    @Test
    void 만료된_예약_삭제시_예외가_발생한다() {
        setUpTimeAndTheme();
        Slot pastSlot = Slot.create(LocalDate.now().minusDays(1), time, theme);
        Long slotId = slotDao.insert(pastSlot);
        Long expiredId = reservationUpdatingDao.insert(
                Reservation.restore(null, pastSlot.withId(slotId), "브라운", LocalDateTime.now()));

        assertThatThrownBy(() -> reservationService.delete(expiredId))
                .isInstanceOf(ExpiredDateTimeException.class);
    }

    @Test
    void 슬롯_변경_시_대기열이_있으면_대기자가_기존_슬롯에_승격된다() {
        setUpTimeAndTheme();
        Long timeId2 = reservationTimeUpdatingDao.insert(new ReservationTimeRequest(LocalTime.of(11, 0)));
        ReservationResponse created = reservationService.create(new ReservationRequest("브라운", LocalDate.now().plusDays(1), timeId, themeId));
        Slot slot = findSlot(created.date());
        reservationWaitingDao.create(ReservationWaiting.create("네오", slot));

        ReservationRequest newSlot = new ReservationRequest("브라운", LocalDate.now().plusDays(2), timeId2, themeId);
        reservationService.update(created.id(), newSlot);

        List<ReservationResponse> all = reservationService.readAll();
        assertThat(all).extracting(ReservationResponse::name).contains("네오");
        assertThat(reservationWaitingDao.findAllReservationWaiting()).isEmpty();
    }

    @Test
    void 예약_삭제_시_대기열이_여러_개면_가장_먼저_등록된_대기자가_승격된다() {
        setUpTimeAndTheme();
        ReservationResponse created = reservationService.create(new ReservationRequest("브라운", LocalDate.now().plusDays(1), timeId, themeId));
        Slot slot = findSlot(created.date());
        reservationWaitingDao.create(ReservationWaiting.restore(null, slot, "네오", null, LocalDateTime.now()));
        reservationWaitingDao.create(ReservationWaiting.restore(null, slot, "제이슨", null, LocalDateTime.now().plusSeconds(1)));

        reservationService.delete(created.id());

        List<ReservationResponse> remaining = reservationService.readAll();
        assertThat(remaining).hasSize(1);
        assertThat(remaining.get(0).name()).isEqualTo("네오");
        assertThat(reservationWaitingDao.findAllReservationWaiting()).hasSize(1);
        assertThat(reservationWaitingDao.findAllReservationWaiting().get(0).getName()).isEqualTo("제이슨");
    }
}
