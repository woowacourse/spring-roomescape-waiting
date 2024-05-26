package roomescape.domain.reservation.detail;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.exception.DomainNotFoundException;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.fixture.Fixture;

@DataJpaTest
class ThemeRepositoryTest {

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    @DisplayName("특정 기간 중 예약이 많은 순으로 인기 테마를 조회한다.")
    void findPopularThemes() {
        LocalDate startDate = LocalDate.of(2024, 4, 6);
        LocalDate endDate = LocalDate.of(2024, 4, 7);
        int limit = 2;

        LocalDate includedDate = LocalDate.of(2024, 4, 6);
        LocalDate excludedDate = LocalDate.of(2024, 4, 8);

        Member member = memberRepository.save(Fixture.MEMBER_1);

        Theme theme1 = themeRepository.save(Fixture.THEME_1);
        Theme theme2 = themeRepository.save(Fixture.THEME_2);

        ReservationTime time1 = reservationTimeRepository.save(Fixture.RESERVATION_TIME_1);
        ReservationTime time2 = reservationTimeRepository.save(Fixture.RESERVATION_TIME_2);

        reservationRepository.save(new Reservation(new ReservationDetail(includedDate, time1, theme2), member));
        reservationRepository.save(new Reservation(new ReservationDetail(includedDate, time2, theme2), member));
        reservationRepository.save(new Reservation(new ReservationDetail(includedDate, time2, theme1), member));
        reservationRepository.save(new Reservation(new ReservationDetail(excludedDate, time1, theme1), member));
        reservationRepository.save(new Reservation(new ReservationDetail(excludedDate, time2, theme1), member));

        List<Theme> popularThemes = themeRepository.findPopularThemes(startDate, endDate, limit);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(popularThemes).hasSize(2);

            softly.assertThat(popularThemes.get(0).getId()).isEqualTo(theme2.getId());
            softly.assertThat(popularThemes.get(0).getName()).isEqualTo(theme2.getName());

            softly.assertThat(popularThemes.get(1).getId()).isEqualTo(theme1.getId());
            softly.assertThat(popularThemes.get(1).getName()).isEqualTo(theme1.getName());
        });
    }

    @Test
    @DisplayName("아이디로 테마를 조회한다.")
    void getById() {
        Theme savedTheme = themeRepository.save(new Theme("테마1", "테마1 설명", "https://example1.com"));

        Theme theme = themeRepository.getById(savedTheme.getId());

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(theme.getId()).isNotNull();
            softly.assertThat(theme.getName()).isEqualTo("테마1");
            softly.assertThat(theme.getDescription()).isEqualTo("테마1 설명");
            softly.assertThat(theme.getThumbnail()).isEqualTo("https://example1.com");
        });
    }

    @Test
    @DisplayName("아이디로 테마를 조회하고, 없을 경우 예외를 발생시킨다.")
    void getByIdWhenNotExist() {
        themeRepository.save(new Theme("테마1", "테마1 설명", "https://example1.com"));

        assertThatThrownBy(() -> themeRepository.getById(-1L))
                .isInstanceOf(DomainNotFoundException.class);
    }
}
