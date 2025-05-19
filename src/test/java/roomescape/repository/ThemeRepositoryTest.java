package roomescape.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ThemeRepositoryTest {

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Test
    @DisplayName("테마를 저장하고 ID로 조회할 수 있다")
    void saveAndFindById() {
        // given
        Theme theme = new Theme(null, "테마1", "테마1 설명", "thumbnail1.jpg");

        // when
        Theme savedTheme = themeRepository.save(theme);
        Optional<Theme> foundTheme = themeRepository.findById(savedTheme.getId());

        // then
        assertThat(foundTheme).isPresent();
        assertThat(foundTheme.get().getName()).isEqualTo("테마1");
        assertThat(foundTheme.get().getDescription()).isEqualTo("테마1 설명");
        assertThat(foundTheme.get().getThumbnail()).isEqualTo("thumbnail1.jpg");
    }

    @Test
    @DisplayName("모든 테마를 조회할 수 있다")
    void findAll() {
        // given
        Theme theme1 = new Theme(null, "테마1", "테마1 설명", "thumbnail1.jpg");
        Theme theme2 = new Theme(null, "테마2", "테마2 설명", "thumbnail2.jpg");
        themeRepository.saveAll(List.of(theme1, theme2));

        // when
        List<Theme> themes = themeRepository.findAll();

        // then
        assertThat(themes).hasSize(2);
        assertThat(themes).extracting("name").containsExactlyInAnyOrder("테마1", "테마2");
    }

    @Test
    @DisplayName("테마를 삭제할 수 있다")
    void delete() {
        // given
        Theme theme = new Theme(null, "테마1", "테마1 설명", "thumbnail1.jpg");
        Theme savedTheme = themeRepository.save(theme);

        // when
        themeRepository.delete(savedTheme);
        Optional<Theme> foundTheme = themeRepository.findById(savedTheme.getId());

        // then
        assertThat(foundTheme).isEmpty();
    }

    @Test
    @DisplayName("인기 테마를 조회할 수 있다")
    void findPopular() {
        // given
        Theme theme1 = new Theme(null, "테마1", "테마1 설명", "thumbnail1.jpg");
        Theme theme2 = new Theme(null, "테마2", "테마2 설명", "thumbnail2.jpg");
        Theme theme3 = new Theme(null, "테마3", "테마3 설명", "thumbnail3.jpg");
        
        themeRepository.saveAll(List.of(theme1, theme2, theme3));
        
        Member member = Member.createWithoutId("사용자", "user@example.com", Role.USER, "password");
        memberRepository.save(member);
        
        ReservationTime time = new ReservationTime(null, LocalTime.of(10, 0));
        reservationTimeRepository.save(time);
        
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(30);
        LocalDate endDate = today.plusDays(1);
        
        Reservation r1 = Reservation.createWithoutId(member, today.minusDays(5), time, theme1);
        Reservation r2 = Reservation.createWithoutId(member, today.minusDays(10), time, theme1);
        Reservation r3 = Reservation.createWithoutId(member, today.minusDays(15), time, theme1);
        Reservation r4 = Reservation.createWithoutId(member, today.minusDays(7), time, theme2);
        Reservation r5 = Reservation.createWithoutId(member, today.minusDays(12), time, theme2);
        Reservation r6 = Reservation.createWithoutId(member, today.minusDays(20), time, theme3);
        
        reservationRepository.saveAll(List.of(r1, r2, r3, r4, r5, r6));

        // when
        List<Theme> popularThemes = themeRepository.findPopular(startDate, endDate);

        // then
        assertThat(popularThemes).hasSize(3);
        assertThat(popularThemes.get(0).getId()).isEqualTo(theme1.getId());
        assertThat(popularThemes.get(1).getId()).isEqualTo(theme2.getId());
        assertThat(popularThemes.get(2).getId()).isEqualTo(theme3.getId());
    }
}
