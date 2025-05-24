package roomescape.reservation.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Waiting;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

@DataJpaTest
public class WaitingRepositoryTest {

    @Autowired
    private WaitingRepository waitingRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Test
    void 대기_저장() {
        // given
        final Member member = new Member("우가", "wooga@email.com", "1234", Role.USER);
        final ReservationTime reservationTime = new ReservationTime(LocalTime.of(20, 20));
        final Theme theme = new Theme("공포", "설명", "썸네일");
        final LocalDate date = LocalDate.of(2026, 10, 10);
        final Waiting waiting = new Waiting(member, reservationTime, theme, date);

        memberRepository.save(member);
        reservationTimeRepository.save(reservationTime);
        themeRepository.save(theme);

        // when
        final Waiting savedWaiting = waitingRepository.save(waiting);

        // then
        Assertions.assertThat(waitingRepository.findAll()).containsExactly(savedWaiting);
    }

    @Test
    void 날짜와_테마와_시간이_같은_대기_존재_여부_확인() {
        // given
        final Member member = new Member("우가", "wooga@email.com", "1234", Role.USER);
        final ReservationTime reservationTime = new ReservationTime(LocalTime.of(20, 20));
        final Theme theme = new Theme("공포", "설명", "썸네일");
        final LocalDate date = LocalDate.of(2026, 10, 10);
        final Waiting waiting = new Waiting(member, reservationTime, theme, date);

        memberRepository.save(member);
        reservationTimeRepository.save(reservationTime);
        themeRepository.save(theme);
        waitingRepository.save(waiting);

        // when
        final boolean exists = waitingRepository.existsByDateAndTimeAndTheme(date, reservationTime, theme);

        // then
        Assertions.assertThat(exists).isTrue();
    }

    @Test
    void 멤버를_기준으로_대기_찾기() {
        // given
        final Member member = new Member("우가", "wooga@email.com", "1234", Role.USER);
        final Member member2 = new Member("엘리", "yebink@gmail.com", "1234", Role.ADMIN);
        final ReservationTime reservationTime = new ReservationTime(LocalTime.of(20, 20));
        final Theme theme = new Theme("공포", "설명", "썸네일");
        final LocalDate date = LocalDate.of(2026, 10, 10);
        final Waiting waiting = new Waiting(member, reservationTime, theme, date);
        final Waiting waiting2 = new Waiting(member2, reservationTime, theme, date);

        memberRepository.save(member);
        memberRepository.save(member2);
        reservationTimeRepository.save(reservationTime);
        themeRepository.save(theme);
        final Waiting savedWaiting = waitingRepository.save(waiting);
        waitingRepository.save(waiting2);

        // when
        final List<Waiting> foundWaiting = waitingRepository.findByMember(member);

        // then
        Assertions.assertThat(foundWaiting).containsOnly(savedWaiting);
    }

    @Test
    void 아이디_기준으로_대기_찾기() {
        // given
        final Member member = new Member("우가", "wooga@email.com", "1234", Role.USER);
        final ReservationTime reservationTime = new ReservationTime(LocalTime.of(20, 20));
        final Theme theme = new Theme("공포", "설명", "썸네일");
        final LocalDate date = LocalDate.of(2026, 10, 10);
        final Waiting waiting = new Waiting(member, reservationTime, theme, date);

        memberRepository.save(member);
        reservationTimeRepository.save(reservationTime);
        themeRepository.save(theme);
        final Waiting savedWaiting = waitingRepository.save(waiting);

        // when
        final Waiting foundWaiting = waitingRepository.findById(savedWaiting.getId()).orElseThrow();

        // then
        Assertions.assertThat(foundWaiting).isEqualTo(savedWaiting);
    }

