package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.util.Fixture.HORROR_DESCRIPTION;
import static roomescape.util.Fixture.HORROR_THEME_NAME;
import static roomescape.util.Fixture.KAKI_EMAIL;
import static roomescape.util.Fixture.KAKI_NAME;
import static roomescape.util.Fixture.KAKI_PASSWORD;
import static roomescape.util.Fixture.THUMBNAIL;
import static roomescape.util.Fixture.TODAY;

import jakarta.persistence.EntityManager;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberName;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Description;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Status;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.ThemeName;

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

    @Autowired
    EntityManager entityManager;

    @DisplayName("id로 엔티티를 찾는다.")
    @Test
    void findByIdTest() {
        Theme theme = new Theme(new ThemeName(HORROR_THEME_NAME), new Description(HORROR_DESCRIPTION), THUMBNAIL);
        Theme savedTheme = themeRepository.save(theme);
        Theme findTheme = themeRepository.findById(savedTheme.getId()).get();

        assertAll(
                () -> assertThat(findTheme.getName()).isEqualTo(HORROR_THEME_NAME),
                () -> assertThat(findTheme.getDescription()).isEqualTo(HORROR_DESCRIPTION),
                () -> assertThat(findTheme.getThumbnail()).isEqualTo(THUMBNAIL)
        );
    }

    @DisplayName("이름으로 엔티티를 찾는다.")
    @Test
    void findByIdNameTest() {
        Theme theme = new Theme(new ThemeName(HORROR_THEME_NAME), new Description(HORROR_DESCRIPTION), THUMBNAIL);
        Theme savedTheme = themeRepository.save(theme);
        Theme findTheme = themeRepository.findByThemeName_Name(savedTheme.getName()).get();

        assertAll(
                () -> assertThat(findTheme.getName()).isEqualTo(HORROR_THEME_NAME),
                () -> assertThat(findTheme.getDescription()).isEqualTo(HORROR_DESCRIPTION),
                () -> assertThat(findTheme.getThumbnail()).isEqualTo(THUMBNAIL)
        );
    }

    @DisplayName("전체 엔티티를 조회한다.")
    @Test
    void findAllTest() {
        Theme theme = new Theme(new ThemeName(HORROR_THEME_NAME), new Description(HORROR_DESCRIPTION), THUMBNAIL);
        themeRepository.save(theme);
        List<Theme> themes = themeRepository.findAll();

        assertThat(themes.size()).isEqualTo(1);
    }

    @DisplayName("테마 ID로 예약이 참조된 테마들을 찾는다.")
    @Test
    void findReservationInSameIdTest() {
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.now()));

        Theme theme = themeRepository.save(
                new Theme(
                        new ThemeName(HORROR_THEME_NAME),
                        new Description(HORROR_DESCRIPTION),
                        THUMBNAIL
                )
        );

        Member member = memberRepository.save(Member.createMemberByUserRole(new MemberName(KAKI_NAME), KAKI_EMAIL, KAKI_PASSWORD));

        reservationRepository.save(new Reservation(member, TODAY, theme, reservationTime, Status.SUCCESS));
        boolean exist = !themeRepository.findThemesThatReservationReferById(theme.getId()).isEmpty();

        assertThat(exist).isTrue();
    }

    @DisplayName("id를 받아 삭제한다.")
    @Test
    void deleteTest() {
        Theme theme = themeRepository.save(
                new Theme(
                        new ThemeName(HORROR_THEME_NAME),
                        new Description(HORROR_DESCRIPTION),
                        THUMBNAIL
                )
        );
        themeRepository.deleteById(theme.getId());
        List<Theme> themes = themeRepository.findAll();

        assertThat(themes.size()).isEqualTo(0);
    }
}
