package roomescape.member.service;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.domain.AuthInfo;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.dto.response.FindReservationResponse;
import roomescape.member.dto.response.FindWaitingRankResponse;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.dto.response.FindMembersResponse;
import roomescape.reservation.model.Reservation;
import roomescape.reservation.model.ReservationTime;
import roomescape.reservation.model.Theme;
import roomescape.reservation.model.Waiting;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.repository.ThemeRepository;
import roomescape.reservation.repository.WaitingRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private WaitingRepository waitingRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    private Member member;
    private AuthInfo authInfo;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(new Member("몰리", Role.USER, "login@naver.com", "hihi"));
        authInfo = new AuthInfo(member.getId(), member.getName(), member.getRole());
    }

    @Test
    @DisplayName("모든 멤버 목록을 조회한다.")
    void getMembers() {
        memberRepository.save(new Member("멤버1", Role.USER, "member1@naver.com", "pass1"));
        memberRepository.save(new Member("멤버2", Role.USER, "member2@naver.com", "pass2"));

        List<FindMembersResponse> response = memberService.getMembers();

        assertAll(
                () -> assertThat(response).hasSize(3),
                () -> assertThat(response).extracting("name").containsExactlyInAnyOrder("몰리", "멤버1", "멤버2")
        );
    }

    @Test
    @DisplayName("멤버의 예약 목록을 조회한다.")
    void getReservationsByMember() {
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.parse("20:00")));
        Theme theme = themeRepository.save(new Theme("테마이름", "설명", "썸네일"));
        reservationRepository.save(new Reservation(member, LocalDate.now().plusDays(1), reservationTime, theme));
        reservationRepository.save(new Reservation(member, LocalDate.now().plusDays(2), reservationTime, theme));

        List<FindReservationResponse> response = memberService.getReservationsByMember(authInfo);

        assertAll(
                () -> assertThat(response).hasSize(2),
                () -> assertThat(response).extracting("date")
                        .containsExactlyInAnyOrder(LocalDate.now().plusDays(1), LocalDate.now().plusDays(2))
        );
    }

    @Test
    @DisplayName("멤버의 대기 목록을 조회한다.")
    void getWaitingsByMember() {
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.parse("20:00")));
        Theme theme = themeRepository.save(new Theme("테마이름", "설명", "썸네일"));
        waitingRepository.save(new Waiting(member, LocalDate.now().plusDays(1), reservationTime, theme));
        waitingRepository.save(new Waiting(member, LocalDate.now().plusDays(2), reservationTime, theme));

        List<FindWaitingRankResponse> response = memberService.getWaitingsByMember(authInfo);

        assertAll(
                () -> assertThat(response).hasSize(2),
                () -> assertThat(response).extracting("date")
                        .containsExactlyInAnyOrder(LocalDate.now().plusDays(1), LocalDate.now().plusDays(2))
        );
    }
}
