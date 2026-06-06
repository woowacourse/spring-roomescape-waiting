package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.ThemeDao;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.request.ReservationRequest;
import roomescape.dto.request.UserReservationUpdateRequest;
import roomescape.dto.response.ReservationRankResponse;
import roomescape.dto.response.ReservationResponse;
import roomescape.exception.AlreadyExistsException;
import roomescape.exception.InvalidStateException;
import roomescape.exception.NotFoundException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ThemeDao themeDao;

    @Autowired
    private ReservationTimeDao reservationTimeDao;

    private Long themeId;
    private Long timeId;

    @BeforeEach
    void setUp() {
        Theme theme = themeDao.save(new Theme("테스트 테마", "설명", "/test"));
        ReservationTime time = reservationTimeDao.save(new ReservationTime(LocalTime.of(9, 0)));
        themeId = theme.getId();
        timeId = time.getId();
    }

    @Test
    void 존재하지_않는_시간_ID로_예약_시_예외() {
        Long invalidTimeId = 0L;
        ReservationRequest request = new ReservationRequest("아나키", LocalDate.now().plusDays(1), invalidTimeId, themeId);

        assertThatThrownBy(() -> reservationService.save(request))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void 존재하지_않는_테마_ID로_예약_시_예외() {
        Long invalidThemeId = 0L;
        ReservationRequest request = new ReservationRequest("아나키", LocalDate.now().plusDays(1), timeId, invalidThemeId);

        assertThatThrownBy(() -> reservationService.save(request))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void 이미_지난_시간_날짜_예약_시_예외() {
        ReservationRequest request = new ReservationRequest("아나키", LocalDate.now().minusDays(1), timeId, themeId);

        assertThatThrownBy(() -> reservationService.save(request))
                .isInstanceOf(InvalidStateException.class)
                .hasMessageContaining("이미 지난 시간/날짜는 예약할 수 없습니다.");
    }

    @Test
    void 중복_없는_정상_예약() {
        ReservationRequest request = new ReservationRequest("아나키", LocalDate.now().plusDays(1), timeId, themeId);

        assertDoesNotThrow(() -> reservationService.save(request));
    }

    @Test
    void 이미_예약된_슬롯에_예약_시_대기_등록() {
        ReservationRequest firstRequest = new ReservationRequest("그해", LocalDate.now().plusDays(1), timeId, themeId);
        reservationService.save(firstRequest);

        ReservationRequest secondRequest = new ReservationRequest("아나키", LocalDate.now().plusDays(1), timeId, themeId);
        ReservationResponse saved = reservationService.save(secondRequest);

        assertThat(saved.status()).isEqualTo(ReservationStatus.WAITING);
    }

    @Test
    void 예약_변경_시_이미_예약이_존재하면_대기_불가() {
        ReservationTime anotherTime = reservationTimeDao.save(new ReservationTime(LocalTime.of(21, 0)));
        ReservationRequest firstRequest = new ReservationRequest("그해", LocalDate.now().plusDays(1), timeId, themeId);
        ReservationRequest secondRequest = new ReservationRequest("그해", LocalDate.now().plusDays(1), anotherTime.getId(), themeId);

        long id = reservationService.save(firstRequest).id();
        reservationService.save(secondRequest);

        UserReservationUpdateRequest updateRequest = new UserReservationUpdateRequest(LocalDate.now().plusDays(1), anotherTime.getId());

        assertThatThrownBy(() -> reservationService.update(id, updateRequest))
                .isInstanceOf(AlreadyExistsException.class);
    }

    @Test
    void 같은_사용자_중복_예약_및_대기_불가() {
        ReservationRequest firstRequest = new ReservationRequest("그해", LocalDate.now().plusDays(1), timeId, themeId);
        reservationService.save(firstRequest);

        ReservationRequest secondRequest = new ReservationRequest("그해", LocalDate.now().plusDays(1), timeId, themeId);

        assertThatThrownBy(() -> reservationService.save(secondRequest))
                .isInstanceOf(AlreadyExistsException.class);
    }

    @Test
    void 예약_삭제_시_대기_순번_승인_확인() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        ReservationRequest firstRequest = new ReservationRequest("브라운", futureDate, timeId, themeId);
        ReservationRequest secondRequest = new ReservationRequest("테스트유저", futureDate, timeId, themeId);

        long firstId = reservationService.save(firstRequest).id();
        long secondId = reservationService.save(secondRequest).id();

        reservationService.delete(firstId);

        List<ReservationRankResponse> reservations = reservationService.find("테스트유저");
        assertThat(reservations.getFirst().status()).isEqualTo(ReservationStatus.CONFIRMED);
    }
}
