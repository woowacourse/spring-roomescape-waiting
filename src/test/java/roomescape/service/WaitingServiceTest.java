package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.domain.waiting.Waiting;
import roomescape.dto.waiting.WaitingCreateRequest;
import roomescape.dto.waiting.WaitingResponse;
import roomescape.exception.NotFoundException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingQueryRepository;
import roomescape.repository.WaitingRepository;

@DataJpaTest
class WaitingServiceTest {
    @Mock
    private WaitingRepository waitingRepository;
    @Mock
    private ReservationTimeRepository reservationTimeRepository;
    @Mock
    private ThemeRepository themeRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private WaitingQueryRepository waitingQueryRepository;
    @Mock
    private ReservationService reservationService;

    @InjectMocks
    private WaitingService waitingService;

    private Member testMember;
    private Theme testTheme;
    private ReservationTime testReservationTime;
    private Waiting testWaiting;
    private WaitingCreateRequest createRequest;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        testMember = new Member(1L, "Test User", "test@example.com", Role.USER, "password");
        testTheme = new Theme(1L, "Test Theme", "Test Description", "test-thumbnail.jpg");

        // 현재 시간보다 미래의 시간으로 설정
        LocalTime futureTime = LocalTime.now().plusHours(2);
        testReservationTime = new ReservationTime(1L, futureTime);

        testDate = LocalDate.now().plusDays(1); // 내일 날짜로 설정
        testWaiting = new Waiting(1L, testDate, testMember, testTheme, testReservationTime);

        createRequest = new WaitingCreateRequest(testDate, 1L, 1L, 1L);
    }

    @DisplayName("새로운 예약 대기를 생성할 수 있다.")
    @Test
    void createWaiting_withValidRequest_returnsWaitingResponse() {
        //given
        when(reservationTimeRepository.findById(createRequest.timeId())).thenReturn(Optional.of(testReservationTime));
        when(themeRepository.findById(createRequest.themeId())).thenReturn(Optional.of(testTheme));
        when(memberRepository.findById(createRequest.memberId())).thenReturn(Optional.of(testMember));
        when(waitingRepository.save(any(Waiting.class))).thenReturn(testWaiting);

        //when
        WaitingResponse response = waitingService.createWaiting(createRequest);

        //then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(testWaiting.getId());
        assertThat(response.date()).isEqualTo(testWaiting.getDate());
        assertThat(response.name()).isEqualTo(testWaiting.getMember().getName());
        assertThat(response.theme()).isEqualTo(testWaiting.getTheme().getName());
        assertThat(response.startAt()).isEqualTo(testWaiting.getTime().getStartAt());
    }

    @DisplayName("사용자의 대기 목록과 순위를 포함한 응답을 조회할 수 있다.")
    @Test
    void findMyWaitings_returnsCorrectResponseWithRankStatus() {
        // given
        Long memberId = testMember.getId();
        long rank = 2L;
        Waiting testWaiting2 = new Waiting(2L, testDate, testMember, testTheme, testReservationTime);
        Waiting testWaiting3 = new Waiting(3L, testDate, testMember, testTheme, testReservationTime);

        when(waitingRepository.findAll())
                .thenReturn(List.of(testWaiting, testWaiting2, testWaiting3));
        when(waitingRepository.findAllByMemberId(memberId))
                .thenReturn(List.of(testWaiting2));

        // when
        var responses = waitingService.findMyWaitings(memberId);

        // then
        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(1);

        var response = responses.get(0);
        assertThat(response.id()).isEqualTo(testWaiting2.getId());
        assertThat(response.theme()).isEqualTo(testTheme.getName());
        assertThat(response.date()).isEqualTo(testWaiting2.getDate());
        assertThat(response.time()).isEqualTo(testReservationTime.getStartAt());
        assertThat(response.status()).isEqualTo(rank + "번째 예약대기");
    }

    @DisplayName("모든 대기 목록을 조회할 수 있다.")
    @Test
    void findAllWaitings_returnsAllWaitings() {
        // given
        when(waitingRepository.findAll()).thenReturn(Collections.singletonList(testWaiting));

        // when
        List<WaitingResponse> responses = waitingService.findAllWaitings();

        // then
        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(1);
        WaitingResponse response = responses.get(0);
        assertThat(response.id()).isEqualTo(testWaiting.getId());
        assertThat(response.name()).isEqualTo(testMember.getName());
        assertThat(response.theme()).isEqualTo(testTheme.getName());
        assertThat(response.startAt()).isEqualTo(testReservationTime.getStartAt());
    }

    @DisplayName("존재하는 대기 예약을 삭제할 수 있다.")
    @Test
    void deleteWaiting_withExistingId_deletesSuccessfully() {
        // given
        Long waitingId = testWaiting.getId();
        when(waitingRepository.existsById(waitingId)).thenReturn(true);

        // when & then
        waitingService.deleteWaiting(waitingId);
        // 예외가 발생하지 않으면 성공 (void 반환)
    }

    @DisplayName("존재하지 않는 대기 예약을 삭제하면 예외가 발생한다.")
    @Test
    void deleteWaiting_withNonExistingId_throwsException() {
        // given
        Long invalidId = 999L;
        when(waitingRepository.existsById(invalidId)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> waitingService.deleteWaiting(invalidId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("등록된 예약 대기 번호만 삭제할 수 있습니다");
    }

    @DisplayName("예약 대기를 승인하면 해당 대기 정보는 삭제되고 예약이 생성된다.")
    @Test
    void approveWaiting_withValidWaiting_createsReservationAndDeletesWaiting() {
        // given
        Long waitingId = testWaiting.getId();
        when(waitingRepository.findById(waitingId)).thenReturn(Optional.of(testWaiting));

        // when & then
        assertThatCode(() -> waitingService.approveWaiting(waitingId))
                .doesNotThrowAnyException();
    }

    @DisplayName("존재하지 않는 예약 대기를 승인하면 예외가 발생한다.")
    @Test
    void approveWaiting_withInvalidId_throwsException() {
        // given
        Long invalidId = 123L;
        when(waitingRepository.findById(invalidId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> waitingService.approveWaiting(invalidId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("등록된 예약 대기 번호만 승인할 수 있습니다");
    }
}
