package roomescape.repository.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.entity.Member;
import roomescape.entity.Reservation;
import roomescape.entity.ReservationTime;
import roomescape.entity.Theme;

@DataJpaTest
class JpaThemeRepositoryTest {

    @PersistenceContext
    private EntityManager em;
    @Autowired
    private JpaThemeRepository themeRepository;
    @Autowired
    private JpaReservationTimeRepository reservationTimeRepository;
    @Autowired
    private JpaReservationRepository reservationRepository;
    @Autowired
    private JpaMemberRepository memberRepository;

    @Test
    @DisplayName("일주일 동안의 인기 테마를 검색할 수 있다.")
    void findTopReservedThemesInPeriodWithLimit() {
        Member member = saveMember(1L);
        Theme theme1 = saveTheme(1L);
        Theme theme2 = saveTheme(2L);
        ReservationTime time1 = saveTime(LocalTime.of(10, 0));
        ReservationTime time2 = saveTime(LocalTime.of(11, 0));
        LocalDate date = LocalDate.of(2025, 4, 28);

        em.flush();
        em.clear();

        Reservation reservation1 = new Reservation(member, date, time1, theme1);
        Reservation reservation2 = new Reservation(member, date, time2, theme1);
        Reservation reservation3 = new Reservation(member, date, time1, theme2);
        reservationRepository.save(reservation1);
        reservationRepository.save(reservation2);
        reservationRepository.save(reservation3);

        List<Theme> themes = themeRepository.findTopRankByDateBetween(date.minusDays(7), date);

        assertThat(themes.getFirst().getId()).isEqualTo(theme1.getId());
    }

    @Test
    @DisplayName("해당 시간이 없다면 true를 반환한다.")
    void existThemeByName() {
        Theme theme = new Theme("이름", "설명", "썸네일");
        themeRepository.save(theme);

        assertThat(themeRepository.existsByName(theme.getName())).isTrue();
    }

    @Test
    @DisplayName("해당 시간이 없다면 false를 반환한다.")
    void notExistThemeByName() {
        Theme theme = new Theme("이름", "설명", "썸네일");

        assertThat(themeRepository.existsByName(theme.getName())).isFalse();
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
