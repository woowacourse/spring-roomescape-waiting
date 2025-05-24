package roomescape.repository.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.entity.Member;
import roomescape.entity.ReservationTime;
import roomescape.entity.Theme;
import roomescape.entity.Waiting;

@DataJpaTest
class JpaWaitingRepositoryTest {

    @PersistenceContext
    private EntityManager em;
    @Autowired
    private JpaThemeRepository themeRepository;
    @Autowired
    private JpaReservationTimeRepository reservationTimeRepository;
    @Autowired
    private JpaMemberRepository memberRepository;
    @Autowired
    private JpaWaitingRepository waitingRepository;

    @Test
    @DisplayName("사용자 정보로 예약 대기를 조회할 수 있다.")
    void findByMemberId() {
        Member member = saveMember(1L);
        Theme theme = saveTheme(1L);
        ReservationTime time = saveTime(LocalTime.of(10, 0));
        LocalDate date = LocalDate.of(2025, 4, 28);

        em.flush();
        em.clear();

        Waiting waiting = new Waiting(member, date, time, theme, 1L);
        waitingRepository.save(waiting);

        assertThat(waitingRepository.findByMemberId(member.getId())).hasSize(1);
    }

    @Test
    @DisplayName("날짜, 테마, 시간으로 예약 대기를 조회할 수 있다.")
    void findByDateAndThemeIdAndTimeId() {
        Member member = saveMember(1L);
        Theme theme = saveTheme(1L);
        ReservationTime time = saveTime(LocalTime.of(10, 0));
        LocalDate date = LocalDate.of(2025, 4, 28);

        em.flush();
        em.clear();

        Waiting waiting = new Waiting(member, date, time, theme, 1L);
        waitingRepository.save(waiting);

        assertThat(waitingRepository.findByDateAndThemeIdAndTimeId(
            date, theme.getId(), time.getId())).hasSize(1);
    }

    @Test
    @DisplayName("날짜, 테마, 시간으로 기존 예약 대기 수를 조회할 수 있다.")
    void countByDateAndThemeIdAndTimeId() {
        Member member1 = saveMember(1L);
        Member member2 = saveMember(2L);
        Theme theme = saveTheme(1L);
        ReservationTime time = saveTime(LocalTime.of(10, 0));
        LocalDate date = LocalDate.of(2025, 4, 28);

        em.flush();
        em.clear();

        Waiting waiting1 = new Waiting(member1, date, time, theme, 1L);
        Waiting waiting2 = new Waiting(member2, date, time, theme, 2L);
        waitingRepository.save(waiting1);
        waitingRepository.save(waiting2);

        assertThat(waitingRepository.countByDateAndThemeIdAndTimeId(
            date, theme.getId(), time.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("날짜, 시간, 테마, 사용자 정보와 일치하는 예약 대기가 존재하면 true를 반환한다.")
    void existsByDateAndTimeIdAndThemeIdAndMemberId() {
        Member member = saveMember(1L);
        Theme theme = saveTheme(1L);
        ReservationTime time = saveTime(LocalTime.of(10, 0));
        LocalDate date = LocalDate.of(2025, 4, 28);

        em.flush();
        em.clear();

        Waiting waiting = new Waiting(member, date, time, theme, 1L);
        waitingRepository.save(waiting);

        assertThat(waitingRepository.existsByDateAndTimeIdAndThemeIdAndMemberId(
            date, time.getId(), theme.getId(), member.getId())).isTrue();
    }

    @Test
    @DisplayName("날짜, 시간, 테마, 사용자 정보와 일치하는 예약 대기가 존재하면 false를 반환한다.")
    void notExistsByDateAndTimeIdAndThemeIdAndMemberId() {
        Member member = saveMember(1L);
        Theme theme = saveTheme(1L);
        ReservationTime time = saveTime(LocalTime.of(10, 0));
        LocalDate date = LocalDate.of(2025, 4, 28);

        em.flush();
        em.clear();

        Waiting waiting = new Waiting(member, date, time, theme, 1L);
        waitingRepository.save(waiting);

        assertThat(waitingRepository.existsByDateAndTimeIdAndThemeIdAndMemberId(
            date, time.getId(), theme.getId() + 1, member.getId())).isFalse();
    }

    private Member saveMember(Long tmp) {
        Member member = Member.createUser("이름" + tmp, "이메일" + tmp, "비밀번호" + tmp);
        memberRepository.save(member);

        return member;
    }

    private Theme saveTheme(Long tmp) {
        Theme theme = new Theme("이름" + tmp, "설명" + tmp, "썸네일" + tmp);
        themeRepository.save(theme);

        return theme;
    }

    private ReservationTime saveTime(LocalTime reservationTime) {
        ReservationTime time = new ReservationTime(reservationTime);
        reservationTimeRepository.save(time);

        return time;
    }
}