    @Test
    void 테마와_날짜와_시간이_같은_대기를_찾는데_ID를_기준으로_첫번째_찾기() {
        // given
        final Member member = new Member("우가", "wooga@email.com", "1234", Role.USER);
        final Member member2 = new Member("엘리", "yebink@gmail.com", "1234", Role.ADMIN);
        final ReservationTime reservationTime = new ReservationTime(LocalTime.of(20, 20));
        final Theme theme = new Theme("공포", "설명", "썸네일");
        final LocalDate date = LocalDate.of(2026, 10, 10);
        final Waiting waiting = new Waiting(member, reservationTime, theme, date);
        final Waiting waiting2 = new Waiting(member2, reservationTime, theme, date);

        memberRepository.save(member);
        memberRepository.save(member2);
        reservationTimeRepository.save(reservationTime);
        themeRepository.save(theme);
        final Waiting savedWaiting = waitingRepository.save(waiting);
        waitingRepository.save(waiting2);

        // when
        final Waiting foundWaiting = waitingRepository.findFirstByThemeAndDateAndTimeOrderByIdAsc(theme, date,
                        reservationTime)
                .orElseThrow();

        // then
        Assertions.assertThat(foundWaiting).isEqualTo(savedWaiting);
    }

    @Test
    void 아이디를_기준으로_대기_삭제() {
        // given
        final Member member = new Member("우가", "wooga@email.com", "1234", Role.USER);
        final ReservationTime reservationTime = new ReservationTime(LocalTime.of(20, 20));
        final Theme theme = new Theme("공포", "설명", "썸네일");
        final LocalDate date = LocalDate.of(2026, 10, 10);
        final Waiting waiting = new Waiting(member, reservationTime, theme, date);

        memberRepository.save(member);
        reservationTimeRepository.save(reservationTime);
        themeRepository.save(theme);
        final Waiting savedWaiting = waitingRepository.save(waiting);

        // when
        waitingRepository.deleteById(savedWaiting.getId());

        // then
        Assertions.assertThat(waitingRepository.findById(savedWaiting.getId())).isEmpty();
    }

    @Test
    void 테마와_날짜와_시간과_아이디보다_작은_것의_개수_세기() {
        // given
        final Member member = new Member("우가", "wooga@email.com", "1234", Role.USER);
        final Member member2 = new Member("엘리", "yebink@gmail.com", "1234", Role.ADMIN);
        final ReservationTime reservationTime = new ReservationTime(LocalTime.of(20, 20));
        final Theme theme = new Theme("공포", "설명", "썸네일");
        final LocalDate date = LocalDate.of(2026, 10, 10);
        final Waiting waiting = new Waiting(member, reservationTime, theme, date);
        final Waiting waiting2 = new Waiting(member2, reservationTime, theme, date);

        memberRepository.save(member);
        memberRepository.save(member2);
        reservationTimeRepository.save(reservationTime);
        themeRepository.save(theme);
        final Waiting savedWaiting = waitingRepository.save(waiting);
        final Waiting savedWaiting2 = waitingRepository.save(waiting2);

        // when
        final long count = waitingRepository.countBefore(theme, date, reservationTime, savedWaiting2.getId());

        // then
        Assertions.assertThat(count).isEqualTo(1);
    }

    @Test
    void 모든_대기_정보_찾기() {
        // given
        final Member member = new Member("우가", "wooga@email.com", "1234", Role.USER);
        final ReservationTime reservationTime = new ReservationTime(LocalTime.of(20, 20));
        final Theme theme = new Theme("공포", "설명", "썸네일");
        final LocalDate date = LocalDate.of(2026, 10, 10);
        final Waiting waiting = new Waiting(member, reservationTime, theme, date);

        memberRepository.save(member);
        reservationTimeRepository.save(reservationTime);
        themeRepository.save(theme);
        final Waiting savedWaiting = waitingRepository.save(waiting);

        // when
        final List<Waiting> allWaitings = waitingRepository.findAll();

        // then
        Assertions.assertThat(allWaitings).containsExactly(savedWaiting);
    }
}
