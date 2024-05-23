package roomescape.core.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.core.domain.Member;
import roomescape.core.domain.Reservation;
import roomescape.core.domain.ReservationTime;
import roomescape.core.domain.Theme;
import roomescape.core.dto.waiting.WaitingRequest;
import roomescape.core.dto.waiting.WaitingResponse;
import roomescape.core.repository.MemberRepository;
import roomescape.core.repository.ReservationRepository;
import roomescape.core.repository.ReservationTimeRepository;
import roomescape.core.repository.ThemeRepository;
import roomescape.utils.DatabaseCleaner;
import roomescape.utils.TestFixture;

@ServiceTest
class WaitingServiceTest {
    private static final String TODAY = TestFixture.getTodayDate();
    private static final String TOMORROW = TestFixture.getTomorrowDate();

    @Autowired
    private WaitingService waitingService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private TestFixture testFixture;

    @BeforeEach
    void setUp() {
        databaseCleaner.executeTruncate();
        testFixture.initTestData();
    }

    @Test
    @DisplayName("예약 대기를 생성한다.")
    void create() {
        final WaitingRequest request = new WaitingRequest(1L, TOMORROW, 1L, 1L);

        final WaitingResponse response = waitingService.create(request);

        assertAll(
                () -> assertThat(response.getTheme().getId()).isEqualTo(request.getThemeId()),
                () -> assertThat(response.getDate()).isEqualTo(request.getDate()),
                () -> assertThat(response.getMember().getId()).isEqualTo(request.getMemberId()),
                () -> assertThat(response.getTime().getId()).isEqualTo(request.getTimeId())
        );
    }

    @Test
    @DisplayName("예약 대기를 생성할 때, 이미 해당 날짜, 시간, 테마에 예약한 내역이 있으면 예외가 발생한다.")
    void createWithDuplicatedReservation() {
        final WaitingRequest request = new WaitingRequest(1L, TODAY, 1L, 1L);

        assertThatThrownBy(() -> waitingService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(WaitingService.BOOKED_TIME_WAITING_EXCEPTION_MESSAGE);
    }

    @Test
    @DisplayName("예약 대기를 생성할 때, 이미 해당 날짜, 시간, 테마에 예약 대기한 내역이 있으면 예외가 발생한다.")
    void createWithDuplicatedWaiting() {
        final WaitingRequest request = new WaitingRequest(1L, TOMORROW, 1L, 1L);

        waitingService.create(request);

        assertThatThrownBy(() -> waitingService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(WaitingService.WAITED_TIME_WAITING_EXCEPTION_MESSAGE);
    }

    @Test
    @DisplayName("예약 대기 목록을 조회한다.")
    void findAll() {
        final WaitingRequest request = new WaitingRequest(1L, TOMORROW, 1L, 1L);

        waitingService.create(request);

        assertThat(waitingService.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("예약 대기를 삭제한다.")
    void delete() {
        final WaitingRequest request = new WaitingRequest(1L, TOMORROW, 1L, 1L);
        final WaitingResponse response = waitingService.create(request);

        waitingService.delete(response.getId());

        assertThat(waitingService.findAll()).isEmpty();
    }

    private void saveReservation(final String date) {
        final Member member = memberRepository.findById(1L).orElseThrow();
        final ReservationTime time = reservationTimeRepository.findById(1L).orElseThrow();
        final Theme theme = themeRepository.findById(1L).orElseThrow();

        reservationRepository.save(new Reservation(member, date, time, theme));
    }
}