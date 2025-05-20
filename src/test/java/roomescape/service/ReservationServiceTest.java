package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;
import roomescape.dto.MemberRegisterRequest;
import roomescape.dto.ReservationRequestV2;
import roomescape.dto.ReservationResponse;
import roomescape.dto.ReservationThemeRequest;
import roomescape.dto.ReservationTimeRequest;

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

    @Test
    @DisplayName("사용자의 id를 이용해 예약을 생성한다")
    void createReservationTest() {
        // given
        Long memberId = memberService.addMember(new MemberRegisterRequest("", "", "")).id();
        Long timeId = reservationTimeService.addReservationTime(new ReservationTimeRequest(LocalTime.now())).id();
        Long themeId = reservationThemeService.addReservationTheme(new ReservationThemeRequest("", "", "")).id();

        ReservationRequestV2 reservationRequest = new ReservationRequestV2(
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
    @DisplayName("사용자가 없는 theme id를 이용해 예약을 생성한다")
    void createReservationTest2() {
        // given
        Long memberId = memberService.addMember(new MemberRegisterRequest("", "", "")).id();
        Long timeId = reservationTimeService.addReservationTime(new ReservationTimeRequest(LocalTime.now())).id();
        Long themeId = reservationThemeService.addReservationTheme(new ReservationThemeRequest("", "", "")).id();
        Long nonExistTimeId = 2L;
        Long nonExistThemeId = 2L;

        ReservationRequestV2 reservationRequest1 = new ReservationRequestV2(
                LocalDate.now().plusDays(1),
                nonExistThemeId,
                timeId
        );

        ReservationRequestV2 reservationRequest2 = new ReservationRequestV2(
                LocalDate.now().plusDays(1),
                themeId,
                nonExistTimeId
        );

        // when, then
        assertAll(
                () -> assertThatThrownBy(
                        () -> reservationService.addReservationWithMemberId(reservationRequest1, memberId)
                ).isInstanceOf(NoSuchElementException.class),
                () -> assertThatThrownBy(
                        () -> reservationService.addReservationWithMemberId(reservationRequest2, memberId)
                ).isInstanceOf(NoSuchElementException.class)
        );
    }

    @Test
    @DisplayName("미래가 아닌 날짜로 예약 시도 시 예외 발생")
    void createReservationTest3() {
        // given
        Long memberId = memberService.addMember(new MemberRegisterRequest("", "", "")).id();
        Long timeId = reservationTimeService.addReservationTime(new ReservationTimeRequest(LocalTime.now())).id();
        Long themeId = reservationThemeService.addReservationTheme(new ReservationThemeRequest("", "", "")).id();

        ReservationRequestV2 reservationRequest1 = new ReservationRequestV2(
                LocalDate.now(),
                themeId,
                timeId
        );

        ReservationRequestV2 reservationRequest2 = new ReservationRequestV2(
                LocalDate.now().minusDays(1),
                themeId,
                timeId
        );

        // when, then
        assertAll(
                () -> assertThatThrownBy(
                        () -> reservationService.addReservationWithMemberId(reservationRequest1, memberId)
                ).isInstanceOf(IllegalArgumentException.class),
                () -> assertThatThrownBy(
                        () -> reservationService.addReservationWithMemberId(reservationRequest2, memberId)
                ).isInstanceOf(IllegalArgumentException.class)
        );
    }


    @DisplayName("예약이 중복되어 예외가 발생 한다.")
    @Test
    void duplicateTest() {
        // given
        Long memberId = memberService.addMember(new MemberRegisterRequest("", "", "")).id();
        Long timeId = reservationTimeService.addReservationTime(new ReservationTimeRequest(LocalTime.now())).id();
        Long themeId = reservationThemeService.addReservationTheme(new ReservationThemeRequest("", "", "")).id();

        ReservationRequestV2 reservationRequest = new ReservationRequestV2(
                LocalDate.now().plusDays(1),
                themeId,
                timeId
        );
        reservationService.addReservationWithMemberId(reservationRequest,
                memberId);

        // when & then
        assertThatThrownBy(() -> reservationService.addReservationWithMemberId(reservationRequest, memberId))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("모든 예약 정보를 가져온다.")
    void getAllReservationsTest() {
        // given
        Long memberId = memberService.addMember(new MemberRegisterRequest("", "", "")).id();
        Long timeId = reservationTimeService.addReservationTime(new ReservationTimeRequest(LocalTime.now())).id();
        Long themeId = reservationThemeService.addReservationTheme(new ReservationThemeRequest("", "", "")).id();

        ReservationRequestV2 reservationRequest = new ReservationRequestV2(
                LocalDate.now().plusDays(1),
                themeId,
                timeId
        );
        reservationService.addReservationWithMemberId(reservationRequest,
                memberId);
        //when
        final List<ReservationResponse> expected = reservationService.getAllReservations();

        //then
        assertThat(expected).hasSize(1);
    }
}
