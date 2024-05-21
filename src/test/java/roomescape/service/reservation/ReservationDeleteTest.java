package roomescape.service.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.domain.ReservationWaitStatus.CONFIRMED;
import static roomescape.domain.ReservationWaitStatus.WAITING;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWait;
import roomescape.domain.Theme;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberEmail;
import roomescape.domain.member.MemberName;
import roomescape.domain.member.MemberPassword;
import roomescape.domain.member.Role;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ReservationWaitRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.BaseServiceTest;

public class ReservationDeleteTest extends BaseServiceTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationWaitRepository reservationWaitRepository;

    @Autowired
    private ReservationDeleteService reservationDeleteService;

    @BeforeEach
    void setUp() {
        reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        themeRepository.save(new Theme("방탈출 1", "1번 방탈출", "썸네일 1"));
        memberRepository.save(new Member(new MemberName("사용자1"),
                new MemberEmail("user1@wooteco.com"),
                new MemberPassword("1234"),
                Role.USER));
        memberRepository.save(new Member(new MemberName("사용자2"),
                new MemberEmail("user2@wooteco.com"),
                new MemberPassword("1234"),
                Role.USER));
        memberRepository.save(new Member(new MemberName("사용자3"),
                new MemberEmail("user3@wooteco.com"),
                new MemberPassword("1234"),
                Role.USER));
    }

    @Test
    @DisplayName("예약 삭제 시 동일 날짜, 시간, 테마의 예약 대기가 있으면 가장 최근의 예약 대기를 승인하고 기존 예약을 삭제한다.")
    void deleteReservationWhenHasReservationWait() {
        Member member = memberRepository.findById(1L).get();
        LocalDate date = LocalDate.now().plusDays(1L);
        ReservationTime time = reservationTimeRepository.findById(1L).get();
        Theme theme = themeRepository.findById(1L).get();
        reservationRepository.save(new Reservation(member, date, time, theme));
        Member member2 = memberRepository.findById(2L).get();
        Member member3 = memberRepository.findById(3L).get();
        reservationWaitRepository.save(new ReservationWait(member2, date, time, theme, WAITING));
        reservationWaitRepository.save(new ReservationWait(member3, date, time, theme, WAITING));

        reservationDeleteService.deleteReservation(1L);

        assertAll(
                () -> assertThat(reservationRepository.findAll()).hasSize(1),
                () -> assertThat(reservationRepository.findAll().get(0).getMember().getId()).isSameAs(2L),
                () -> assertThat(reservationWaitRepository.findById(1L).get().getStatus()).isSameAs(CONFIRMED)
        );
    }
}
