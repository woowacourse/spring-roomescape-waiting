package roomescape.reservation.service;

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
import roomescape.global.auth.LoginMember;
import roomescape.global.exception.custom.BadRequestException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.CreateReservationRequest;
import roomescape.reservation.dto.MyReservationResponse;
import roomescape.reservation.dto.ReservationResponse;

class ReservationServiceTest {

    private static final LocalDate TOMORROW = LocalDate.now().plusDays(1);

    private final FakeReservationTimeDao fakeReservationTimeDao = new FakeReservationTimeDao();
    private final FakeReservationDao fakeReservationDao = new FakeReservationDao();
    private final FakeThemeDao fakeThemeDao = new FakeThemeDao();
    private final FakeMemberDao fakeMemberDao = new FakeMemberDao();
    private final FakeWaitingDao fakeWaitingDao = new FakeWaitingDao();
    private final ReservationService reservationService = new ReservationService(fakeReservationDao,
            fakeReservationTimeDao,
            fakeThemeDao, fakeMemberDao, fakeWaitingDao);

    @BeforeEach
    void setUp() {
        fakeReservationTimeDao.save(TIME);
        fakeThemeDao.save(THEME);
        fakeMemberDao.save(MEMBER);
        CreateReservationRequest request1 = new CreateReservationRequest(
                TOMORROW, TIME.getId(), THEME.getId(), MEMBER.getId());
        CreateReservationRequest request2 = new CreateReservationRequest(TOMORROW.plusDays(1),
                TIME.getId(), THEME.getId(), MEMBER.getId());
        reservationService.createReservation(request1);
        reservationService.createReservation(request2);
    }

    @DisplayName("예약 목록을 조회할 수 있다.")
    @Test
    void testFindAll() {
        // given
        // when
        List<ReservationResponse> reservations = reservationService.getAllReservations();
        // then
        assertThat(reservations).hasSize(2);
    }

    @DisplayName("예약을 삭제할 수 있다.")
    @Test
    void testCancelById() {
        // given
        // when
        reservationService.cancelReservationById(1L);
        // then
        assertThat(reservationService.getAllReservations()).hasSize(1);
    }

    @DisplayName("나의 예약 목록을 조회할 수 있다.")
    @Test
    void testGetMyReservations() {
        // given
        LoginMember loginMember = new LoginMember(MEMBER.getId(), MEMBER.getName().getValue(),
                MEMBER.getEmail().getValue(),
                MEMBER.getRole().name());
        // when
        List<MyReservationResponse> myReservations = reservationService.getMyReservations(loginMember);
        // then
        assertThat(myReservations).hasSize(2);
    }

    @DisplayName("예약 생성 테스트")
    @Nested
    class CreateReservationTest {

        private static final CreateReservationRequest REQUEST = new CreateReservationRequest(
                TOMORROW.plusDays(2),
                TIME.getId(),
                THEME.getId(),
                MEMBER.getId()
        );


        @DisplayName("예약을 생성할 수 있다.")
        @Test
        void testCreate() {
            // when
            ReservationResponse response = reservationService.createReservation(REQUEST);
            // then
            Reservation savedReservation = fakeReservationDao.findById(response.id()).orElseThrow();
            assertThat(response.id()).isEqualTo(3L);
            assertMember(response, savedReservation);
            assertDateAndTime(response, savedReservation);
            assertTheme(response, savedReservation);
        }

        @DisplayName("중복 예약일 경우 예외가 발생한다.")
        @Test
        void testValidateDuplication() {
            // given
            reservationService.createReservation(REQUEST);
            // when
            // then
            assertThatThrownBy(() -> reservationService.createReservation(REQUEST))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("해당 시간에 이미 예약이 존재합니다.");
        }

