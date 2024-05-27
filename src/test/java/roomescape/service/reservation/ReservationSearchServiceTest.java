package roomescape.service.reservation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.domain.Member;
import roomescape.domain.Password;
import roomescape.domain.Role;
import roomescape.domain.dto.ReservationResponse;
import roomescape.domain.dto.ReservationWaitingResponse;
import roomescape.domain.dto.ReservationsMineResponse;
import roomescape.domain.dto.ResponsesWrapper;
import roomescape.repository.ReservationRepository;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ReservationSearchServiceTest {
    private final ReservationSearchService service;
    private final ReservationRepository repository;

    @Autowired
    public ReservationSearchServiceTest(final ReservationSearchService service, final ReservationRepository repository) {
        this.service = service;
        this.repository = repository;
    }

    @Test
    @DisplayName("예약 목록을 반환한다.")
    void given_when_findEntireReservations_then_returnReservationResponses() {
        assertThat(service.findEntireReservations().getData().size()).isEqualTo(10);
    }

    @Test
    @DisplayName("로그인한 회원의 예약 목록을 반환한다.")
    void given_member_when_findMemberReservations_then_returnReservationMineResponses() {
        //given
        Password password = new Password("hashedpassword", "salt");
        Member member = new Member(1L, "user@test.com", password, "poke", Role.USER);
        //when, then
        assertThat(service.findMemberReservations(member).getData()).hasSize(8);
    }

    @Test
    @DisplayName("로그인한 회원의 예약 목록 중 대기중인 예약이 있을경우 대기번호를 포함한 문자를 반환한다.")
    void given_member_when_findMemberReservationsWithWaitingAndGetMessage_then_containsWaitingNumber() {
        //given
        Password password = new Password("hashedpassword", "salt");
        Member member = new Member(1L, "user@test.com", password, "poke", Role.USER);
        //when
        final ReservationsMineResponse waitingMineResponse = service.findMemberReservations(member).getData().get(7);
        final String message = waitingMineResponse.status();
        //then
        assertThat(message).isEqualTo("2번째 예약대기");
    }

    @Test
    @DisplayName("사용자 Id 테마 Id 시작 및 종료 날짜로 예약 목록을 반환한다.")
    void given_memberIdAndThemeIdAndDateFromAndDateTo_when_findReservations_then_returnReservationResponses() {
        //given
        Long themeId = 2L;
        Long memberId = 1L;
        LocalDate dateFrom = LocalDate.parse("2024-04-30");
        LocalDate dateTo = LocalDate.parse("2024-05-01");
        //when
        final ResponsesWrapper<ReservationResponse> reservationResponses = service.findReservations(themeId, memberId, dateFrom, dateTo);
        //then
        assertThat(reservationResponses.getData()).hasSize(3);
    }

    @Test
    @DisplayName("예약 대기 목록을 반환한다.")
    void given_when_findEntireWaitingReservationList_then_ReservationWaitingResponse() {
        //when
        final ResponsesWrapper<ReservationWaitingResponse> waitingReservations = service.findEntireWaitingReservations();
        //then
        assertThat(waitingReservations.getData()).hasSize(2);
    }
}