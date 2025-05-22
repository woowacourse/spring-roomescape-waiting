package roomescape.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.domain.waiting.Waiting;
import roomescape.domain.waiting.WaitingWithRank;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class JpaWaitingRepositoryTest {

    @Autowired
    private JpaThemeRepository themeRepository;

    @Autowired
    private JpaWaitingRepository waitingRepository;

    @Autowired
    private JpaReservationTimeRepository reservationTimeRepository;

    @Autowired
    private JpaMemberRepository memberRepository;

    Member savedMember;
    ReservationTime savedTime;
    Theme savedTheme;
    Waiting savedWaiting;

    @BeforeEach
    void setUp() {
        Member member = new Member(null, "가이온", "hello@woowa.com", Role.USER, "password");
        savedMember = memberRepository.save(member);

        ReservationTime time = new ReservationTime(null, LocalTime.of(10,0));
        savedTime = reservationTimeRepository.save(time);

        Theme theme = new Theme(null, "테마1", "설명", "썸네일");
        savedTheme = themeRepository.save(theme);

        Waiting waiting = Waiting.createWithoutId(member, LocalDate.now(), time, theme);
        savedWaiting = waitingRepository.save(waiting);
    }

    @DisplayName("날짜, 테마, 멤버, 시간으로 예약대기가 존재하는지 확인 테스트")
    @Test
    void existByDateAndThemeAndMemberAndTime(){
        boolean exists = waitingRepository.existsFor(LocalDate.now(), savedTime.getId(), savedTheme.getId(), savedMember.getId());

        assertTrue(exists);
    }

    @Test
    @DisplayName("특정 날짜, 시간, 테마, 회원에 대한 예약 대기 존재 여부를 확인한다")
    void existsFor() {
        // given
        Member member = new Member(null, "테스트", "test@test.com", Role.USER, "password");
        member = memberRepository.save(member);
        Theme theme = new Theme(null, "테마1", "테마1 설명", "테마1 썸네일");
        theme = themeRepository.save(theme);
        ReservationTime time = new ReservationTime(null, LocalTime.of(10, 0));
        time = reservationTimeRepository.save(time);
        LocalDate date = LocalDate.now().plusDays(1);
        Waiting waiting = Waiting.createWithoutId(member, date, time, theme);
        waitingRepository.save(waiting);

        // when
        boolean exists = waitingRepository.existsFor(date, time.getId(), theme.getId(), member.getId());

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("회원의 예약 대기 목록을 순위와 함께 조회한다")
    void findByMemberId() {
        // given
        Member member1 = new Member(null, "테스트", "test@test.com", Role.USER, "password");
        member1 = memberRepository.save(member1);

        Member member2 = new Member(null, "테스트", "test@test.com", Role.USER, "password");
        member2 = memberRepository.save(member2);

        Theme theme = new Theme(null, "테마1", "테마1 설명", "테마1 썸네일");
        theme = themeRepository.save(theme);
        ReservationTime time = new ReservationTime(null, LocalTime.of(10, 0));
        time = reservationTimeRepository.save(time);

        LocalDate date = LocalDate.now().plusDays(1);

        Waiting waiting1 = Waiting.createWithoutId(member1, date, time, theme);
        Waiting waiting2 = Waiting.createWithoutId(member2, date, time, theme);
        waitingRepository.save(waiting1);
        waiting2 = waitingRepository.save(waiting2);

        // when
        List<WaitingWithRank> result = waitingRepository.findByMemberId(member2.getId());

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getWaiting().getId()).isEqualTo(waiting2.getId());
        assertThat(result.get(0).getRank()).isEqualTo(2);
    }

    @Test
    @DisplayName("특정 테마, 날짜, 시간에 대한 예약 대기 목록을 조회한다")
    void findWaitingsFor() {
        // given
        Member member = new Member(null, "테스트", "test@test.com", Role.USER, "password");
        member = memberRepository.save(member);
        Theme theme = new Theme(null, "테마1", "테마1 설명", "테마1 썸네일");
        theme = themeRepository.save(theme);
        ReservationTime time = new ReservationTime(null, LocalTime.of(10, 0));
        time = reservationTimeRepository.save(time);
        LocalDate date = LocalDate.now().plusDays(1);
        Waiting waiting = Waiting.createWithoutId(member, date, time, theme);
        waiting = waitingRepository.save(waiting);

        // when
        List<Waiting> result = waitingRepository.findWaitingsFor(theme, date, time, PageRequest.of(0, 1));

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(waiting.getId());
    }
}
