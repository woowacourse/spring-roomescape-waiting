package roomescape.service.member;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.member.Role;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.ReservationWaiting;
import roomescape.domain.reservation.ReservationWaitingRepository;
import roomescape.domain.schedule.ReservationDate;
import roomescape.domain.schedule.ReservationTime;
import roomescape.domain.schedule.ReservationTimeRepository;
import roomescape.domain.schedule.Schedule;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.service.member.dto.MemberReservationResponse;
import roomescape.service.member.dto.MemberResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Sql("/truncate.sql")
class MemberServiceTest {
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberService memberService;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private ReservationWaitingRepository reservationWaitingRepository;

    @DisplayName("존재하는 모든 사용자를 조회한다.")
    @Test
    void findAll() {
        // given
        memberRepository.save(new Member("lini", "lini@email.com", "lini123", Role.MEMBER));
        memberRepository.save(new Member("lini2", "lini2@email.com", "lini123", Role.MEMBER));
        memberRepository.save(new Member("lini3", "lini3@email.com", "lini123", Role.MEMBER));

        // when
        List<MemberResponse> memberResponses = memberService.findAll();

        // then
        assertThat(memberResponses).hasSize(3);
    }

    @DisplayName("id로 사용자를 조회한다.")
    @Test
    void findById() {
        // given
        Member member = memberRepository.save(new Member("lini", "lini@email.com", "lini123", Role.MEMBER));

        // when
        Member result = memberService.findById(member.getId());

        // then
        assertThat(result.getEmail()).isEqualTo(member.getEmail());
    }

    @DisplayName("id로 사용자의 예약과 예약 대기 목록을 조회한다.")
    @Test
    void findReservations() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.now()));
        Theme theme = themeRepository.save(
                new Theme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg")
        );
        Member member = memberRepository.save(new Member("pedro", "pedro@email.com", "pedro123", Role.MEMBER));
        Schedule schedule = new Schedule(ReservationDate.of(LocalDate.now()), reservationTime);
        memberRepository.save(new Member("pedro", "pedro@email.com", "pedro123", Role.MEMBER));
        reservationRepository.save(
                new Reservation(member, schedule, theme, ReservationStatus.RESERVED)
        );
        reservationWaitingRepository.save(new ReservationWaiting(member, theme, schedule));

        // when
        List<MemberReservationResponse> reservations = memberService.findReservations(1L);

        // then
        assertThat(reservations).hasSize(2);
    }
}
