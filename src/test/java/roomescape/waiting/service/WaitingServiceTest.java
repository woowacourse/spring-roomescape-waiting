package roomescape.waiting.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.fixture.MemberFixture.MEMBER;
import static roomescape.fixture.ThemeFixture.THEME;
import static roomescape.fixture.TimeFixture.TIME;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.fake.FakeMemberDao;
import roomescape.fake.FakeReservationDao;
import roomescape.fake.FakeReservationTimeDao;
import roomescape.fake.FakeThemeDao;
import roomescape.fake.FakeWaitingDao;
import roomescape.global.exception.custom.BadRequestException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.waiting.domain.Waiting;
import roomescape.reservation.waiting.dto.CreateWaitingRequest;
import roomescape.reservation.waiting.dto.WaitingResponse;
import roomescape.reservation.waiting.service.WaitingService;

class WaitingServiceTest {

    private static final LocalDate TOMORROW = LocalDate.now().plusDays(1);

    private final FakeReservationTimeDao fakeReservationTimeDao = new FakeReservationTimeDao();
    private final FakeReservationDao fakeReservationDao = new FakeReservationDao();
    private final FakeThemeDao fakeThemeDao = new FakeThemeDao();
    private final FakeMemberDao fakeMemberDao = new FakeMemberDao();
    private final FakeWaitingDao fakeWaitingDao = new FakeWaitingDao();
    private final WaitingService waitingService = new WaitingService(fakeWaitingDao, fakeReservationTimeDao,
            fakeThemeDao, fakeMemberDao, fakeReservationDao);

    @BeforeEach
    void setUp() {
        fakeReservationTimeDao.save(TIME);
        fakeThemeDao.save(THEME);
        fakeMemberDao.save(MEMBER);

        Reservation reservation1 = new Reservation(1L, MEMBER, TOMORROW, THEME, TIME);
        Reservation reservation2 = new Reservation(2L, MEMBER, TOMORROW.plusDays(1), THEME, TIME);
        fakeReservationDao.save(reservation1);
        fakeReservationDao.save(reservation2);

        CreateWaitingRequest request1 = new CreateWaitingRequest(
                TOMORROW, TIME.getId(), THEME.getId(), MEMBER.getId());
        CreateWaitingRequest request2 = new CreateWaitingRequest(
                TOMORROW.plusDays(1), TIME.getId(), THEME.getId(), MEMBER.getId());
        waitingService.createWaiting(request1);
        waitingService.createWaiting(request2);
    }

    @DisplayName("예약 대기를 생성할 수 있다.")
    @Test
    void testCreate() {
        CreateWaitingRequest request = new CreateWaitingRequest(TOMORROW, TIME.getId(), THEME.getId(), MEMBER.getId());
        WaitingResponse response = waitingService.createWaiting(request);
        Waiting savedWaiting = fakeWaitingDao.findById(response.id()).orElseThrow();

        assertThat(response.id()).isEqualTo(3L);
        assertMember(response, request, savedWaiting);
        assertDateAndTime(response, request, savedWaiting);
        assertTheme(response, request, savedWaiting);
    }

    @DisplayName("예약 대기 목록을 조회할 수 있다.")
    @Test
    void testGetAllWaitings() {
        // given
        // when
        List<WaitingResponse> waitings = waitingService.getAllWaitings();
        // then
        assertThat(waitings).hasSize(2);
    }

    @DisplayName("예약 대기를 삭제할 수 있다.")
    @Test
    void testCancelWaiting() {
        // given
        // when
        waitingService.cancelWaiting(1L);
        // then
        assertThat(waitingService.getAllWaitings()).hasSize(1);
    }

    private void assertMember(WaitingResponse response, CreateWaitingRequest request, Waiting savedWaiting) {
        assertAll("Member 정보 일치",
                () -> assertThat(response.member().id()).isEqualTo(request.memberId()),
                () -> assertThat(response.member().name()).isEqualTo(MEMBER.getName().getValue()),
                () -> assertThat(response.member().email()).isEqualTo(MEMBER.getEmail().getValue()),

                () -> assertThat(savedWaiting.getMember().getId()).isEqualTo(request.memberId()),
                () -> assertThat(savedWaiting.getMember().getName().getValue()).isEqualTo(MEMBER.getName().getValue()),
                () -> assertThat(savedWaiting.getMember().getEmail().getValue()).isEqualTo(MEMBER.getEmail().getValue())
        );
    }

    private void assertDateAndTime(WaitingResponse response, CreateWaitingRequest request, Waiting savedWaiting) {
        assertAll("날짜와 시간 정보 일치",
                () -> assertThat(response.date()).isEqualTo(request.date()),
                () -> assertThat(response.time().id()).isEqualTo(request.timeId()),
                () -> assertThat(response.time().startAt()).isEqualTo(TIME.getStartAt()),

                () -> assertThat(savedWaiting.getDate()).isEqualTo(request.date()),
                () -> assertThat(savedWaiting.getTime().getId()).isEqualTo(request.timeId()),
                () -> assertThat(savedWaiting.getTime().getStartAt()).isEqualTo(TIME.getStartAt())
        );
    }

    private void assertTheme(WaitingResponse response, CreateWaitingRequest request, Waiting savedWaiting) {
        assertAll("테마 정보 일치",
                () -> assertThat(response.theme().id()).isEqualTo(request.themeId()),
                () -> assertThat(response.theme().name()).isEqualTo(THEME.getName().getValue()),
                () -> assertThat(response.theme().description()).isEqualTo(THEME.getDescription().getValue()),
                () -> assertThat(response.theme().thumbnail()).isEqualTo(THEME.getThumbnail().getValue()),

                () -> assertThat(savedWaiting.getTheme().getId()).isEqualTo(request.themeId()),
                () -> assertThat(savedWaiting.getTheme().getName().getValue()).isEqualTo(THEME.getName().getValue()),
                () -> assertThat(savedWaiting.getTheme().getDescription()).isEqualTo(THEME.getDescription()),
                () -> assertThat(savedWaiting.getTheme().getThumbnail()).isEqualTo(THEME.getThumbnail())
        );
    }

    @DisplayName("예약 대기 승인 테스트")
    @Nested
    class ApproveWaitingTest {

        @DisplayName("예약 대기를 승인할 수 있다.")
        @Test
        void testApprove() {
            // given
            // when
            fakeReservationDao.deleteById(1L);
            ReservationResponse response = waitingService.approveWaiting(1L);
            // then
            assertAll("응답 검증",
                    () -> assertThat(response.date()).isEqualTo(TOMORROW),
                    () -> assertThat(response.member().id()).isEqualTo(MEMBER.getId()),
                    () -> assertThat(response.time().id()).isEqualTo(TIME.getId()),
                    () -> assertThat(response.theme().id()).isEqualTo(THEME.getId())
            );
            assertThat(waitingService.getAllWaitings()).hasSize(1);
        }

        @DisplayName("예약 대기가 존재하지 않으면 예외가 발생한다.")
        @Test
        void shouldThrowException_WhenWaitingNotExist() {
            // given
            // when
            // then
            assertThatThrownBy(() -> {
                waitingService.approveWaiting(3L);
            }).isInstanceOf(BadRequestException.class);
        }

        @DisplayName("해당 시간에 예약이 존재하면 예외가 발생한다.")
        @Test
        void shouldThrowException_WhenReservationExist() {
            // given
            // when
            // then
            assertThatThrownBy(() -> {
                waitingService.approveWaiting(1L);
            }).isInstanceOf(BadRequestException.class);
        }
    }
}
