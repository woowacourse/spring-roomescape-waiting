package roomescape.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.*;
import roomescape.infrastructure.*;
import roomescape.service.request.WaitingAppRequest;
import roomescape.service.response.ReservationWaitingAppResponse;
import roomescape.service.response.ReservationWaitingWithRankAppResponse;
import roomescape.web.exception.AuthorizationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.Fixture.VALID_RESERVATION_TIME;
import static roomescape.Fixture.VALID_THEME;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Sql(scripts = "/truncate.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Transactional
class ReservationWaitingServiceTest {

    @Autowired
    private ReservationWaitingService reservationWaitingService;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationWaitingRepository reservationWaitingRepository;

    private final List<Member> members = List.of(
            new Member(
                    new MemberName("감자"),
                    new MemberEmail("111@aaa.com"),
                    new MemberPassword("asd"),
                    MemberRole.USER
            ),
            new Member(
                    new MemberName("고구마"),
                    new MemberEmail("222@aaa.com"),
                    new MemberPassword("asd"),
                    MemberRole.USER
            ),
            new Member(
                    new MemberName("단호박"),
                    new MemberEmail("333@aaa.com"),
                    new MemberPassword("asd"),
                    MemberRole.USER
            )
    );

    @Test
    @DisplayName("올바르게 예약 대기를 생성한다.")
    void save() {
        Member otherMember = memberRepository.save(members.get(0));
        Member member = memberRepository.save(members.get(1));
        String date = LocalDate.now().plusDays(1).toString();
        Theme theme = themeRepository.save(VALID_THEME);
        ReservationTime time = reservationTimeRepository.save(VALID_RESERVATION_TIME);
        reservationRepository.save(new Reservation(otherMember, new ReservationDate(date), time, theme));

        WaitingAppRequest request = new WaitingAppRequest(date, time.getId(), theme.getId(), member.getId());
        ReservationWaitingAppResponse response = reservationWaitingService.save(request);
        ReservationWaiting expectedWaiting = reservationWaitingRepository.findById(response.id()).get();

        assertThat(response).isEqualTo(new ReservationWaitingAppResponse(expectedWaiting));
    }

    @Test
    @DisplayName("본인이 예약한 날짜, 시간, 테마에 대해서 대기 생성 시 예외가 발생한다.")
    void saveWithSelfReservationExist() {
        Member member = memberRepository.save(members.get(0));
        String date = LocalDate.now().plusDays(1).toString();
        Theme theme = themeRepository.save(VALID_THEME);
        ReservationTime time = reservationTimeRepository.save(VALID_RESERVATION_TIME);
        reservationRepository.save(new Reservation(member, new ReservationDate(date), time, theme));

        WaitingAppRequest request = new WaitingAppRequest(date, time.getId(), theme.getId(), member.getId());

        assertThatThrownBy(() -> reservationWaitingService.save(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("본인이 예약한 날짜, 시간, 테마에 대해서는 대기를 생성할 수 없습니다.");
    }

    @Test
    @DisplayName("예약이 없는 날짜, 시간, 테마에 대해서 대기 생성 시 예외가 발생한다.")
    void saveWithNoReservationExist() {
        Member member = memberRepository.save(members.get(0));
        String date = LocalDate.now().plusDays(1).toString();
        Theme theme = themeRepository.save(VALID_THEME);
        ReservationTime time = reservationTimeRepository.save(VALID_RESERVATION_TIME);

        WaitingAppRequest request = new WaitingAppRequest(date, time.getId(), theme.getId(), member.getId());

        assertThatThrownBy(() -> reservationWaitingService.save(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("예약이 존재하지 않는 날짜, 시간, 테마에 대해서는 대기를 생성할 수 없습니다.");
    }

    @Test
    @DisplayName("날짜, 시간, 테마에 대해서 중복된 대기 생성 시 예외가 발생한다.")
    void saveWithDuplicatedWaiting() {
        Member otherMember = memberRepository.save(members.get(0));
        Member member = memberRepository.save(members.get(1));
        String date = LocalDate.now().plusDays(1).toString();
        Theme theme = themeRepository.save(VALID_THEME);
        ReservationTime time = reservationTimeRepository.save(VALID_RESERVATION_TIME);
        reservationRepository.save(new Reservation(otherMember, new ReservationDate(date), time, theme));
        reservationWaitingRepository.save(new ReservationWaiting(LocalDateTime.now(), member, new ReservationDate(date), time, theme));

        WaitingAppRequest request = new WaitingAppRequest(date, time.getId(), theme.getId(), member.getId());

        assertThatThrownBy(() -> reservationWaitingService.save(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("동일한 사용자의 중복된 예약 대기를 생성할 수 없습니다. ");
    }

    @Test
    @DisplayName("지나간 시간에 대한 대기 생성 시 예외가 발생한다.")
    void saveWithPastTime() {
        Member otherMember = memberRepository.save(members.get(0));
        Member member = memberRepository.save(members.get(1));
        String date = LocalDate.now().minusDays(1).toString();
        Theme theme = themeRepository.save(VALID_THEME);
        ReservationTime time = reservationTimeRepository.save(VALID_RESERVATION_TIME);
        reservationRepository.save(new Reservation(otherMember, new ReservationDate(date), time, theme));

        WaitingAppRequest request = new WaitingAppRequest(date, time.getId(), theme.getId(), member.getId());

        assertThatThrownBy(() -> reservationWaitingService.save(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("지나간 시간에 대한 에약 대기는 생성할 수 없습니다.");
    }

    @Test
    @DisplayName("지나간 시간에 대한 대기 생성 시 예외가 발생한다.")
    void findWaitingWithRankByMemberId() {
        List<Member> savedMembers = members.stream()
                .map(memberRepository::save)
                .toList();
        String date = LocalDate.now().minusDays(1).toString();
        LocalDateTime createdDateTime = LocalDateTime.now().minusMonths(2);
        Theme theme = themeRepository.save(VALID_THEME);
        ReservationTime time = reservationTimeRepository.save(VALID_RESERVATION_TIME);
        reservationRepository.save(new Reservation(savedMembers.get(0), new ReservationDate(date), time, theme));
        reservationWaitingRepository.save(new ReservationWaiting(createdDateTime, savedMembers.get(1), new ReservationDate(date), time, theme));
        ReservationWaiting savedWaiting = reservationWaitingRepository.save(new ReservationWaiting(createdDateTime, savedMembers.get(2), new ReservationDate(date), time, theme));

        List<ReservationWaitingWithRankAppResponse> waitingWithRankAppResponses = reservationWaitingService.findWaitingWithRankByMemberId(savedMembers.get(2).getId());

        assertThat(waitingWithRankAppResponses)
                .hasSize(1)
                .containsExactly(new ReservationWaitingWithRankAppResponse(new ReservationWaitingWithRank(savedWaiting, 2L)));
    }

    @Test
    @DisplayName("사용자의 예약 대기를 올바르게 삭제한다.")
    void deleteMemberWaiting() {
        Member reservedMember = memberRepository.save(members.get(0));
        Member waitingMember = memberRepository.save(members.get(1));
        String date = LocalDate.now().minusDays(1).toString();
        LocalDateTime createdDateTime = LocalDateTime.now().minusMonths(2);
        Theme theme = themeRepository.save(VALID_THEME);
        ReservationTime time = reservationTimeRepository.save(VALID_RESERVATION_TIME);
        reservationRepository.save(new Reservation(reservedMember, new ReservationDate(date), time, theme));
        ReservationWaiting savedWaiting = reservationWaitingRepository.save(new ReservationWaiting(createdDateTime, waitingMember, new ReservationDate(date), time, theme));

        reservationWaitingService.deleteMemberWaiting(waitingMember.getId(), savedWaiting.getId());

        assertThat(reservationWaitingRepository.findById(savedWaiting.getId())).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 예약 대기 삭제 시 예외가 발생한다.")
    void deleteNotExistWaiting() {
        Member member = memberRepository.save(members.get(0));

        assertThatThrownBy(() ->
                reservationWaitingService.deleteMemberWaiting(member.getId(), 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("예약 대기 삭제 실패: 대기를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("자신이 생성하지 않은 예약 대기 삭제 시 예외가 발생한다.")
    void deleteWaitingWithInvalidMember() {
        Member reservedMember = memberRepository.save(members.get(0));
        Member waitingMember = memberRepository.save(members.get(1));
        String date = LocalDate.now().minusDays(1).toString();
        LocalDateTime createdDateTime = LocalDateTime.now().minusMonths(2);
        Theme theme = themeRepository.save(VALID_THEME);
        ReservationTime time = reservationTimeRepository.save(VALID_RESERVATION_TIME);
        reservationRepository.save(new Reservation(reservedMember, new ReservationDate(date), time, theme));
        ReservationWaiting savedWaiting = reservationWaitingRepository.save(new ReservationWaiting(createdDateTime, waitingMember, new ReservationDate(date), time, theme));

        assertThatThrownBy(() ->
                reservationWaitingService.deleteMemberWaiting(reservedMember.getId(), savedWaiting.getId()))
                .isInstanceOf(AuthorizationException.class)
                .hasMessageContaining("예약 대기 삭제 권한이 없는 사용자입니다.");
    }
}
