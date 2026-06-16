package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.request.ReservationRequest;
import roomescape.dto.request.ReservationUpdateRequest;
import roomescape.dto.response.ReservationResponse;
import roomescape.exception.AlreadyExistsException;
import roomescape.exception.InvalidStateException;
import roomescape.exception.NotFoundException;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    private Long themeId;
    private Long timeId;

    @BeforeEach
    void setUp() {
        Theme theme = themeRepository.save(new Theme("테스트 테마", "설명", "/test"));
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(9, 0)));
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
        ReservationTime anotherTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(21, 0)));
        ReservationRequest firstRequest = new ReservationRequest("그해", LocalDate.now().plusDays(1), timeId, themeId);
        ReservationRequest secondRequest = new ReservationRequest("그해", LocalDate.now().plusDays(1),
                anotherTime.getId(), themeId);

        long id = reservationService.save(firstRequest).id();
        reservationService.save(secondRequest);

        ReservationUpdateRequest updateRequest = new ReservationUpdateRequest(LocalDate.now().plusDays(1),
                anotherTime.getId());

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
    void 예약_변경시_승인된_예약이_있을_경우_예약_대기() {
        ReservationTime anotherTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(21, 0)));
        reservationService.save(
                new ReservationRequest("브라운", LocalDate.now().plusDays(1), anotherTime.getId(), themeId));

        long id = reservationService.save(new ReservationRequest("그해", LocalDate.now().plusDays(1), timeId, themeId))
                .id();

        ReservationUpdateRequest updateRequest = new ReservationUpdateRequest(LocalDate.now().plusDays(1),
                anotherTime.getId());
        ReservationResponse updated = reservationService.update(id, updateRequest);

        assertThat(updated.status()).isEqualTo(ReservationStatus.WAITING);
    }

    @Test
    void 예약_삭제_시_대기_1순위_예약_승격() {
        LocalDate date = LocalDate.now().plusDays(1);
        Long firstId = reservationService.save(new ReservationRequest("첫번째", date, timeId, themeId)).id();
        Long secondId = reservationService.save(new ReservationRequest("두번째", date, timeId, themeId)).id();
        Long thirdId = reservationService.save(new ReservationRequest("세번째", date, timeId, themeId)).id();

        reservationService.delete(firstId);

        ReservationResponse second = findReservation(secondId);
        ReservationResponse third = findReservation(thirdId);

        assertThat(second.status()).isEqualTo(ReservationStatus.PENDING_PAYMENT);
        assertThat(third.status()).isEqualTo(ReservationStatus.WAITING);
    }

    private ReservationResponse findReservation(Long id) {
        return reservationService.findAll().stream()
                .filter(it -> it.id().equals(id))
                .findFirst()
                .orElseThrow();
    }
}
