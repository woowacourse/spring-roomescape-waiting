package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Theme;

@DataJpaTest
class ThemeRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("id로 엔티티를 찾는다.")
    void findByIdTest() {
        Theme theme = new Theme("공포", "무서운 테마", "https://i.pinimg.com/236x.jpg");
        Long themeId = themeRepository.save(theme).getId();
        Theme findTheme = themeRepository.findById(themeId).get();

        assertThat(findTheme.getId()).isEqualTo(themeId);
    }

    @Test
    @DisplayName("이름으로 엔티티를 찾는다.")
    void findByIdNameTest() {
        Theme theme = new Theme("공포", "무서운 테마", "https://i.pinimg.com/236x.jpg");
        Long themeId = themeRepository.save(theme).getId();
        Theme findTheme = themeRepository.findByThemeName(theme.getName()).get();

        assertThat(findTheme.getId()).isEqualTo(themeId);
    }

    @Test
    @DisplayName("전체 엔티티를 조회한다.")
    void findAllTest() {
        Theme theme1 = new Theme("공포", "무서운 테마", "https://i.pinimg.com/236x.jpg");
        Theme theme2 = new Theme("SF", "미래 테마", "https://i.pinimg.com/123x.jpg");
        themeRepository.save(theme1);
        themeRepository.save(theme2);
        List<Theme> themes = themeRepository.findAll();

        assertThat(themes.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("id를 받아 삭제한다.")
    void deleteTest() {
        Theme theme = new Theme("공포", "무서운 테마", "https://i.pinimg.com/236x.jpg");
        Long themeId = themeRepository.save(theme).getId();
        themeRepository.deleteById(themeId);
        List<Theme> themes = themeRepository.findAll();

        assertThat(themes.size()).isEqualTo(0);
    }
}