        @DisplayName("예약 시간이 존재하지 않을 경우 예외가 발생한다.")
        @Test
        void testValidateTime() {
            // given
            CreateReservationRequest request = new CreateReservationRequest(TOMORROW, 2L,
                    THEME.getId(), MEMBER.getId());
            // when
            // then
            assertThatThrownBy(() -> reservationService.createReservation(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("예약 시간이 존재하지 않습니다.");
        }

        @DisplayName("테마가 존재하지 않을 경우 예외가 발생한다.")
        @Test
        void testValidateTheme() {
            // given
            CreateReservationRequest request = new CreateReservationRequest(TOMORROW, TIME.getId(),
                    2L, MEMBER.getId());
            // when
            // then
            assertThatThrownBy(() -> reservationService.createReservation(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("테마가 존재하지 않습니다.");
        }

        @DisplayName("과거 시간에 예약할 경우 예외가 발생한다.")
        @Test
        void testValidatePastTime() {
            // given
            LocalDate yesterday = LocalDate.now().minusDays(1);
            CreateReservationRequest request = new CreateReservationRequest(yesterday, TIME.getId(),
                    THEME.getId(), MEMBER.getId());
            // when
            // then
            assertThatThrownBy(() -> reservationService.createReservation(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("지나간 날짜와 시간은 예약 불가합니다.");
        }

        private void assertMember(ReservationResponse response, Reservation savedReservation) {
            assertAll("Member 정보 일치",
                    () -> assertThat(response.member().id()).isEqualTo(CreateReservationTest.REQUEST.memberId()),
                    () -> assertThat(response.member().name()).isEqualTo(MEMBER.getName().getValue()),
                    () -> assertThat(response.member().email()).isEqualTo(MEMBER.getEmail().getValue()),

                    () -> assertThat(savedReservation.getMember().getId()).isEqualTo(
                            CreateReservationTest.REQUEST.memberId()),
                    () -> assertThat(savedReservation.getMember().getName().getValue()).isEqualTo(
                            MEMBER.getName().getValue()),
                    () -> assertThat(savedReservation.getMember().getEmail().getValue()).isEqualTo(
                            MEMBER.getEmail().getValue())
            );
        }

        private void assertDateAndTime(ReservationResponse response, Reservation savedReservation) {
            assertAll("날짜와 시간 정보 일치",
                    () -> assertThat(response.date()).isEqualTo(CreateReservationTest.REQUEST.date()),
                    () -> assertThat(response.time().id()).isEqualTo(CreateReservationTest.REQUEST.timeId()),
                    () -> assertThat(response.time().startAt()).isEqualTo(TIME.getStartAt()),

                    () -> assertThat(savedReservation.getDate()).isEqualTo(CreateReservationTest.REQUEST.date()),
                    () -> assertThat(savedReservation.getTime().getId()).isEqualTo(
                            CreateReservationTest.REQUEST.timeId()),
                    () -> assertThat(savedReservation.getTime().getStartAt()).isEqualTo(TIME.getStartAt())
            );
        }

        private void assertTheme(ReservationResponse response, Reservation savedReservation) {
            assertAll("테마 정보 일치",
                    () -> assertThat(response.theme().id()).isEqualTo(CreateReservationTest.REQUEST.themeId()),
                    () -> assertThat(response.theme().name()).isEqualTo(THEME.getName().getValue()),
                    () -> assertThat(response.theme().description()).isEqualTo(THEME.getDescription().getValue()),
                    () -> assertThat(response.theme().thumbnail()).isEqualTo(THEME.getThumbnail().getValue()),

                    () -> assertThat(savedReservation.getTheme().getId()).isEqualTo(
                            CreateReservationTest.REQUEST.themeId()),
                    () -> assertThat(savedReservation.getTheme().getName().getValue()).isEqualTo(
                            THEME.getName().getValue()),
                    () -> assertThat(savedReservation.getTheme().getDescription()).isEqualTo(THEME.getDescription()),
                    () -> assertThat(savedReservation.getTheme().getThumbnail()).isEqualTo(THEME.getThumbnail())
            );
        }
    }
}
