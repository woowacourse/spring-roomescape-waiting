package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.config.TestClockConfig;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWithStatus;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.domain.Waitlist;
import roomescape.domain.exception.RoomEscapeException;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationUpdateRequest;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.SlotRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitlistRepository;

@SpringBootTest
@Import(TestClockConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationServiceTest {

    private static final LocalDate FIXED_TODAY = TestClockConfig.FIXED_NOW.toLocalDate();
    private static final LocalTime FIXED_TIME = TestClockConfig.FIXED_NOW.toLocalTime();
    private static final LocalTime BEFORE_FIXED_TIME = FIXED_TIME.minusHours(1);
    private static final LocalDate FUTURE_FIRST_DATE = FIXED_TODAY.plusDays(1);
    private static final LocalDate FUTURE_SECOND_DATE = FIXED_TODAY.plusDays(2);
    private static final LocalTime TEN = LocalTime.of(10, 0);

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private WaitlistRepository waitlistRepository;

    @Autowired
    private ReservationTimeRepository timeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private SlotRepository slotRepository;

    @Test
    void 예약을_추가한다() {
        ReservationTime reservationTime = createReservationTime(TEN);
        Theme theme = createTheme();

        ReservationRequest request = new ReservationRequest(
            "브라운",
            FUTURE_SECOND_DATE,
            reservationTime.getId(),
            theme.getId()
        );

        ReservationWithStatus reservationWithStatus = reservationService.reserveOrWait(request);

        assertThat(reservationWithStatus.getId()).isNotNull();
        assertThat(reservationWithStatus.getName()).isEqualTo("브라운");
        assertThat(reservationWithStatus.getDate()).isEqualTo(FUTURE_SECOND_DATE);
        assertThat(reservationWithStatus.getTime().getId()).isEqualTo(reservationTime.getId());
        assertThat(reservationWithStatus.getTime().getStartAt()).isEqualTo(reservationTime.getStartAt());
        assertThat(reservationWithStatus.getTheme().getId()).isEqualTo(theme.getId());
        assertThat(reservationWithStatus.getTheme().getName()).isEqualTo(theme.getName());
    }

    @Test
    void 현재시각과_같은_예약은_추가할_수_있다() {
        ReservationTime reservationTime = createReservationTime(FIXED_TIME);
        Theme theme = createTheme();

        ReservationRequest request = new ReservationRequest(
            "브라운",
            FIXED_TODAY,
            reservationTime.getId(),
            theme.getId()
        );

        ReservationWithStatus reservationWithStatus = reservationService.reserveOrWait(request);

        assertThat(reservationWithStatus.getStatus()).isEqualTo(ReservationStatus.RESERVED);
        assertThat(reservationWithStatus.getDate()).isEqualTo(FIXED_TODAY);
        assertThat(reservationWithStatus.getTime().getStartAt()).isEqualTo(FIXED_TIME);
    }

    @Test
    void 예약을_추가할_때_예약시간이_없는_경우_예외() {
        Theme theme = createTheme();

        ReservationRequest request = new ReservationRequest(
            "브라운",
            FUTURE_SECOND_DATE,
            1L,
            theme.getId()
        );

        assertThatThrownBy(() -> reservationService.reserveOrWait(request))
            .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 예약을_추가할_때_테마가_없는_경우_예외() {
        ReservationTime reservationTime = createReservationTime(TEN);

        ReservationRequest request = new ReservationRequest(
            "브라운",
            FUTURE_SECOND_DATE,
            reservationTime.getId(),
            1L
        );

        assertThatThrownBy(() -> reservationService.reserveOrWait(request))
            .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 다른_사용자가_이미_예약한_슬롯이면_대기_등록된다() {
        ReservationTime reservationTime = createReservationTime(TEN);
        Theme theme = createTheme();

        ReservationRequest request = new ReservationRequest(
            "브라운",
            FUTURE_SECOND_DATE,
            reservationTime.getId(),
            theme.getId()
        );

        reservationService.reserveOrWait(request);

        ReservationRequest waitlistFirstRequest = new ReservationRequest(
            "워니",
            FUTURE_SECOND_DATE,
            reservationTime.getId(),
            theme.getId()
        );
        reservationService.reserveOrWait(waitlistFirstRequest);

        String other = "브리";
        ReservationRequest waitlistSecondRequest = new ReservationRequest(
            other,
            FUTURE_SECOND_DATE,
            reservationTime.getId(),
            theme.getId()
        );
        ReservationWithStatus reservationWithStatus = reservationService.reserveOrWait(waitlistSecondRequest);

        assertThat(reservationWithStatus.getId()).isNotNull();
        assertThat(reservationWithStatus.getName()).isEqualTo(other);
        assertThat(reservationWithStatus.getDate()).isEqualTo(FUTURE_SECOND_DATE);
        assertThat(reservationWithStatus.getTime().getId()).isEqualTo(reservationTime.getId());
        assertThat(reservationWithStatus.getTheme().getId()).isEqualTo(theme.getId());
        assertThat(reservationWithStatus.getStatus()).isEqualTo(ReservationStatus.WAITING);
        assertThat(reservationWithStatus.getWaitingOrder()).isEqualTo(2);

        Waitlist waitlist = waitlistRepository.getById(
            reservationWithStatus.getId(),
            "존재하지 않는 예약 대기입니다."
        );

        assertThat(waitlist.getCreatedAt()).isEqualTo(TestClockConfig.FIXED_NOW);
    }

    @Test
    void 걑은_사용자가_이미_예약한_슬롯이면_대기_순번으로_넘어가지_않고_예외() {
        ReservationTime reservationTime = createReservationTime(TEN);
        Theme theme = createTheme();

        ReservationRequest request = new ReservationRequest(
            "브라운",
            FUTURE_SECOND_DATE,
            reservationTime.getId(),
            theme.getId()
        );

        reservationService.reserveOrWait(request);

        assertThatThrownBy(() -> reservationService.reserveOrWait(request))
            .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 다른_사용자가_이미_예약한_슬롯에서_사용자가_중복_대기할_수_없다() {
        ReservationTime reservationTime = createReservationTime(TEN);
        Theme theme = createTheme();

        ReservationRequest request = new ReservationRequest(
            "브라운",
            FUTURE_SECOND_DATE,
            reservationTime.getId(),
            theme.getId()
        );

        reservationService.reserveOrWait(request);

        ReservationRequest waitlistRequest = new ReservationRequest(
            "브리",
            FUTURE_SECOND_DATE,
            reservationTime.getId(),
            theme.getId()
        );
        reservationService.reserveOrWait(waitlistRequest);

        assertThatThrownBy(() -> reservationService.reserveOrWait(waitlistRequest))
            .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 모든_예약을_조회한다() {
        ReservationTime reservationTime = createReservationTime(TEN);
        Theme theme = createTheme();

        ReservationRequest request = new ReservationRequest(
            "브라운",
            FUTURE_SECOND_DATE,
            reservationTime.getId(),
            theme.getId()
        );

        reservationService.reserveOrWait(request);

        List<Reservation> reservations = reservationService.getReservations();

        assertThat(reservations).hasSize(1);

        Reservation result = reservations.getFirst();
        assertThat(result.getName()).isEqualTo("브라운");
        assertThat(result.getDate()).isEqualTo(FUTURE_SECOND_DATE);
        assertThat(result.getTime().getId()).isEqualTo(reservationTime.getId());
        assertThat(result.getTheme().getId()).isEqualTo(theme.getId());
    }

    @Test
    void 내_예약_목록에서_예약과_대기를_함께_조회한다() {
        ReservationTime twelveClock = createReservationTime(LocalTime.of(12, 0));
        Theme theme = createTheme();

        String name = "브라운";
        String anotherName = "브리";
        ReservationRequest existingReservationRequest = new ReservationRequest(
            name,
            FUTURE_SECOND_DATE,
            twelveClock.getId(),
            theme.getId()
        );
        ReservationRequest myWaitingRequest = new ReservationRequest(
            anotherName,
            FUTURE_SECOND_DATE,
            twelveClock.getId(),
            theme.getId()
        );
        ReservationRequest myReservedRequest = new ReservationRequest(
            anotherName,
            FUTURE_FIRST_DATE,
            twelveClock.getId(),
            theme.getId()
        );

        reservationService.reserveOrWait(existingReservationRequest);
        reservationService.reserveOrWait(myReservedRequest);
        reservationService.reserveOrWait(myWaitingRequest);

        List<ReservationWithStatus> myReservations = reservationService.getMyReservations(anotherName);

        assertThat(myReservations).hasSize(2);
        assertThat(myReservations)
            .extracting(
                ReservationWithStatus::getName,
                ReservationWithStatus::getDate,
                reservation -> reservation.getTime().getId(),
                reservation -> reservation.getTheme().getId(),
                reservation -> reservation.getStatus(),
                ReservationWithStatus::getWaitingOrder
            )
            .containsExactly(
                tuple(anotherName, FUTURE_SECOND_DATE, twelveClock.getId(), theme.getId(),
                    ReservationStatus.WAITING, 1),
                tuple(anotherName, FUTURE_FIRST_DATE, twelveClock.getId(), theme.getId(),
                    ReservationStatus.RESERVED, null)
            );
    }

    @Test
    void id에_맞는_예약을_조회한다() {
        ReservationTime reservationTime = createReservationTime(TEN);
        Theme theme = createTheme();

        ReservationRequest request = new ReservationRequest(
            "브라운",
            FUTURE_SECOND_DATE,
            reservationTime.getId(),
            theme.getId()
        );

        ReservationWithStatus savedReservation = reservationService.reserveOrWait(request);

        Reservation reservation = reservationService.getReservation(savedReservation.getId());

        assertThat(reservation.getId()).isEqualTo(savedReservation.getId());
        assertThat(reservation.getName()).isEqualTo("브라운");
        assertThat(reservation.getDate()).isEqualTo(FUTURE_SECOND_DATE);
        assertThat(reservation.getTime().getId()).isEqualTo(reservationTime.getId());
        assertThat(reservation.getTheme().getId()).isEqualTo(theme.getId());
    }

    @Test
    void 예약을_삭제한다() {
        ReservationTime reservationTime = createReservationTime(TEN);
        Theme theme = createTheme();

        ReservationRequest request = new ReservationRequest(
            "브라운",
            FUTURE_SECOND_DATE,
            reservationTime.getId(),
            theme.getId()
        );

        ReservationWithStatus savedReservation = reservationService.reserveOrWait(request);

        reservationService.deleteReservation(savedReservation.getId());

        assertThatThrownBy(() -> reservationService.getReservation(savedReservation.getId()))
            .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 없는_예약을_삭제할_수_없다() {
        assertThatThrownBy(() -> reservationService.deleteReservation(1L))
            .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 내_예약을_취소한다() {
        ReservationTime reservationTime = createReservationTime(TEN);
        Theme theme = createTheme();
        String name = "브라운";

        ReservationRequest request = new ReservationRequest(
            name,
            FUTURE_SECOND_DATE,
            reservationTime.getId(),
            theme.getId()
        );
        ReservationWithStatus reservation = reservationService.reserveOrWait(request);

        reservationService.cancelMyReservation(reservation.getId(), name);

        assertThatThrownBy(() -> reservationService.getReservation(reservation.getId()))
            .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 사용자_예약을_취소할_때_존재하지_않는_예약이면_예외() {
        assertThatThrownBy(() -> reservationService.cancelMyReservation(1L, "브라운"))
            .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 예약을_취소할_때_이미_지난_예약이면_예외() {
        ReservationTime pastTime = createReservationTime(BEFORE_FIXED_TIME);
        Theme theme = createTheme();
        String name = "브라운";
        Long reservationId = reservationRepository.save(createReservation(
            name,
            FIXED_TODAY,
            pastTime,
            theme
        ));

        assertThatThrownBy(() -> reservationService.cancelMyReservation(reservationId, name))
            .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 선택한_예약에_내_이름이_일치하면_예약의_날짜와_시간을_수정할_수_있다() {
        ReservationTime reservationTime = createReservationTime(TEN);
        Theme theme = createTheme();
        String name = "브라운";
        ReservationRequest request = new ReservationRequest(
            name,
            FUTURE_SECOND_DATE,
            reservationTime.getId(),
            theme.getId()
        );
        ReservationWithStatus reservation = reservationService.reserveOrWait(request);

        ReservationTime updateTime = createReservationTime(LocalTime.of(12, 0));
        LocalDate updateDate = FUTURE_SECOND_DATE.plusDays(1);
        ReservationUpdateRequest updateRequest = new ReservationUpdateRequest(
            updateDate,
            updateTime.getId()
        );

        Reservation updatedReservation = reservationService.updateReservation(reservation.getId(), name, updateRequest);

        assertThat(updatedReservation.getId()).isNotNull();
        assertThat(updatedReservation.getName()).isEqualTo(name);
        assertThat(updatedReservation.getDate()).isEqualTo(updateDate);
        assertThat(updatedReservation.getTime().getId()).isEqualTo(updateTime.getId());
        assertThat(updatedReservation.getTime().getStartAt()).isEqualTo(updateTime.getStartAt());
        assertThat(updatedReservation.getTheme().getId()).isEqualTo(theme.getId());
        assertThat(updatedReservation.getTheme().getName()).isEqualTo(theme.getName());
    }

    @Test
    void 예약을_수정할_때_존재하지_않는_예약이면_예외() {
        ReservationTime updateTime = createReservationTime(TEN);
        ReservationUpdateRequest updateRequest = new ReservationUpdateRequest(
            FUTURE_SECOND_DATE,
            updateTime.getId()
        );

        assertThatThrownBy(() -> reservationService.updateReservation(1L, "브라운", updateRequest))
            .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 예약을_수정할_때_존재하지_않는_시간_ID이면_예외() {
        ReservationTime reservationTime = createReservationTime(TEN);
        Theme theme = createTheme();
        String name = "브라운";
        ReservationRequest request = new ReservationRequest(
            name,
            FUTURE_SECOND_DATE,
            reservationTime.getId(),
            theme.getId()
        );
        ReservationWithStatus reservation = reservationService.reserveOrWait(request);

        ReservationUpdateRequest updateRequest = new ReservationUpdateRequest(
            FUTURE_SECOND_DATE.plusDays(1),
            999L
        );

        assertThatThrownBy(() -> reservationService.updateReservation(reservation.getId(), name, updateRequest))
            .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 예약을_수정할_때_변경하려는_예약_시간이_이미_차_있으면_예외() {
        ReservationTime tenClock = createReservationTime(TEN);
        ReservationTime twelveClock = createReservationTime(LocalTime.of(12, 0));
        Theme theme = createTheme();

        String name = "브라운";
        ReservationRequest request = new ReservationRequest(
            name,
            FUTURE_SECOND_DATE,
            tenClock.getId(),
            theme.getId()
        );
        ReservationWithStatus reservation = reservationService.reserveOrWait(request);

        ReservationRequest anotherRequest = new ReservationRequest(
            "브리",
            FUTURE_SECOND_DATE.plusDays(1),
            twelveClock.getId(),
            theme.getId()
        );
        reservationService.reserveOrWait(anotherRequest);

        ReservationUpdateRequest updateRequest = new ReservationUpdateRequest(
            FUTURE_SECOND_DATE.plusDays(1),
            twelveClock.getId()
        );

        assertThatThrownBy(() -> reservationService.updateReservation(reservation.getId(), name, updateRequest))
            .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 예약_취소_시_첫_번째_대기가_예약으로_승격된다() {
        ReservationTime tenClock = createReservationTime(TEN);
        Theme theme = createTheme();

        ReservationRequest brownRequest = new ReservationRequest(
            "브라운",
            FUTURE_SECOND_DATE,
            tenClock.getId(),
            theme.getId()
        );

        ReservationRequest neoRequest = new ReservationRequest(
            "네오",
            FUTURE_SECOND_DATE,
            tenClock.getId(),
            theme.getId()
        );

        ReservationRequest pobiRequest = new ReservationRequest(
            "포비",
            FUTURE_SECOND_DATE,
            tenClock.getId(),
            theme.getId()
        );

        ReservationWithStatus savedReservation = reservationService.reserveOrWait(brownRequest);
        reservationService.reserveOrWait(neoRequest);
        reservationService.reserveOrWait(pobiRequest);
        Long slotId = reservationRepository.findById(savedReservation.getId()).orElseThrow().getSlot().getId();
        reservationService.cancelMyReservation(savedReservation.getId(), "브라운");

        List<Reservation> reservations = reservationService.getReservations();
        assertThat(reservations)
            .extracting(Reservation::getName)
            .contains("네오");

        List<Waitlist> waitlists = waitlistRepository.findBySlotId(slotId);
        assertThat(waitlists)
            .extracting(Waitlist::getName)
            .doesNotContain("네오");
    }

    @Test
    void 관리자_예약_삭제_시_첫_번째_대기가_예약으로_승격된다() {
        ReservationTime tenClock = createReservationTime(TEN);
        Theme theme = createTheme();

        ReservationRequest brownRequest = new ReservationRequest(
            "브라운",
            FUTURE_SECOND_DATE,
            tenClock.getId(),
            theme.getId()
        );

        ReservationRequest neoRequest = new ReservationRequest(
            "네오",
            FUTURE_SECOND_DATE,
            tenClock.getId(),
            theme.getId()
        );

        ReservationRequest pobiRequest = new ReservationRequest(
            "포비",
            FUTURE_SECOND_DATE,
            tenClock.getId(),
            theme.getId()
        );

        ReservationWithStatus savedReservation = reservationService.reserveOrWait(brownRequest);
        reservationService.reserveOrWait(neoRequest);
        reservationService.reserveOrWait(pobiRequest);

        Long slotId = reservationRepository.findById(savedReservation.getId())
            .orElseThrow().getSlot().getId();

        reservationService.deleteReservation(savedReservation.getId());

        List<Reservation> reservations = reservationService.getReservations();
        assertThat(reservations)
            .extracting(Reservation::getName)
            .contains("네오");

        List<Waitlist> waitlists = waitlistRepository.findBySlotId(slotId);
        assertThat(waitlists)
            .extracting(Waitlist::getName)
            .doesNotContain("네오");
    }

    @Test
    void 대기가_없으면_예약만_삭제된다() {
        ReservationTime tenClock = createReservationTime(TEN);
        Theme theme = createTheme();

        String name = "브라운";
        ReservationRequest request = new ReservationRequest(
            name,
            FUTURE_SECOND_DATE,
            tenClock.getId(),
            theme.getId()
        );

        ReservationWithStatus saved = reservationService.reserveOrWait(request);
        reservationService.deleteReservation(saved.getId());

        assertThatThrownBy(() -> reservationService.getReservation(saved.getId()))
            .isInstanceOf(RoomEscapeException.class);
    }

    private ReservationTime createReservationTime(LocalTime time) {
        ReservationTime reservationTime = new ReservationTime(time);
        Long id = timeRepository.save(reservationTime).getId();
        return new ReservationTime(id, reservationTime.getStartAt());
    }

    private Theme createTheme() {
        Theme theme = new Theme("방탈출 제목", "방탈출 설명", "thumbnail.png");
        Long id = themeRepository.save(theme).getId();
        return new Theme(
            id,
            theme.getName(),
            theme.getDescription(),
            theme.getThumbnailImageUrl()
        );
    }

    private Reservation createReservation(String name, LocalDate date, ReservationTime time, Theme theme) {
        Slot slot = slotRepository.getOrCreate(Slot.of(date, time, theme));
        return new Reservation(name, slot);
    }
}
