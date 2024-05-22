package roomescape.service.member;

import org.apache.catalina.Store;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.member.Role;
import roomescape.domain.reservation.*;
import roomescape.domain.schedule.ReservationDate;
import roomescape.domain.schedule.ReservationTime;
import roomescape.domain.schedule.ReservationTimeRepository;
import roomescape.domain.schedule.Schedule;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.service.member.dto.MemberResponse;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
    private ReservationDetailRepository reservationDetailRepository;

    @DisplayName("존재하는 모든 사용자를 조회한다.")
    @Test
    void findAll() {
        //given
        memberRepository.save(new Member("lini", "lini@email.com", "lini123", Role.GUEST));
        memberRepository.save(new Member("lini2", "lini2@email.com", "lini123", Role.GUEST));
        memberRepository.save(new Member("lini3", "lini3@email.com", "lini123", Role.GUEST));

        //when
        List<MemberResponse> memberResponses = memberService.findAll();

        //then
        assertThat(memberResponses).hasSize(3);
    }

    @DisplayName("id로 사용자를 조회한다.")
    @Test
    void findById() {
        //given
        Member member = memberRepository.save(new Member("lini", "lini@email.com", "lini123", Role.GUEST));

        //when
        Member result = memberService.findById(member.getId());

        //then
        assertThat(result.getEmail()).isEqualTo(member.getEmail());
    }

    @DisplayName("사용자 본인의 모든 예약 및 대기 정보를 조회한다.")
    @Test
    void findReservations(){
        //given
        Member member = memberRepository.save(new Member("lini", "lini@email.com", "lini123", Role.GUEST));
        Member member2 = memberRepository.save(new Member("pedro", "pedro@email.com", "pedro123", Role.GUEST));
        ReservationDetail reservationDetail = createReservationDetail();
        Reservation reservation = new Reservation(member, reservationDetail, ReservationStatus.RESERVED);
        Reservation reservation2 = new Reservation(member, reservationDetail, ReservationStatus.WAITING);
        Reservation reservation3 = new Reservation(member2, reservationDetail, ReservationStatus.WAITING);
        reservationRepository.save(reservation);
        reservationRepository.save(reservation2);
        reservationRepository.save(reservation3);

    }

    private ReservationDetail createReservationDetail() {
        ReservationDate reservationDate = ReservationDate.of(LocalDate.now().plusDays(1));
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10,0)));
        Theme theme = themeRepository.save(new Theme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"));
        return reservationDetailRepository.save(new ReservationDetail(new Schedule(reservationDate, reservationTime), theme));
    }
}
