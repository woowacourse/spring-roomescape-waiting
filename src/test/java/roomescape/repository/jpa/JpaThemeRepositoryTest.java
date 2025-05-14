package roomescape.repository.jpa;

import static org.assertj.core.api.Assertions.assertThat;

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

    @Autowired
    private JpaThemeRepository jpaThemeRepository;
    @Autowired
    private JpaReservationTimeRepository jpaReservationTimeRepository;
    @Autowired
    private JpaReservationRepository jpaReservationRepository;

    @Test
    @DisplayName("일주일 동안의 인기 테마를 검색할 수 있다.")
    void findTopReservedThemesInPeriodWithLimit() {
        Member member = Member.createUser("이름", "이메일", "비밀번호");
        Theme theme = new Theme("이름", "설명", "썸네일");
        ReservationTime time = new ReservationTime(LocalTime.of(10, 0), null);
        LocalDate date = LocalDate.of(2025, 4, 28);

        Reservation reservation = new Reservation(member, date, time, theme);

        jpaThemeRepository.save(theme);
        jpaReservationTimeRepository.save(time);
        jpaReservationRepository.save(reservation);

        List<Theme> themes = jpaThemeRepository.findTop10ByDateBetween(date.minusDays(7), date);

        assertThat(themes.getLast().getId()).isEqualTo(1);
    }

    @Test
    @DisplayName("해당 시간이 없다면 true를 반환한다.")
    void existThemeByName() {
        Theme theme = new Theme("이름", "설명", "썸네일");
        jpaThemeRepository.save(theme);

        assertThat(jpaThemeRepository.existsByName(theme.getName())).isTrue();
    }

    @Test
    @DisplayName("해당 시간이 없다면 false를 반환한다.")
    void notExistThemeByName() {
        Theme theme = new Theme("이름", "설명", "썸네일");

        assertThat(jpaThemeRepository.existsByName(theme.getName())).isFalse();
    }
}
