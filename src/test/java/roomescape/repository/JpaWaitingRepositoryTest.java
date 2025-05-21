package roomescape.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.domain.waiting.Waiting;

import java.time.LocalDate;
import java.time.LocalTime;

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
}
