package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationWaiting.ReservationWaiting;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.slot.Slot;
import roomescape.domain.slot.SlotDomainService;
import roomescape.domain.theme.Theme;
import roomescape.dto.reservationWaiting.ReservationWaitingRequest;
import roomescape.dto.reservationWaiting.ReservationWaitingResponse;
import roomescape.exception.InvalidInputException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.fake.FakeReservationQueryingDao;
import roomescape.fake.FakeReservationWaitingDao;
import roomescape.fake.FakeSlotDao;

class ReservationWaitingServiceTest {

    private FakeReservationWaitingDao waitingDao;
    private FakeSlotDao slotDao;
    private FakeReservationQueryingDao reservationQueryingDao;
    private ReservationWaitingService service;

    private static final LocalDate tomorrow = LocalDate.now().plusDays(1);
    private static final ReservationTime reservationTime = new ReservationTime(1L, LocalTime.parse("10:00"));
    private static final Theme theme = new Theme(2L, "test", "설명", "url");

    @BeforeEach
    void setUp() {
        waitingDao = new FakeReservationWaitingDao();
        slotDao = new FakeSlotDao();
        reservationQueryingDao = new FakeReservationQueryingDao();

        SlotDomainService slotDomainService = new SlotDomainService(slotDao, null, null);
        service = new ReservationWaitingService(waitingDao, slotDomainService, reservationQueryingDao);
    }

    private Slot reservedSlot(Long slotId, LocalDate date, String ownerName) {
        Slot slot = Slot.restore(slotId, date, reservationTime, theme);
        slotDao.save(slot);
        reservationQueryingDao.save(Reservation.restore(slotId * 10, slot, ownerName, LocalDateTime.now()));
        return slot;
    }

    @Test
    void 예약_대기열이_정상_생성된다() {
        reservedSlot(1L, tomorrow, "다른사람");

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
        Slot slot = reservedSlot(1L, tomorrow, "다른사람");
        waitingDao.create(ReservationWaiting.restore(1L, slot, "테스트", 1L, LocalDateTime.now()));

        ReservationWaitingRequest request = new ReservationWaitingRequest("테스트", tomorrow, 1L, 2L);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void 예약_대기열이_정상_삭제된다() {
        Slot slot = reservedSlot(1L, tomorrow, "다른사람");
        waitingDao.create(ReservationWaiting.restore(1L, slot, "테스트", 1L, LocalDateTime.now()));

        assertThatCode(() -> service.delete(1L)).doesNotThrowAnyException();
    }

    @Test
    void 예약자_이름으로_대기_등록_시도하면_예외가_발생한다() {
        reservedSlot(1L, tomorrow, "테스트");

        ReservationWaitingRequest request = new ReservationWaitingRequest("테스트", tomorrow, 1L, 2L);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void 전체_대기열을_조회한다() {
        Slot slot = reservedSlot(1L, tomorrow, "다른사람");
        waitingDao.create(ReservationWaiting.restore(1L, slot, "테스트", 1L, LocalDateTime.now()));
        waitingDao.create(ReservationWaiting.restore(2L, slot, "브라운", 2L, LocalDateTime.now()));

        List<ReservationWaitingResponse> result = service.readAll();

        assertThat(result).hasSize(2);
    }

    @Test
    void 이름으로_대기열을_조회한다() {
        Slot slot = reservedSlot(1L, tomorrow, "다른사람");
        waitingDao.create(ReservationWaiting.restore(1L, slot, "테스트", 1L, LocalDateTime.now()));
        waitingDao.create(ReservationWaiting.restore(2L, slot, "브라운", 2L, LocalDateTime.now()));

        List<ReservationWaitingResponse> result = service.readByName("테스트");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("테스트");
    }

    @Test
    void 지난_예약에_대기열_등록_시도하면_예외가_발생한다() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        reservedSlot(1L, yesterday, "다른사람");

        ReservationWaitingRequest request = new ReservationWaitingRequest("테스트", yesterday, 1L, 2L);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void 중간_순번_대기를_취소하면_뒤_순번이_변경된다() {
        Slot slot = reservedSlot(1L, tomorrow, "예약자");
        LocalDateTime base = LocalDateTime.now();
        waitingDao.create(ReservationWaiting.restore(null, slot, "대기1", null, base));
        Long secondId = waitingDao.create(ReservationWaiting.restore(null, slot, "대기2", null, base.plusSeconds(1)));
        waitingDao.create(ReservationWaiting.restore(null, slot, "대기3", null, base.plusSeconds(2)));

        service.delete(secondId);

        assertThat(service.readAll())
                .extracting(ReservationWaitingResponse::name, ReservationWaitingResponse::sequence)
                .containsExactlyInAnyOrder(
                        tuple("대기1", 1L),
                        tuple("대기3", 2L));
    }

    @Test
    void 대기열이_없을_때_전체_조회하면_빈_리스트가_반환된다() {
        List<ReservationWaitingResponse> result = service.readAll();

        assertThat(result).isEmpty();
    }

    @Test
    void 이름으로_조회_시_일치하는_항목이_없으면_빈_리스트가_반환된다() {
        Slot slot = reservedSlot(1L, tomorrow, "다른사람");
        waitingDao.create(ReservationWaiting.restore(1L, slot, "테스트", 1L, LocalDateTime.now()));

        List<ReservationWaitingResponse> result = service.readByName("없는사람");

        assertThat(result).isEmpty();
    }

    @Test
    void 서로_다른_예약의_대기_순번은_독립적으로_계산된다() {
        Slot slot1 = reservedSlot(1L, tomorrow, "예약자A");
        Slot slot2 = reservedSlot(2L, tomorrow.plusDays(1), "예약자B");

        waitingDao.create(ReservationWaiting.restore(1L, slot1, "대기1", null, LocalDateTime.now()));
        waitingDao.create(ReservationWaiting.restore(2L, slot1, "대기2", null, LocalDateTime.now().plusSeconds(1)));
        waitingDao.create(ReservationWaiting.restore(3L, slot2, "대기3", null, LocalDateTime.now()));

        List<ReservationWaitingResponse> slot1Waitings = service.readAll().stream()
                .filter(r -> r.date().equals(tomorrow))
                .toList();
        List<ReservationWaitingResponse> slot2Waitings = service.readAll().stream()
                .filter(r -> r.date().equals(tomorrow.plusDays(1)))
                .toList();

        assertThat(slot1Waitings).hasSize(2);
        assertThat(slot2Waitings).hasSize(1);
        assertThat(slot2Waitings.get(0).sequence()).isEqualTo(1L);
    }
}
