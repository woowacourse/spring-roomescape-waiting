package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.request.ReservationRequest;
import roomescape.dto.request.UserReservationUpdateRequest;
import roomescape.dto.response.ReservationOrderResponse;
import roomescape.exception.DuplicateReservationException;
import roomescape.exception.IdNotFoundException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    private static final LocalDate FUTURE_DATE = LocalDate.now().plusDays(7);

    @Mock
    private ReservationRepository reservationDao;

    @Mock
    private ReservationTimeRepository reservationTimeDao;

    @Mock
    private ThemeRepository themeDao;

    @InjectMocks
    private ReservationService reservationService;

    private ReservationTime time() {
        return new ReservationTime(1L, LocalTime.of(10, 0));
    }

    private Theme theme() {
        return new Theme(1L, "테마", "설명", "/url");
    }

    @Test
    void 존재하지_않는_시간_ID로_예약_시_예외() {
        given(reservationTimeDao.findTimeById(999L)).willThrow(new EmptyResultDataAccessException(1));
        ReservationRequest request = new ReservationRequest("아나키", FUTURE_DATE, 999L, 1L);

        assertThatThrownBy(() -> reservationService.save(request, LocalDateTime.now()))
                .isInstanceOf(IdNotFoundException.class)
                .hasMessageContaining("요청하신 시간 정보를 찾을 수 없습니다.");
    }

    @Test
    void 존재하지_않는_테마_ID로_예약_시_예외() {
        given(reservationTimeDao.findTimeById(1L)).willReturn(time());
        given(themeDao.findThemeById(999L)).willThrow(new EmptyResultDataAccessException(1));
        ReservationRequest request = new ReservationRequest("아나키", FUTURE_DATE, 1L, 999L);

        assertThatThrownBy(() -> reservationService.save(request, LocalDateTime.now()))
                .isInstanceOf(IdNotFoundException.class)
                .hasMessageContaining("요청하신 테마를 찾을 수 없습니다.");
    }

    @Test
    void 이미_지난_시간_날짜_예약_시_예외() {
        given(reservationTimeDao.findTimeById(1L)).willReturn(time());
        given(themeDao.findThemeById(1L)).willReturn(theme());
        ReservationRequest request = new ReservationRequest("아나키", LocalDate.now().minusDays(1), 1L, 1L);

        assertThatThrownBy(() -> reservationService.save(request, LocalDateTime.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 지난 시간/날짜는 예약할 수 없습니다.");
    }

    @Test
    void 중복_없는_정상_예약() {
        given(reservationTimeDao.findTimeById(1L)).willReturn(time());
        given(themeDao.findThemeById(1L)).willReturn(theme());
        given(reservationDao.save(any())).willReturn(
                new Reservation(1L, "아나키", FUTURE_DATE, time(), theme(), LocalDateTime.now()));
        ReservationRequest request = new ReservationRequest("아나키", FUTURE_DATE, 1L, 1L);

        assertDoesNotThrow(() -> reservationService.save(request, LocalDateTime.now()));
    }

    @Test
    void 같은_사용자_중복_예약_시_도메인_예외로_변환() {
        given(reservationTimeDao.findTimeById(1L)).willReturn(time());
        given(themeDao.findThemeById(1L)).willReturn(theme());
        given(reservationDao.save(any())).willThrow(new DuplicateKeyException("duplicate"));
        ReservationRequest request = new ReservationRequest("그해", FUTURE_DATE, 1L, 1L);

        assertThatThrownBy(() -> reservationService.save(request, LocalDateTime.now()))
                .isInstanceOf(DuplicateReservationException.class);
    }

    @Test
    void 대기_순번은_같은_슬롯에서_더_빨리_신청한_수로_계산된다() {
        ReservationTime time = time();
        Theme theme = theme();
        LocalDateTime base = LocalDateTime.now();
        Reservation earlier = new Reservation(1L, "브라운", FUTURE_DATE, time, theme, base);
        Reservation mine = new Reservation(2L, "아나키", FUTURE_DATE, time, theme, base.plusSeconds(1));
        given(reservationDao.findByName("아나키")).willReturn(List.of(mine));
        given(reservationDao.findBySlot(FUTURE_DATE, 1L, 1L)).willReturn(List.of(earlier, mine));

        List<ReservationOrderResponse> result = reservationService.findByName("아나키");

        assertThat(result.getFirst().order()).isEqualTo(1);
    }

    @Test
    void 예약_변경_시_이미_예약이_존재하면_변경_불가() {
        given(reservationTimeDao.findTimeById(1L)).willReturn(time());
        given(themeDao.findThemeById(1L)).willReturn(theme());
        given(reservationDao.existsBy(any(), any(), any())).willReturn(true);
        UserReservationUpdateRequest request = new UserReservationUpdateRequest(FUTURE_DATE, 1L, 1L);

        assertThatThrownBy(() -> reservationService.update(1L, request))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
