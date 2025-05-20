package roomescape.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class JpaThemeRepositoryTest {

    @Autowired
    private JpaThemeRepository themeRepository;

    @Autowired
    private JpaReservationRepository reservationRepository;

    @Autowired
    private JpaReservationTimeRepository reservationTimeRepository;

    @Autowired
    private JpaMemberRepository memberRepository;

    @Test
    @DisplayName("테마를 저장한다.")
    void save() {
        // given
        Theme theme = new Theme(null, "테마1", "설명1", "썸네일1");

        // when
        Theme savedTheme = themeRepository.save(theme);

        // then
        assertThat(savedTheme.getId()).isNotNull();
        assertThat(savedTheme.getName()).isEqualTo("테마1");
        assertThat(savedTheme.getDescription()).isEqualTo("설명1");
        assertThat(savedTheme.getThumbnail()).isEqualTo("썸네일1");
    }

    @Test
    @DisplayName("인기테마를 조회한다")
    void findPopular() {
        // given
        Theme theme1 = themeRepository.save(new Theme(null, "테마1", "설명1", "썸네일1"));
        Theme theme2 = themeRepository.save(new Theme(null, "테마2", "설명2", "썸네일2"));
        
        Member member = memberRepository.save(Member.createWithoutId("이름", "email@email.com", Role.USER, "password"));
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(null, LocalTime.of(10, 0)));

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate lastWeek = today.minusDays(7);

        // theme1: 오늘 2건, 어제 1건
        reservationRepository.save(Reservation.createWithoutId(member, today, time, theme1));
        reservationRepository.save(Reservation.createWithoutId(member, today, time, theme1));
        reservationRepository.save(Reservation.createWithoutId(member, yesterday, time, theme1));

        // theme2: 오늘 1건
        reservationRepository.save(Reservation.createWithoutId(member, today, time, theme2));

        // when
        List<Theme> popularThemes = themeRepository.findPopular(
                lastWeek,
                today.plusDays(1),
                PageRequest.of(0, 10)
        );

        // then
        assertThat(popularThemes).hasSize(2);
        assertThat(popularThemes.get(0).getName()).isEqualTo("테마1");
        assertThat(popularThemes.get(1).getName()).isEqualTo("테마2");
    }
}
