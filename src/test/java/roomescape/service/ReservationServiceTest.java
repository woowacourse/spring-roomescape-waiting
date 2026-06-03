package roomescape.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.ServiceTest;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.ThemeDao;
import roomescape.dao.WaitingDao;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.dto.request.ReservationRequest;
import roomescape.dto.response.ReservationResponse;
import roomescape.dto.response.ReservationTimeResponse;
import roomescape.dto.response.ThemeResponse;
import roomescape.exception.code.ReservationErrorCode;
import roomescape.exception.code.ReservationTimeErrorCode;
import roomescape.exception.code.ThemeErrorCode;
import roomescape.exception.domain.ReservationException;
import roomescape.exception.domain.ReservationTimeException;
import roomescape.exception.domain.ThemeException;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class ReservationServiceTest extends ServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationTimeDao reservationTimeDao;

    @Autowired
    private ThemeDao themeDao;
    @Autowired
    private WaitingDao waitingDao;

    @Autowired
    private Clock clock;
    @Autowired
    private ReservationDao reservationDao;
    @Autowired
    private WaitingService waitingService;

    @Test
    void 예약을_생성할_수_있다() {
        // given
        ReservationTime reservationTime = saveReservationTime(LocalTime.of(13, 0));
        Theme theme = saveTheme("테마1", "로지와 러키의 방탈출", "https:fsof/ommff");

        ReservationRequest request = createReservationRequest(reservationTime.getId(), theme.getId(), LocalDate.of(2026, 5, 8));

        // when
        ReservationResponse response = reservationService.create(request);

        // then
        assertThat(response)
                .extracting(
                        ReservationResponse::name,
                        ReservationResponse::date,
                        reservationResponse -> reservationResponse.time().id()
                )
                .containsExactly(
                        request.name(),
                        request.date(),
                        request.timeId()
                );
    }

    @Test
    void 예약_생성시_예약시간이_존재하지_않으면_예외가_발생한다() {
        // given
        Theme theme = saveTheme("테마1", "로지와 러키의 방탈출", "https:fsof/ommff");
        ReservationRequest request = createReservationRequest(0L, theme.getId(), LocalDate.of(2026, 5, 8));

        // when & then
        assertThatThrownBy(() -> reservationService.create(request))
                .isInstanceOf(ReservationTimeException.class)
                .hasMessage(ReservationTimeErrorCode.RESERVATION_TIME_NOT_FOUND.getMessage());
    }

    @Test
    void 예약_생성시_테마가_존재하지_않으면_예외가_발생한다() {
        // given
        ReservationTime reservationTime = saveReservationTime(LocalTime.of(13, 0));
        ReservationRequest request = createReservationRequest(reservationTime.getId(), 0L, LocalDate.of(2026, 5, 8));

        // when & then
        assertThatThrownBy(() -> reservationService.create(request))
                .isInstanceOf(ThemeException.class)
                .hasMessage(ThemeErrorCode.THEME_NOT_FOUND.getMessage());
    }

    @Test
    void 예약_생성시_동일한_조건의_예약이_이미_존재하면_예외가_발생한다() {
        // given
        ReservationTime reservationTime = saveReservationTime(LocalTime.of(13, 0));
        Theme theme = saveTheme("테마1", "로지와 러키의 방탈출", "https:fsof/ommff");
        ReservationRequest request = createReservationRequest(reservationTime.getId(), theme.getId(), LocalDate.of(2026, 5, 8));
        reservationService.create(request);

        // when & then
        assertThatThrownBy(() -> reservationService.create(request))
                .isInstanceOf(ReservationException.class)
                .hasMessage(ReservationErrorCode.RESERVATION_ALREADY_EXISTS.getMessage());
    }

    @Test
    void 예약을_수정할_수_있다() {
        // given
        ReservationTime originalTime = saveReservationTime(LocalTime.of(10, 0));
        ReservationTime changedTime = saveReservationTime(LocalTime.of(20, 0));

        Theme originalTheme = saveTheme("방탈출1", "로지와 러키의 방탈출", "https:fsof/ommff");
        Theme changedTheme = saveTheme("방탈출2", "밤밤과 러로의 방탈출", "https:fsof/sdafjifdsmmff");

        ReservationRequest createRequest = createReservationRequest(
                originalTime.getId(),
                originalTheme.getId(),
                LocalDate.of(2026, 5, 10)
        );
        ReservationResponse savedReservation = reservationService.create(createRequest);

        ReservationRequest updateRequest = new ReservationRequest(
                "브라운",
                LocalDate.of(2026, 5, 12),
                changedTime.getId(),
                changedTheme.getId()
        );

        // when
        ReservationResponse response = reservationService.update(savedReservation.id(), updateRequest);

        // then
        assertAll(
                () -> assertThat(response)
                        .extracting(
                                ReservationResponse::id,
                                ReservationResponse::name,
                                ReservationResponse::date
                        )
                        .containsExactly(
                                savedReservation.id(),
                                "브라운",
                                LocalDate.of(2026, 5, 12)
                        ),
                () -> assertThat(response.time())
                        .extracting(
                                ReservationTimeResponse::id,
                                ReservationTimeResponse::startAt
                        )
                        .containsExactly(
                                changedTime.getId(),
                                changedTime.getStartAt()
                        ),
                () -> assertThat(response.theme())
                        .extracting(
                                ThemeResponse::id,
                                ThemeResponse::name,
                                ThemeResponse::description,
                                ThemeResponse::thumbnail
                        )
                        .containsExactly(
                                changedTheme.getId(),
                                changedTheme.getName(),
                                changedTheme.getDescription(),
                                changedTheme.getThumbnail()
                        )
        );
    }

    @Test
    void 이미_예약된_날짜_시간_테마로는_예약을_수정할_수_없다() {
        // given
        ReservationTime alreadyReservedTime = saveReservationTime(LocalTime.of(20, 0));
        Theme alreadyReservedTheme = saveTheme("방탈출2", "밤밤과 러로의 방탈출", "https:fsof/sdafjifdsmmff");
        ReservationRequest alreadyReservedRequest = new ReservationRequest(
                "로지",
                LocalDate.of(2026, 5, 12),
                alreadyReservedTime.getId(),
                alreadyReservedTheme.getId()
        );
        reservationService.create(alreadyReservedRequest);

        ReservationTime originalTime = saveReservationTime(LocalTime.of(10, 0));
        Theme originalTheme = saveTheme("방탈출1", "로지와 러키의 방탈출", "https:fsof/ommff");
        ReservationRequest createRequest = createReservationRequest(
                originalTime.getId(),
                originalTheme.getId(),
                LocalDate.of(2026, 5, 10)
        );
        ReservationResponse savedReservation = reservationService.create(createRequest);

        ReservationRequest updateRequest = new ReservationRequest(
                "브라운",
                LocalDate.of(2026, 5, 12),
                alreadyReservedTime.getId(),
                alreadyReservedTheme.getId()
        );

        // when & then
        assertThatThrownBy(() -> reservationService.update(savedReservation.id(), updateRequest))
                .isInstanceOf(ReservationException.class)
                .hasMessage(ReservationErrorCode.RESERVATION_ALREADY_EXISTS.getMessage());
    }

    @Test
    void 현재_시간보다_이전_날짜로_예약을_수정하면_예외가_발생한다() {
        ReservationTime originalTime = saveReservationTime(LocalTime.of(11, 0));
        Theme originalTheme = saveTheme("방탈출1", "로지와 러키의 방탈출", "https:fsof/ommff");

        ReservationRequest createRequest = new ReservationRequest(
                "러키",
                LocalDate.of(2026, 5, 25),
                originalTime.getId(),
                originalTheme.getId()
        );
        ReservationResponse savedReservation = reservationService.create(createRequest);

        ReservationTime pastTime = saveReservationTime(LocalTime.of(10, 0));
        Theme updateTheme = saveTheme("방탈출2", "밤밤과 러로의 방탈출", "https:fsof/sdafjifdsmmff");

        ReservationRequest updateRequest = new ReservationRequest(
                "러키",
                LocalDate.of(2026, 5, 4),
                pastTime.getId(),
                updateTheme.getId()
        );

        // when & then
        assertThatThrownBy(() -> reservationService.update(savedReservation.id(), updateRequest))
                .isInstanceOf(ReservationException.class)
                .hasMessage(ReservationErrorCode.PAST_DATE_NOT_ALLOWED.getMessage());
    }

    @Test
    void 예약_취소_시_해당_슬롯의_대기자만_승격된다() {
        // given
        ReservationTime reservationTime = saveReservationTime(LocalTime.of(13, 0));
        Theme theme = saveTheme("테마1", "로지와 러키의 방탈출", "https:fsof/ommff");

        // 예약 1 (슬롯 1)
        ReservationRequest request1 = createReservationRequest(reservationTime.getId(), theme.getId(), LocalDate.of(2026, 5, 8));
        ReservationResponse response1 = reservationService.create(request1);
        Slot slot1 = reservationDao.findById(response1.id()).orElseThrow().getSlot();

        // 예약 2 (슬롯 2)
        ReservationRequest request2 = createReservationRequest(reservationTime.getId(), theme.getId(), LocalDate.of(2026, 5, 9));
        ReservationResponse response2 = reservationService.create(request2);
        Slot slot2 = reservationDao.findById(response2.id()).orElseThrow().getSlot();

        saveWaiting("대기자1", slot1);
        saveWaiting("대기자2", slot2);

        // when
        reservationService.delete(response1.id());

        // then
        assertAll(
                () -> assertThat(reservationService.getReservationsByName("대기자1")).hasSize(1),
                () -> assertThat(reservationService.getReservationsByName("대기자2")).isEmpty(),
                () -> assertThat(waitingService.getWaitingsByName("대기자2")).hasSize(1)
        );
    }

    private void saveWaiting(String userName, Slot slot) {
        waitingDao.save(new Waiting(LocalDateTime.now(clock), slot.getId(), userName));
    }

    @Test
    void 삭제할_예약이_존재하지_않으면_예외를_반환한다() {
        // when & then
        assertThatThrownBy(() -> reservationService.delete(0L))
                .isInstanceOf(ReservationException.class)
                .hasMessage(ReservationErrorCode.RESERVATION_NOT_FOUND.getMessage());
    }

    @Test
    void 예약_취소_마감_기한이_지난_예약은_삭제할_수_없다() {
        // given
        ReservationTime reservationTime = saveReservationTime(LocalTime.of(13, 0));
        Theme theme = saveTheme("테마1", "로지와 러키의 방탈출", "https:fsof/ommff");

        ReservationRequest request = createReservationRequest(
                reservationTime.getId(),
                theme.getId(),
                LocalDate.of(2026, 5, 5)
        );
        ReservationResponse response = reservationService.create(request);

        int beforeSize = reservationService.getReservations().size();

        // when & then
        assertThatThrownBy(() -> reservationService.delete(response.id()))
                .isInstanceOf(ReservationException.class)
                .hasMessage(ReservationErrorCode.RESERVATION_CANCEL_DEADLINE_PASSED.getMessage());

        List<ReservationResponse> reservations = reservationService.getReservations();
        assertAll(
                () -> assertThat(reservations).hasSize(beforeSize),
                () -> assertThat(reservations)
                        .extracting(ReservationResponse::id)
                        .contains(response.id())
        );
    }

    private ReservationTime saveReservationTime(LocalTime startAt) {
        ReservationTime reservationTime = new ReservationTime(startAt);
        return reservationTimeDao.save(reservationTime);
    }

    private Theme saveTheme(String name, String description, String thumbnail) {
        Theme theme = new Theme(name, description, thumbnail);
        return themeDao.save(theme);
    }

    private ReservationRequest createReservationRequest(long timeId, long themeId, LocalDate localDate) {
        return new ReservationRequest(
                "예약1",
                localDate,
                timeId,
                themeId
        );
    }
}
