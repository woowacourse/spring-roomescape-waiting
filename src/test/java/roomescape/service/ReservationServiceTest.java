package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
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
import roomescape.service.reservation.ReservationCreationService;
import roomescape.service.reservation.ReservationService;
import roomescape.service.reservation.ReservationThemeService;
import roomescape.service.reservation.ReservationTimeService;

@SpringBootTest
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@TestPropertySource(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationCreationService reservationCreationService;

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
    @DisplayName("모든 예약 정보를 가져온다.")
    void getAllReservationsTest() {
        //when
        final List<ReservationResponse> expected = reservationService.getAllReservations();

        //then
        assertThat(expected).hasSize(4);
    }

    @Test
    @DisplayName("회원 ID로만 필터링하면 해당 회원의 모든 예약이 조회된다")
    void filterByMemberIdOnly() {
        // when
        List<ReservationResponse> result = reservationService.getFilteredReservations(
                memberId1, null, null, null);

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("존재하지 않는 회원 ID로 필터링하면 빈 결과가 반환된다")
    void filterByNonExistingMemberId() {
        // when
        List<ReservationResponse> result = reservationService.getFilteredReservations(
                999L, null, null, null);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("테마 ID로만 필터링하면 해당 테마의 모든 예약이 조회된다")
    void filterByThemeIdOnly() {
        // when
        List<ReservationResponse> result = reservationService.getFilteredReservations(
                null, themeId1, null, null);

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("존재하지 않는 테마 ID로 필터링하면 빈 결과가 반환된다")
    void filterByNonExistingThemeId() {
        // when
        List<ReservationResponse> result = reservationService.getFilteredReservations(
                null, 999L, null, null);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("시작 날짜만 지정하면 해당 날짜 이후의 예약이 조회된다")
    void filterByDateFromOnly() {
        // when
        List<ReservationResponse> result = reservationService.getFilteredReservations(
                null, null, twoDaysLater, null);

        // then
        assertAll(
                () -> assertThat(result).hasSize(3),
                () -> assertThat(result).allMatch(res -> !res.date().isBefore(twoDaysLater))
        );
    }

    @Test
    @DisplayName("종료 날짜만 지정하면 해당 날짜 이전의 예약이 조회된다")
    void filterByDateToOnly() {
        // when
        List<ReservationResponse> result = reservationService.getFilteredReservations(
                null, null, null, twoDaysLater);

        // then
        assertAll(
                () -> assertThat(result).hasSize(3),
                () -> assertThat(result).allMatch(res -> !res.date().isAfter(twoDaysLater))
        );
    }

    @Test
    @DisplayName("시작 날짜와 종료 날짜를 모두 지정하면 해당 기간 내의 예약이 조회된다")
    void filterByDateRange() {
        // when
        List<ReservationResponse> result = reservationService.getFilteredReservations(
                null, null, tomorrow, twoDaysLater);

        // then
        assertAll(
                () -> assertThat(result).hasSize(3),
                () -> assertThat(result).allMatch(res ->
                        !res.date().isBefore(tomorrow) && !res.date().isAfter(twoDaysLater))
        );
    }

    @Test
    @DisplayName("날짜 범위에 예약이 없으면 빈 결과가 반환된다")
    void filterByEmptyDateRange() {
        // when
        LocalDate futureDate = LocalDate.now().plusDays(10);
        List<ReservationResponse> result = reservationService.getFilteredReservations(
                null, null, futureDate, futureDate.plusDays(1));

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("회원 ID와 테마 ID로 필터링하면 해당 회원의 해당 테마 예약만 조회된다")
    void filterByMemberIdAndThemeId() {
        // when
        List<ReservationResponse> result = reservationService.getFilteredReservations(
                memberId1, themeId1, null, null);

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("회원 ID와 날짜 범위로 필터링하면 해당 회원의 해당 기간 예약만 조회된다")
    void filterByMemberIdAndDateRange() {
        // when
        List<ReservationResponse> result = reservationService.getFilteredReservations(
                memberId2, null, twoDaysLater, threeDaysLater);

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("테마 ID와 날짜 범위로 필터링하면 해당 테마의 해당 기간 예약만 조회된다")
    void filterByThemeIdAndDateRange() {
        // when
        List<ReservationResponse> result = reservationService.getFilteredReservations(
                null, themeId2, twoDaysLater, null);

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("모든 필터 조건을 적용하면 모든 조건을 만족하는 예약만 조회된다")
    void filterByAllConditions() {
        // when
        List<ReservationResponse> result = reservationService.getFilteredReservations(
                memberId2, themeId2, threeDaysLater, threeDaysLater);

        // then
        assertAll(
                () -> assertThat(result).hasSize(1),
                () -> assertThat(result.getFirst().date()).isEqualTo(threeDaysLater)
        );
    }

    @Test
    @DisplayName("모든 파라미터가 null이면, 모든 예약이 조회된다")
    void filterWithAllNullParameters() {
        // when
        List<ReservationResponse> result = reservationService.getFilteredReservations(
                null, null, null, null);

        // then
        assertThat(result).hasSize(4);
    }

    @Test
    @DisplayName("조건에 맞는 예약이 없는 경우 빈 목록이 반환된다")
    void returnsEmptyListWhenNoReservationsMatch() {
        // when
        List<ReservationResponse> result = reservationService.getFilteredReservations(
                memberId1, themeId1, threeDaysLater, null);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("대기 예약 삭제 시 예약만 삭제된다.")
    void deletePendingReservationTest() {
        // given
        CreateReservationRequest acceptedRequest = new CreateReservationRequest(
                memberId1, LocalDate.now().plusDays(5), themeId1, timeId
        );
        CreateReservationRequest pendingRequest = new CreateReservationRequest(
                memberId2, LocalDate.now().plusDays(5), themeId1, timeId
        );

        ReservationResponse acceptedReservation = reservationCreationService.addReservation(acceptedRequest);
        ReservationResponse pendingReservation = reservationCreationService.addPendingReservation(pendingRequest);

        int initialReservationCount = reservationService.getAllReservations().size();

        // when
        reservationService.removeReservation(pendingReservation.id());

        // then
        List<ReservationResponse> remainingReservations = reservationService.getAllReservations();
        assertThat(remainingReservations).hasSize(initialReservationCount - 1);
        assertThat(remainingReservations)
                .noneMatch(reservation -> reservation.id() == pendingReservation.id());
        assertThat(remainingReservations)
                .anyMatch(reservation -> reservation.id() == acceptedReservation.id());
    }

    @Test
    @DisplayName("확정 예약 삭제 시 다음 대기가 있으면 예약 항목은 삭제되지 않고 다음 예약이 확정상태가 된다.")
    void deleteAcceptedReservationAndHasPendingReservationTest() {
        // given
        CreateReservationRequest acceptedRequest = new CreateReservationRequest(
                memberId1, LocalDate.now().plusDays(6), themeId1, timeId
        );
        CreateReservationRequest pendingRequest = new CreateReservationRequest(
                memberId2, LocalDate.now().plusDays(6), themeId1, timeId
        );

        ReservationResponse acceptedReservation = reservationCreationService.addReservation(acceptedRequest);
        ReservationResponse pendingReservation = reservationCreationService.addPendingReservation(pendingRequest);

        int initialReservationCount = reservationService.getAllReservations().size();

        // when
        reservationService.removeReservation(acceptedReservation.id());

        // then
        List<ReservationResponse> remainingReservations = reservationService.getAllReservations();
        assertThat(remainingReservations).hasSize(initialReservationCount - 1);

        assertThat(remainingReservations)
                .noneMatch(reservation -> reservation.id() == acceptedReservation.id());

        assertThat(remainingReservations)
                .anyMatch(reservation ->
                        reservation.id() == pendingReservation.id() &&
                                reservation.status().equals(ReservationStatus.ACCEPTED.description)
                );
    }

    @Test
    @DisplayName("확정 예약 삭제 시 다음 대기가 없으면 예약 항목과 예약 모두 삭제된다.")
    void deleteAcceptedReservationAndNoPendingReservationTest() {
        // given
        CreateReservationRequest acceptedRequest = new CreateReservationRequest(
                memberId1, LocalDate.now().plusDays(7), themeId1, timeId
        );

        ReservationResponse acceptedReservation = reservationCreationService.addReservation(acceptedRequest);
        int initialReservationCount = reservationService.getAllReservations().size();

        // when
        reservationService.removeReservation(acceptedReservation.id());

        // then
        List<ReservationResponse> remainingReservations = reservationService.getAllReservations();
        assertThat(remainingReservations).hasSize(initialReservationCount - 1);

        assertThat(remainingReservations)
                .noneMatch(reservation -> reservation.id() == acceptedReservation.id());

        CreateReservationRequest newRequest = new CreateReservationRequest(
                memberId2, LocalDate.now().plusDays(7), themeId1, timeId
        );
        ReservationResponse newReservation = reservationCreationService.addReservation(newRequest);
        assertThat(newReservation.status()).isEqualTo(ReservationStatus.ACCEPTED.description);
    }

    @Test
    @DisplayName("확정 예약 삭제 후 다음 대기 예약이 확정되면 priority가 0으로 변경된다.")
    void priorityChangesAfterReservationDeletion() {
        // given
        LocalDate testDate = LocalDate.now().plusDays(11);

        ReservationResponse acceptedReservation = reservationCreationService.addReservation(
                new CreateReservationRequest(memberId1, testDate, themeId1, timeId)
        );

        ReservationResponse pendingReservation = reservationCreationService.addPendingReservation(
                new CreateReservationRequest(memberId2, testDate, themeId1, timeId)
        );

        List<MyPageReservationResponse> beforeDelete = reservationService.getReservationsByMemberId(memberId2);
        assertThat(beforeDelete).anyMatch(
                reservation ->
                        reservation.reservationId().equals(pendingReservation.id()) && reservation.priority() == 1
                );

        // when
        reservationService.removeReservation(acceptedReservation.id());

        // then
        List<MyPageReservationResponse> afterDeletion =
                reservationService.getReservationsByMemberId(memberId2);
        assertThat(afterDeletion).anyMatch(reservation ->
                reservation.reservationId().equals(pendingReservation.id()) &&
                        reservation.priority() == 0 &&
                        reservation.status().equals(ReservationStatus.ACCEPTED.description)
        );
    }

    @Test
    @DisplayName("여러 대기 예약 중 하나가 삭제되면 뒤의 예약들의 priority가 앞당겨진다.")
    void priorityUpdatesAfterMiddlePendingReservationDeletion() {
        // given
        LocalDate testDate = LocalDate.now().plusDays(12);

        reservationCreationService.addReservation(new CreateReservationRequest(memberId1, testDate, themeId1, timeId));

        ReservationResponse pending1Reservation = reservationCreationService.addPendingReservation(
                new CreateReservationRequest(memberId2, testDate, themeId1, timeId)
        );
        ReservationResponse pending2Reservation = reservationCreationService.addPendingReservation(
                new CreateReservationRequest(memberId3, testDate, themeId1, timeId)
        );
        ReservationResponse pending3Reservation = reservationCreationService.addPendingReservation(
                new CreateReservationRequest(memberId4, testDate, themeId1, timeId)
        );

        // when
        reservationService.removeReservation(pending1Reservation.id());

        // then
        List<MyPageReservationResponse> member1Reservations =
                reservationService.getReservationsByMemberId(memberId3);
        List<MyPageReservationResponse> member2Reservations =
                reservationService.getReservationsByMemberId(memberId4);

        assertThat(member1Reservations).anyMatch(reservation ->
                        reservation.reservationId().equals(pending2Reservation.id()) && reservation.priority() == 1
                );
        assertThat(member2Reservations).anyMatch(reservation ->
                        reservation.reservationId().equals(pending3Reservation.id()) && reservation.priority() == 2
                );
    }
}
