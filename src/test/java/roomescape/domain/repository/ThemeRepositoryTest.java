package roomescape.domain.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

@Transactional
@SpringBootTest
class ThemeRepositoryTest {
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("테마를 저장한다")
    void save_ShouldThemePersistence() {
        //given
        Theme theme = new Theme("name", "description", "thumbnail");

        // when
        themeRepository.save(theme);

        // then
        Assertions.assertThat(themeRepository.findAll())
                .hasSize(1);
    }

    @Test
    @DisplayName("테마의 영속성을 id로 조회할 수 있다 - 영속화 되어있는 경우")
    void findById_ShouldGetPersistence() {
        //given
        Theme theme = new Theme("name", "description", "thumbnail");
        themeRepository.save(theme);

        // when & then
        Assertions.assertThat(themeRepository.findById(theme.getId()))
                .isPresent();
    }

    @Test
    @DisplayName("기간 따른 테마 조회가능 하다")
    void findThemesByPeriodWithLimit_ShouldReturnThemes() {
        // given
        Member member = new Member("name", "email", "password");
        memberRepository.save(member);
        ReservationTime time1 = new ReservationTime(LocalTime.of(1, 0));
        Theme theme1 = new Theme("name1", "description", "thumbnail");
        Theme theme2 = new Theme("name2", "description", "thumbnail");
        Theme theme3 = new Theme("name3", "description", "thumbnail");
        Theme theme4 = new Theme("name4", "description", "thumbnail");

        ReservationTime savedTime = reservationTimeRepository.save(time1);
        Theme savedTheme1 = themeRepository.save(theme1);
        Theme savedTheme2 = themeRepository.save(theme2);
        Theme savedTheme3 = themeRepository.save(theme3);
        Theme savedTheme4 = themeRepository.save(theme4);
        reservationRepository.save(new Reservation(LocalDate.of(2023, 2, 1), savedTime, savedTheme1));
        reservationRepository.save(new Reservation(LocalDate.of(2023, 2, 2), savedTime, savedTheme2));
        reservationRepository.save(new Reservation(LocalDate.of(2023, 2, 3), savedTime, savedTheme3));
        reservationRepository.save(new Reservation(LocalDate.of(2023, 2, 4), savedTime, savedTheme4));
        PageRequest pageRequest = PageRequest.of(0, 10);

        // when
        List<Theme> themesByPeriodWithLimit = themeRepository.findThemeByPeriodWithLimit(LocalDate.of(2023, 2, 2),
                LocalDate.of(2023, 2, 3), pageRequest);

        // then

        Assertions.assertThat(themesByPeriodWithLimit)
                .containsExactlyInAnyOrder(
                        savedTheme2,
                        savedTheme3
                );
    }

    @Test
    @DisplayName("테마의 영속성을 id로 조회할 수 있다 - 영속화 되어있지 않은 경우")
    void findById_ShouldGetPersistence_WhenPersistenceDoesNotExists() {
        // when & then
        Assertions.assertThat(themeRepository.findById(0L))
                .isEmpty();
    }

    @Test
    @DisplayName("테마의 영속성을 삭제한다")
    void delete_ShouldRemovePersistence() {
        // given
        Theme theme1 = new Theme("name", "description", "thumbnail");
        Theme theme2 = new Theme("name2", "description2", "thumbnail2");
        themeRepository.save(theme1);
        themeRepository.save(theme2);

        // when
        themeRepository.delete(theme1);

        // then
        Assertions.assertThat(themeRepository.findAll())
                .hasSize(1)
                .containsExactly(theme2);
    }

    @Test
    @DisplayName("모든 테마의 영속성을 삭제한다")
    void deleteAll_ShouldRemoveAllPersistence() {
        // given
        Theme theme1 = new Theme("name1", "description1", "thumbnail1");
        Theme theme2 = new Theme("name2", "description2", "thumbnail2");
        Theme theme3 = new Theme("name3", "description3", "thumbnail3");
        themeRepository.save(theme1);
        themeRepository.save(theme2);
        themeRepository.save(theme3);

        // when
        themeRepository.deleteAll();

        // then
        Assertions.assertThat(themeRepository.findAll())
                .isEmpty();
    }
}
