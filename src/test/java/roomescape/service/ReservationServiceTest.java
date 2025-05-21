package roomescape.service;

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
import roomescape.dto.request.MemberRegisterRequest;
import roomescape.dto.request.ReservationRequest;
import roomescape.dto.request.ReservationThemeRequest;
import roomescape.dto.request.ReservationTimeRequest;
import roomescape.dto.response.ReservationResponse;

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
    private MemberService memberService;

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Autowired
    private ReservationThemeService reservationThemeService;

    private Long memberId1;
    private Long memberId2;
    private Long timeId;
    private Long themeId1;
    private Long themeId2;
    private LocalDate tomorrow;
    private LocalDate twoDaysLater;
    private LocalDate threeDaysLater;

    @BeforeEach
    void setUp() {
        memberId1 = memberService.addMember(new MemberRegisterRequest("user1@example.com", "password1", "User One")).id();
        memberId2 = memberService.addMember(new MemberRegisterRequest("user2@example.com", "password2", "User Two")).id();

        timeId = reservationTimeService.addReservationTime(new ReservationTimeRequest(LocalTime.now())).id();

        themeId1 = reservationThemeService.addReservationTheme(new ReservationThemeRequest("Theme 1", "Description 1", "thumbnail1")).id();
        themeId2 = reservationThemeService.addReservationTheme(new ReservationThemeRequest("Theme 2", "Description 2", "thumbnail2")).id();

        tomorrow = LocalDate.now().plusDays(1);
        twoDaysLater = LocalDate.now().plusDays(2);
        threeDaysLater = LocalDate.now().plusDays(3);

        reservationService.addReservationWithMemberId(
                new ReservationRequest(tomorrow, themeId1, timeId), memberId1);

        reservationService.addReservationWithMemberId(
                new ReservationRequest(twoDaysLater, themeId2, timeId), memberId1);

        reservationService.addReservationWithMemberId(
                new ReservationRequest(twoDaysLater, themeId1, timeId), memberId2);

        reservationService.addReservationWithMemberId(
                new ReservationRequest(threeDaysLater, themeId2, timeId), memberId2);
    }

    @Test
    @DisplayName("사용자의 id를 이용해 예약을 생성한다")
    void createReservationTest() {
        // given
        Long memberId = memberService.addMember(new MemberRegisterRequest("new@example.com", "new-password", "new-name")).id();
        Long timeId = reservationTimeService.addReservationTime(new ReservationTimeRequest(LocalTime.now())).id();
        Long themeId = reservationThemeService.addReservationTheme(new ReservationThemeRequest("new Theme", "new Description", "new Thumbnail")).id();

        ReservationRequest reservationRequest = new ReservationRequest(
                LocalDate.now().plusDays(1),
                themeId,
                timeId
        );

        // when
        ReservationResponse reservationResponse = reservationService.addReservationWithMemberId(reservationRequest,
                memberId);

        // then
        assertAll(
                () -> assertThat(reservationResponse.id()),
                () -> assertThat(reservationResponse.date()),
                () -> assertThat(reservationResponse.name())
        );
    }

    @Test
    @DisplayName("사용자가 없는 theme id 또는 time id를 이용해 예약을 생성한다")
    void createReservationTest2() {
        // given
        Long nonExistTimeId = 999L;
        Long nonExistThemeId = 999L;

        ReservationRequest reservationRequest1 = new ReservationRequest(
                LocalDate.now().plusDays(1),
                nonExistThemeId,
                timeId
        );

        ReservationRequest reservationRequest2 = new ReservationRequest(
                LocalDate.now().plusDays(1),
                themeId1,
                nonExistTimeId
        );

        // when, then
        assertAll(
                () -> assertThatThrownBy(
                        () -> reservationService.addReservationWithMemberId(reservationRequest1, memberId1)
                ).isInstanceOf(NoSuchElementException.class),
                () -> assertThatThrownBy(
                        () -> reservationService.addReservationWithMemberId(reservationRequest2, memberId1)
                ).isInstanceOf(NoSuchElementException.class)
        );
    }

    @Test
    @DisplayName("미래가 아닌 날짜로 예약 시도 시 예외 발생")
    void createReservationTest3() {
        // given
        ReservationRequest reservationRequest1 = new ReservationRequest(
                LocalDate.now(),
                themeId1,
                timeId
        );

        ReservationRequest reservationRequest2 = new ReservationRequest(
                LocalDate.now().minusDays(1),
                themeId1,
                timeId
        );

        // when, then
        assertAll(
                () -> assertThatThrownBy(
                        () -> reservationService.addReservationWithMemberId(reservationRequest1, memberId1)
                ).isInstanceOf(IllegalArgumentException.class),
                () -> assertThatThrownBy(
                        () -> reservationService.addReservationWithMemberId(reservationRequest2, memberId1)
                ).isInstanceOf(IllegalArgumentException.class)
        );
    }


    @DisplayName("예약이 중복되어 예외가 발생 한다.")
    @Test
    void duplicateTest() {
        // given
        ReservationRequest reservationRequest = new ReservationRequest(
                tomorrow,
                themeId1,
                timeId
        );

        // when & then
        assertThatThrownBy(() -> reservationService.addReservationWithMemberId(reservationRequest, memberId1))
                .isInstanceOf(IllegalArgumentException.class);
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
}
