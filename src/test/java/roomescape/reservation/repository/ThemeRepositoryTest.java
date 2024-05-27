package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.util.Fixture.ACTION_THEME;
import static roomescape.util.Fixture.HORROR_THEME;
import static roomescape.util.Fixture.JOJO;
import static roomescape.util.Fixture.KAKI;
import static roomescape.util.Fixture.RESERVATION_HOUR_10;
import static roomescape.util.Fixture.TODAY;

import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationTime;
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

    @Autowired
    EntityManager entityManager;

    @DisplayName("id로 엔티티를 찾는다.")
    @Test
    void findByIdTest() {
        Theme horrorTheme = themeRepository.save(HORROR_THEME);
        Theme findTheme = themeRepository.findById(horrorTheme.getId()).get();

        assertAll(
                () -> assertThat(findTheme.getName()).isEqualTo(horrorTheme.getName()),
                () -> assertThat(findTheme.getDescription()).isEqualTo(horrorTheme.getDescription()),
                () -> assertThat(findTheme.getThumbnail()).isEqualTo(horrorTheme.getThumbnail())
        );
    }

    @DisplayName("이름으로 엔티티를 찾는다.")
    @Test
    void findByIdNameTest() {
        Theme horrorTheme = themeRepository.save(HORROR_THEME);
        Theme findTheme = themeRepository.findByThemeName_Name(horrorTheme.getName()).get();

        assertAll(
                () -> assertThat(findTheme.getName()).isEqualTo(horrorTheme.getName()),
                () -> assertThat(findTheme.getDescription()).isEqualTo(horrorTheme.getDescription()),
                () -> assertThat(findTheme.getThumbnail()).isEqualTo(horrorTheme.getThumbnail())
        );
    }

    @DisplayName("전체 엔티티를 조회한다.")
    @Test
    void findAllTest() {
        themeRepository.save(HORROR_THEME);
        List<Theme> themes = themeRepository.findAll();

        assertThat(themes.size()).isEqualTo(1);
    }

    @DisplayName("테마 ID로 예약이 참조된 테마들을 찾는다.")
    @Test
    void findReservationInSameIdTest() {
        ReservationTime hour10 = reservationTimeRepository.save(RESERVATION_HOUR_10);

        Theme horrorTheme = themeRepository.save(HORROR_THEME);

        Member kaki = memberRepository.save(KAKI);

        reservationRepository.save(new Reservation(kaki, TODAY, horrorTheme, hour10, ReservationStatus.SUCCESS));
        boolean exist = !themeRepository.findThemesThatReservationReferById(horrorTheme.getId())
                .isEmpty();

        assertThat(exist).isTrue();
    }

    @DisplayName("주어진 기간 사이에 가장 많이 예약된 테마를 n개 조회한다..")
    @Test
    void findLimitOfPopularThemesDescBetweenPeriod() {
        ReservationTime hour10 = reservationTimeRepository.save(RESERVATION_HOUR_10);

        Theme horrorTheme = themeRepository.save(HORROR_THEME);
        Theme actionTheme = themeRepository.save(ACTION_THEME);

        Member kaki = memberRepository.save(KAKI);
        Member jojo = memberRepository.save(JOJO);

        reservationRepository.save(new Reservation(kaki, TODAY, horrorTheme, hour10, ReservationStatus.SUCCESS));
        reservationRepository.save(new Reservation(kaki, TODAY, actionTheme, hour10, ReservationStatus.SUCCESS));
        reservationRepository.save(new Reservation(jojo, TODAY, actionTheme, hour10, ReservationStatus.SUCCESS));

        List<Theme> popularThemes = themeRepository.findLimitOfPopularThemesDescBetweenPeriod(TODAY, TODAY, 2);

        assertAll(
                () -> assertThat(popularThemes.get(0).getName()).isEqualTo(actionTheme.getName()),
                () -> assertThat(popularThemes).hasSize(2)
        );
    }

    @DisplayName("id를 받아 삭제한다.")
    @Test
    void deleteTest() {
        Theme horrorTheme = themeRepository.save(HORROR_THEME);
        themeRepository.deleteById(horrorTheme.getId());

        List<Theme> themes = themeRepository.findAll();

        assertThat(themes.size()).isEqualTo(0);
    }
}
