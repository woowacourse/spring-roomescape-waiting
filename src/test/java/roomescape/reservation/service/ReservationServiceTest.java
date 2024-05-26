package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import roomescape.config.DatabaseCleaner;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.dto.LoginMemberInToken;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.dto.request.ReservationCreateRequest;
import roomescape.reservation.dto.request.ReservationSearchRequest;
import roomescape.reservation.dto.response.MyReservationResponse;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.repository.ThemeRepository;
import roomescape.reservation.repository.WaitingRepository;
import roomescape.reservation.vo.WaitingWithRank;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class ReservationServiceTest {

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private WaitingRepository waitingRepository;

    @Autowired
    private ReservationService reservationService;

    @AfterEach
    void init() {
        databaseCleaner.cleanUp();
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간에 예약을 하면 예외가 발생한다.")
    void notExistReservationTimeIdExceptionTest() {
        themeRepository.save(new Theme("공포", "호러 방탈출", "http://asdf.jpg"));
        LoginMemberInToken loginMemberInToken = new LoginMemberInToken(1L, Role.MEMBER, "카키", "kaki@email.com");
        ReservationCreateRequest reservationCreateRequest = new ReservationCreateRequest(
                LocalDate.now(), 1L, 1L);

        assertThatThrownBy(() -> reservationService.save(reservationCreateRequest, loginMemberInToken))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("중복된 예약이 있다면 예외가 발생한다.")
    void duplicateReservationExceptionTest() {
        Theme theme = themeRepository.save(new Theme("공포", "호러 방탈출", "http://asdf.jpg"));
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.parse("10:00")));
        Member member = memberRepository.save(new Member(null, Role.MEMBER, "호기", "hogi@email.com", "1234"));
        LocalDate date = LocalDate.now();
        ReservationCreateRequest request = new ReservationCreateRequest(date, theme.getId(), time.getId());
        LoginMemberInToken loginMember = new LoginMemberInToken(1L, member.getRole(), member.getName(), member.getEmail());
        reservationService.save(request, loginMember);

        ReservationCreateRequest duplicateRequest = new ReservationCreateRequest(date, theme.getId(), time.getId());
        assertThatThrownBy(() -> reservationService.save(duplicateRequest, loginMember))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void findByMemberAndThemeAndDateBetweenTest() {
        Theme theme = new Theme("공포", "호러 방탈출", "http://asdf.jpg");
        Long themeId = themeRepository.save(theme).getId();
        LocalTime localTime = LocalTime.parse("10:00");
        ReservationTime reservationTime = new ReservationTime(localTime);
        Long timeId = reservationTimeRepository.save(reservationTime).getId();
        Member member = new Member("마크", "mark@woowa.com", "1234");
        memberRepository.save(member);

        LocalDate localDate = LocalDate.now().plusYears(1);
        ReservationCreateRequest reservationCreateRequest = new ReservationCreateRequest(localDate, themeId, timeId);
        LoginMemberInToken loginMemberInToken = new LoginMemberInToken(1L, member.getRole(), member.getName(),
                member.getEmail());
        reservationService.save(reservationCreateRequest, loginMemberInToken);

        assertThat(
                reservationService.findAllBySearch(new ReservationSearchRequest(member.getId(), themeId, localDate, localDate)))
                .isNotEmpty();
    }

    @Test
    @DisplayName("예약을 삭제할 때 동일한 테마, 날짜, 시간인 예약 대기들이 있으면 첫 번째 대기가 예약으로 변경되고 그 대기는 삭제된다.")
    void deleteTest() {
        // 예약 생성
        Theme theme = themeRepository.save(new Theme("공포", "호러 방탈출", "http://asdf.jpg"));
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.now()));
        Member member = memberRepository.save(new Member("마크", "mark@woowa.com", "asd"));
        Member member2 = memberRepository.save(new Member("안돌", "andol@woowa.com", "asd"));
        LocalDate date = LocalDate.now().plusYears(1);
        ReservationCreateRequest request = new ReservationCreateRequest(date, theme.getId(), time.getId());
        LoginMemberInToken loginMember = new LoginMemberInToken(1L, member.getRole(), member.getName(), member.getEmail());
        Reservation reservation = reservationService.save(request, loginMember);
        // 동일한 테마, 날짜, 시간인 예약 대기 생성
        waitingRepository.save(new Waiting(member2, date, theme, time));

        assertThat(reservationService.findAllByMemberId(member.getId())).hasSize(1);
        assertThat(reservationService.findAllByMemberId(member2.getId())).isEmpty();
        assertThat(waitingRepository.findAll()).hasSize(1);

        reservationService.delete(reservation.getId(), loginMember);

        assertThat(reservationService.findAllByMemberId(member.getId())).isEmpty();
        assertThat(reservationService.findAllByMemberId(member2.getId())).hasSize(1);
        assertThat(waitingRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("본인의 예약만 취소할 수 있다.")
    void deleteTest_Fail1() {
        Theme theme = themeRepository.save(new Theme("공포", "호러 방탈출", "http://asdf.jpg"));
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.now()));
        Member member = memberRepository.save(new Member("마크", "mark@woowa.com", "1234"));
        LocalDate date = LocalDate.now().plusYears(1);
        ReservationCreateRequest request = new ReservationCreateRequest(date, theme.getId(), time.getId());
        LoginMemberInToken loginMember = new LoginMemberInToken(1L, member.getRole(), member.getName(), member.getEmail());
        Long reservationId = reservationService.save(request, loginMember).getId();

        assertThat(reservationService.findAllByMemberId(member.getId())).isNotEmpty();

        reservationService.delete(reservationId, loginMember);
    }
}
