package roomescape.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import roomescape.common.exception.ErrorCode;
import roomescape.common.exception.ReservationErrorCode;
import roomescape.common.exception.ReservationTimeErrorCode;
import roomescape.common.exception.RoomEscapeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.RoomEscapeFixture;
import roomescape.controller.dto.request.ReservationCreateRequest;
import roomescape.controller.dto.request.ReservationUpdateRequest;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationName;
import roomescape.domain.reservation.ReservationResult;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeName;
import roomescape.domain.theme.ThumbnailUrl;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {
    private static final String URL = "https://zeze.com/thumb.jpg";
    private static final String NAME = "제제";
    private static final LocalDateTime TODAY = LocalDateTime.of(2026, 5, 10, 10, 0, 0);
    private static final Reservation DUMMY = Reservation.load(
            1L,
            new ReservationName(NAME),
            new ReservationDate(LocalDate.of(2099, 1, 1)),
            ReservationTime.of(1L, LocalTime.of(10, 0)),
            Theme.load(1L, new ThemeName("any"), "any", new ThumbnailUrl(URL)),
            TODAY
    );
    private static final long NOT_EXISTS_ID = Long.MAX_VALUE;
    private static final long EXISTS_ID = 1L;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationTimeRepository reservationTimeRepository;

    @Mock
    private ThemeRepository themeRepository;

    @InjectMocks
    private ReservationService reservationService;


    @Test
    void 예약_취소_성공() {
        given(reservationRepository.findById(1L)).willReturn(Optional.of(DUMMY));

        reservationService.cancel(1L, NAME, LocalDateTime.MIN);

        verify(reservationRepository).deleteById(1L);
    }

    @Test
    void 존재하지_않는_예약_취소시_예외_발생() {
        given(reservationRepository.findById(999L)).willReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> reservationService.cancel(999L, NAME, LocalDateTime.MIN))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 존재하지_않는_시간으로_예약시_예외() {
        given(reservationTimeRepository.findById(999L)).willReturn(Optional.empty());

        ReservationCreateRequest request = new ReservationCreateRequest("zeze", LocalDate.parse("2026-05-03"), 999L,
                1L);

        Assertions.assertThatThrownBy(() -> reservationService.reserve(request, LocalDateTime.MAX))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 지나간_날짜로_예약_시_예외가_발생해야_한다() {
        ReservationTime reservationTime = ReservationTime.of(LocalTime.parse("11:00"));
        Theme theme = Theme.load(1L, new ThemeName("테마1"), "설명", new ThumbnailUrl(URL));

        ReservationCreateRequest request = new ReservationCreateRequest("zeze", LocalDate.parse("2026-04-05"), 1L, 1L);
        given(reservationTimeRepository.findById(1L)).willReturn(Optional.of(reservationTime));
        given(themeRepository.findById(1L)).willReturn(Optional.of(theme));

        Assertions.assertThatThrownBy(() -> reservationService.reserve(request, LocalDateTime.MAX));
    }

    @Test
    void 같은_날짜이며_시간이_1초_전이면_예약에_성공해야_한다() {
        ReservationTime reservationTime = ReservationTime.of(LocalTime.parse("11:00"));
        Theme theme = Theme.load(1L, new ThemeName("테마1"), "설명", new ThumbnailUrl(URL));

        ReservationCreateRequest request = new ReservationCreateRequest(NAME, LocalDate.of(2026,4,5), 1L, 1L);
        given(reservationTimeRepository.findById(1L)).willReturn(Optional.of(reservationTime));
        given(themeRepository.findById(1L)).willReturn(Optional.of(theme));
        given(reservationRepository.save(any())).willReturn(DUMMY);
        given(reservationRepository.findByTimeAndThemeAndDate(any(), any(), any())).willReturn(List.of(DUMMY));

        Assertions.assertThatNoException()
                .isThrownBy(() -> reservationService.reserve(request, LocalDateTime.of(2026, 4, 5, 10, 59, 59)));
    }

    @Test
    void 같은_날짜이며_시간이_1초_지났다면_예약에_실패해야_한다() {
        ReservationTime reservationTime = ReservationTime.of(LocalTime.parse("11:00"));
        Theme theme = Theme.load(1L, new ThemeName("테마1"), "설명", new ThumbnailUrl(URL));

        ReservationCreateRequest request = new ReservationCreateRequest("zeze", LocalDate.parse("2026-04-05"), 1L, 1L);
        given(reservationTimeRepository.findById(1L)).willReturn(Optional.of(reservationTime));
        given(themeRepository.findById(1L)).willReturn(Optional.of(theme));

        Assertions.assertThatThrownBy(
                () -> reservationService.reserve(request, LocalDateTime.of(2026, 4, 5, 11, 0, 1)));
    }

    @Test
    void 미래로_예약하면_성공해야_한다() {
        ReservationTime reservationTime = ReservationTime.of(LocalTime.parse("11:00"));
        Theme theme = Theme.load(1L, new ThemeName("테마1"), "설명", new ThumbnailUrl(URL));

        ReservationCreateRequest request = new ReservationCreateRequest("zeze", LocalDate.parse("2026-04-05"), 1L, 1L);
        given(reservationTimeRepository.findById(1L)).willReturn(Optional.of(reservationTime));
        given(themeRepository.findById(1L)).willReturn(Optional.of(theme));
        given(reservationRepository.save(any())).willReturn(DUMMY);
        given(reservationRepository.findByTimeAndThemeAndDate(any(), any(), any())).willReturn(List.of(DUMMY));

        Assertions.assertThatNoException().isThrownBy(
                () -> reservationService.reserve(request, LocalDateTime.of(2026, 4, 5, 10, 59, 59)));

    }

    @Test
    void 예약_수정시_ID가_없으면_예외가_발생한다() {
        ReservationUpdateRequest request = new ReservationUpdateRequest("zeze", LocalDate.parse("2099-04-06"), 1L,
                1L);
        given(reservationRepository.findById(999L)).willReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> reservationService.update(request, 999L, LocalDateTime.MIN))
                .isInstanceOf(RoomEscapeException.class).hasMessage(
                        ReservationErrorCode.RESERVATION_NOT_FOUND.getMessage());
    }

    @Test
    void 예약_수정시_과거_날짜의_예약이면_예외가_발생한다() {
        ReservationUpdateRequest request = new ReservationUpdateRequest("zeze", LocalDate.parse("2000-04-06"), 1L,
                1L);
        given(reservationRepository.findById(1L)).willReturn(Optional.of(DUMMY));

        Assertions.assertThatThrownBy(() -> reservationService.update(request, 1L, LocalDateTime.MAX))
                .isInstanceOf(RoomEscapeException.class).hasMessage(
                        ReservationErrorCode.PAST_RESERVATION_NOT_ALLOWED.getMessage());
    }

    @Test
    void 예약_수정시_시간을_찾을_수_없으면_예외가_발생한다() {
        ReservationUpdateRequest request = new ReservationUpdateRequest("zeze", LocalDate.parse("2099-04-06"), 1L,
                1L);
        given(reservationRepository.findById(1L)).willReturn(Optional.of(DUMMY));
        given(reservationTimeRepository.findById(1L)).willReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> reservationService.update(request, 1L, LocalDateTime.MIN))
                .isInstanceOf(RoomEscapeException.class).hasMessage(
                        ReservationTimeErrorCode.RESERVATION_TIME_NOT_FOUND.getMessage());
    }

    @Test
    void 예약_수정시_사용_불가능한_날짜가_들어오면_예외가_발생한다() {
        ReservationTime reservationTime = ReservationTime.of(1L, LocalTime.parse("11:00"));

        ReservationUpdateRequest request = new ReservationUpdateRequest("zeze", LocalDate.parse("2099-04-06"), 1L,
                1L);
        given(reservationRepository.findById(1L)).willReturn(Optional.of(DUMMY));
        given(reservationTimeRepository.findById(1L)).willReturn(Optional.of(reservationTime));
        given(reservationRepository.existsByTimeAndThemeAndDateAndName(request.getTimeId(), request.getThemeId(),
                request.getDate(), request.getName())).willReturn(true);

        Assertions.assertThatThrownBy(() -> reservationService.update(request, 1L, LocalDateTime.MIN))
                .isInstanceOf(RoomEscapeException.class).hasMessage(
                        ReservationErrorCode.DUPLICATE_RESERVATION.getMessage());
    }

    @Test
    void 예약_삭제_시_ID가_존재하지_않으면_예외가_발생한다() {
        // given
        given(reservationRepository.findById(NOT_EXISTS_ID)).willThrow(RoomEscapeException.class);

        Assertions.assertThatThrownBy(() -> reservationRepository.findById(NOT_EXISTS_ID))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 예약_삭제_시_이름이_다르면_예외가_발생한다() {
        // given
        Reservation reservation = RoomEscapeFixture.reservation();
        given(reservationRepository.findById(EXISTS_ID)).willReturn(Optional.of(reservation));
        // when
        Assertions.assertThatThrownBy(() -> reservationService.cancel(EXISTS_ID, "diff", TODAY))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 예약_삭제_시_문제가_없으면_삭제되어야_한다() {
        // given
        Reservation reservation = RoomEscapeFixture.reservation();
        given(reservationRepository.findById(EXISTS_ID)).willReturn(Optional.of(reservation));

        assertThatCode(() -> reservationService.cancel(EXISTS_ID, reservation.getName().getValue(),
                TODAY)).doesNotThrowAnyException();
    }

    @Test
    void 단건_조회시_존재하는_ID면_결과를_반환한다() {
        given(reservationRepository.findById(EXISTS_ID)).willReturn(Optional.of(DUMMY));
        given(reservationRepository.findByTimeAndThemeAndDate(any(), any(), any())).willReturn(List.of(DUMMY));

        ReservationResult result = reservationService.find(EXISTS_ID);

        Assertions.assertThat(result.getReservation().getId()).isEqualTo(EXISTS_ID);
        Assertions.assertThat(result.getRank().getValue()).isEqualTo(1);
    }

    @Test
    void 단건_조회시_존재하지_않는_ID면_예외가_발생한다() {
        given(reservationRepository.findById(NOT_EXISTS_ID)).willReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> reservationService.find(NOT_EXISTS_ID))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage(ReservationErrorCode.RESERVATION_NOT_FOUND.getMessage());
    }

    @Test
    void 이름_없이_목록_조회시_전체_예약을_반환한다() {
        given(reservationRepository.findAll()).willReturn(List.of(DUMMY));
        given(reservationRepository.findByTimeAndThemeAndDate(any(), any(), any())).willReturn(List.of(DUMMY));

        List<ReservationResult> results = reservationService.findList(null);

        Assertions.assertThat(results).hasSize(1);
        Assertions.assertThat(results.get(0).getReservation().getId()).isEqualTo(EXISTS_ID);
    }

    @Test
    void 이름으로_목록_조회시_해당_이름의_예약만_반환한다() {
        given(reservationRepository.findAllByName(NAME)).willReturn(List.of(DUMMY));
        given(reservationRepository.findByTimeAndThemeAndDate(any(), any(), any())).willReturn(List.of(DUMMY));

        List<ReservationResult> results = reservationService.findList(NAME);

        Assertions.assertThat(results).hasSize(1);
        Assertions.assertThat(results.get(0).getReservation().getName().getValue()).isEqualTo(NAME);
    }

    @Test
    void 첫번째_예약은_승인_상태이다() {
        given(reservationRepository.findById(EXISTS_ID)).willReturn(Optional.of(DUMMY));
        given(reservationRepository.findByTimeAndThemeAndDate(any(), any(), any())).willReturn(List.of(DUMMY));

        ReservationResult result = reservationService.find(EXISTS_ID);

        Assertions.assertThat(result.status()).isEqualTo(roomescape.domain.reservation.Status.APPROVED);
    }

    @Test
    void 두번째_이후_예약은_대기_상태이다() {
        Reservation waiting = Reservation.load(
                2L,
                new ReservationName("대기자"),
                new ReservationDate(LocalDate.of(2099, 1, 1)),
                ReservationTime.of(1L, LocalTime.of(10, 0)),
                Theme.load(1L, new ThemeName("any"), "any", new ThumbnailUrl(URL)),
                TODAY
        );
        given(reservationRepository.findById(2L)).willReturn(Optional.of(waiting));
        given(reservationRepository.findByTimeAndThemeAndDate(any(), any(), any()))
                .willReturn(List.of(DUMMY, waiting));

        ReservationResult result = reservationService.find(2L);

        Assertions.assertThat(result.status()).isEqualTo(roomescape.domain.reservation.Status.WAITING);
        Assertions.assertThat(result.getRank().getValue()).isEqualTo(2);
    }
}
