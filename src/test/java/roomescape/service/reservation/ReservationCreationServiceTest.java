package roomescape.service.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.dto.request.CreateReservationRequest;
import roomescape.dto.request.MemberRegisterRequest;
import roomescape.dto.request.ReservationThemeRequest;
import roomescape.dto.request.ReservationTimeRequest;
import roomescape.dto.response.MyPageReservationResponse;
import roomescape.dto.response.ReservationResponse;
import roomescape.service.member.MemberService;

@SpringBootTest
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@TestPropertySource(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ReservationCreationServiceTest {

    @Autowired
    private ReservationCreationService reservationCreationService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Autowired
    private ReservationThemeService reservationThemeService;

    private Long memberId1;
    private Long memberId2;
    private Long memberId3;
    private Long memberId4;
    private Long timeId;
    private Long themeId1;
    private Long themeId2;
    private LocalDate tomorrow;
    private LocalDate twoDaysLater;
    private LocalDate threeDaysLater;

    @BeforeEach
    void setUp() {
        memberId1 = memberService.addMember(new MemberRegisterRequest("user1@example.com", "password1", "User 1")).id();
        memberId2 = memberService.addMember(new MemberRegisterRequest("user2@example.com", "password2", "User 2")).id();
        memberId3 = memberService.addMember(new MemberRegisterRequest("user3@example.com", "password3", "User 3")).id();
        memberId4 = memberService.addMember(new MemberRegisterRequest("user4@example.com", "password4", "User 4")).id();

        timeId = reservationTimeService.addReservationTime(new ReservationTimeRequest(LocalTime.now())).id();

        themeId1 = reservationThemeService.addReservationTheme(new ReservationThemeRequest("Theme 1", "Description 1", "thumbnail1")).id();
        themeId2 = reservationThemeService.addReservationTheme(new ReservationThemeRequest("Theme 2", "Description 2", "thumbnail2")).id();

        tomorrow = LocalDate.now().plusDays(1);
        twoDaysLater = LocalDate.now().plusDays(2);
        threeDaysLater = LocalDate.now().plusDays(3);

        reservationCreationService.addReservation(
                new CreateReservationRequest(memberId1, tomorrow, themeId1, timeId)
        );
        reservationCreationService.addReservation(
                new CreateReservationRequest(memberId1, twoDaysLater, themeId2, timeId)
        );
        reservationCreationService.addReservation(
                new CreateReservationRequest(memberId2, twoDaysLater, themeId1, timeId)
        );
        reservationCreationService.addReservation(
                new CreateReservationRequest(memberId2, threeDaysLater, themeId2, timeId)
        );
    }

    @Test
    @DisplayName("사용자의 id를 이용해 예약을 생성한다")
    void createReservationTest() {
        // given
        Long memberId = memberService.addMember(new MemberRegisterRequest("new@example.com", "new-password", "new-name")).id();
        Long timeId = reservationTimeService.addReservationTime(new ReservationTimeRequest(LocalTime.now())).id();
        Long themeId = reservationThemeService.addReservationTheme(new ReservationThemeRequest("new Theme", "new Description", "new Thumbnail")).id();

        final CreateReservationRequest createReservationRequest = new CreateReservationRequest(
                memberId,
                LocalDate.now().plusDays(1),
                themeId,
                timeId
        );

        // when
        ReservationResponse reservationResponse = reservationCreationService.addReservation(createReservationRequest);

        // then
        assertAll(
                () -> assertThat(reservationResponse.id()).isNotNull(),
                () -> assertThat(reservationResponse.date()).isEqualTo(LocalDate.now().plusDays(1)),
                () -> assertThat(reservationResponse.name()).isEqualTo("new-name")
        );
    }

    @Test
    @DisplayName("사용자가 없는 theme id 또는 time id를 이용해 예약을 생성하면 예외가 발생한다")
    void createReservationWithNonExistentIds() {
        // given
        Long nonExistTimeId = 999L;
        Long nonExistThemeId = 999L;

        final CreateReservationRequest createReservationRequest1 = new CreateReservationRequest(
                memberId1,
                LocalDate.now().plusDays(1),
                nonExistThemeId,
                timeId
        );

        final CreateReservationRequest createReservationRequest2 = new CreateReservationRequest(
                memberId1,
                LocalDate.now().plusDays(1),
                themeId1,
                nonExistTimeId
        );

        // when, then
        assertAll(
                () -> assertThatThrownBy(
                        () -> reservationCreationService.addReservation(createReservationRequest1)
                ).isInstanceOf(NoSuchElementException.class),
                () -> assertThatThrownBy(
                        () -> reservationCreationService.addReservation(createReservationRequest2)
                ).isInstanceOf(NoSuchElementException.class)
        );
    }

    @Test
    @DisplayName("미래가 아닌 날짜로 예약 시도 시 예외 발생")
    void createReservationWithInvalidDate() {
        // given
        final CreateReservationRequest createReservationRequest1 = new CreateReservationRequest(
                memberId1,
                LocalDate.now(),
                themeId1,
                timeId
        );

        final CreateReservationRequest createReservationRequest2 = new CreateReservationRequest(
                memberId1,
                LocalDate.now().minusDays(1),
                themeId1,
                timeId
        );

        // when, then
        assertAll(
                () -> assertThatThrownBy(
                        () -> reservationCreationService.addReservation(createReservationRequest1)
                ).isInstanceOf(IllegalArgumentException.class),
                () -> assertThatThrownBy(
                        () -> reservationCreationService.addReservation(createReservationRequest2)
                ).isInstanceOf(IllegalArgumentException.class)
        );
    }

    @Test
    @DisplayName("존재하지 않은 예약 항목이라면 새로운 예약을 확정 상태로 생성한다")
    void saveReservationAccepted() {
        // given
        final CreateReservationRequest createReservationRequest = new CreateReservationRequest(
                memberId1, LocalDate.now().plusDays(10), themeId1, timeId
        );

        // when
        final ReservationResponse reservation = reservationCreationService.addReservation(createReservationRequest);

        // then
        assertThat(reservation.status()).isEqualTo(ReservationStatus.ACCEPTED.description);
    }

    @Test
    @DisplayName("같은 사용자가 같은 예약 항목에 예약을 두번 걸 수 없다")
    void duplicateSameMemberSameReservationItemTest() {
        // when, then
        assertThatThrownBy(() -> reservationCreationService.addReservation(
                new CreateReservationRequest(memberId1, tomorrow, themeId1, timeId)
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 예약된 시간입니다.");
    }

    @Test
    @DisplayName("대기 예약은 기존 예약이 있을 때만 가능하다")
    void pendingReservationRequiresExistingReservation() {
        // given
        final CreateReservationRequest createReservationRequest = new CreateReservationRequest(
                memberId2, LocalDate.now().plusDays(10), themeId2, timeId
        );

        // when, then
        assertThatThrownBy(() -> reservationCreationService.addPendingReservation(createReservationRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("대기 예약은 기존 예약이 있을 때만 가능합니다");
    }

    @Test
    @DisplayName("이미 예약된 시간에 일반 예약을 시도하면 예외가 발생한다")
    void cannotCreateNormalReservationWhenAlreadyExists() {
        // when, then
        assertThatThrownBy(() -> reservationCreationService.addReservation(
                new CreateReservationRequest(memberId2, tomorrow, themeId1, timeId)
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 예약된 시간입니다");
    }

    @Test
    @DisplayName("확정 예약의 priority는 0이다.")
    void acceptedReservationHasPriorityZero() {
        // given
        CreateReservationRequest acceptedRequest = new CreateReservationRequest(
                memberId1, LocalDate.now().plusDays(8), themeId1, timeId
        );

        // when
        ReservationResponse acceptedReservation = reservationCreationService.addReservation(acceptedRequest);
        List<MyPageReservationResponse> myReservations =
                reservationService.getReservationsByMemberId(memberId1);

        // then
        assertThat(myReservations)
                .anyMatch(reservation ->
                        reservation.reservationId().equals(acceptedReservation.id()) &&
                                reservation.priority() == 0
                );
    }


    @Test
    @DisplayName("대기 예약의 priority는 앞선 예약 수에 따라 결정된다.")
    void pendingReservationPriorityTest() {
        // given
        LocalDate testDate = LocalDate.now().plusDays(9);

        ReservationResponse accepted = reservationCreationService.addReservation(
                new CreateReservationRequest(memberId1, testDate, themeId1, timeId)
        );
        ReservationResponse pending = reservationCreationService.addPendingReservation(
                new CreateReservationRequest(memberId2, testDate, themeId1, timeId)
        );
        ReservationResponse pending2 = reservationCreationService.addPendingReservation(
                new CreateReservationRequest(memberId3, testDate, themeId1, timeId)
        );

        // when
        List<MyPageReservationResponse> member1Reservations =
                reservationService.getReservationsByMemberId(memberId1);
        List<MyPageReservationResponse> member2Reservations =
                reservationService.getReservationsByMemberId(memberId2);
        List<MyPageReservationResponse> member3Reservations =
                reservationService.getReservationsByMemberId(memberId3);

        // then
        assertThat(member1Reservations)
                .anyMatch(reservation ->
                        reservation.reservationId().equals(accepted.id()) &&
                                reservation.priority() == 0
                );
        assertThat(member2Reservations)
                .anyMatch(reservation ->
                        reservation.reservationId().equals(pending.id()) &&
                                reservation.priority() == 1
                );
        assertThat(member3Reservations)
                .anyMatch(reservation ->
                        reservation.reservationId().equals(pending2.id()) &&
                                reservation.priority() == 2
                );
    }

    @Test
    @DisplayName("서로 다른 예약 아이템의 예약들은 독립적으로 priority가 계산된다.")
    void differentReservationItemsHaveIndependentPriorities() {
        // given
        LocalDate testDate = LocalDate.now().plusDays(10);

        ReservationResponse acceptedTheme1Reservation = reservationCreationService.addReservation(
                new CreateReservationRequest(memberId1, testDate, themeId1, timeId)
        );
        ReservationResponse acceptedTheme2Reservation = reservationCreationService.addReservation(
                new CreateReservationRequest(memberId1, testDate, themeId2, timeId)
        );
        ReservationResponse pendingTheme1Reservation = reservationCreationService.addPendingReservation(
                new CreateReservationRequest(memberId2, testDate, themeId1, timeId)
        );
        ReservationResponse pendingTheme2Reservation = reservationCreationService.addPendingReservation(
                new CreateReservationRequest(memberId2, testDate, themeId2, timeId)
        );

        // when
        List<MyPageReservationResponse> member1Reservations =
                reservationService.getReservationsByMemberId(memberId1);
        List<MyPageReservationResponse> member2Reservations =
                reservationService.getReservationsByMemberId(memberId2);

        // then
        assertThat(member1Reservations)
                .filteredOn(reservation ->
                        reservation.reservationId().equals(acceptedTheme1Reservation.id()) ||
                                reservation.reservationId().equals(acceptedTheme2Reservation.id())
                )
                .allMatch(reservation -> reservation.priority() == 0);

        assertThat(member2Reservations)
                .filteredOn(reservation ->
                        reservation.reservationId().equals(pendingTheme1Reservation.id()) ||
                                reservation.reservationId().equals(pendingTheme2Reservation.id())
                )
                .allMatch(reservation -> reservation.priority() == 1);
    }
}
