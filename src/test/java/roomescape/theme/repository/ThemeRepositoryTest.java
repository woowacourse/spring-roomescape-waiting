package roomescape.theme.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;
import roomescape.config.TestConfig;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.fixture.TestFixture;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;

@DataJpaTest
@Import(TestConfig.class)
@TestPropertySource(properties = {
        "spring.sql.init.data-locations="
})
class ThemeRepositoryTest {

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void findAll() {
        themeRepository.save(Theme.of("추리", "셜록 추리 게임 with Danny", "image.png"));
        themeRepository.save(Theme.of("논리", "논리 게임 with Vector", "image.png"));

        assertThat(themeRepository.findAll().size()).isEqualTo(2);
    }

    @Test
    void findPopularThemes() {
        Member member = TestFixture.makeMember();
        memberRepository.save(member);

        Theme theme1 = Theme.of("추리", "셜록 홈즈: 실종된 보석의 비밀", "sherlock_jewel.png");
        themeRepository.save(theme1);
        Theme theme2 = Theme.of("논리", "양자 코드: 암호 해독 게임", "quantum_code.png");
        themeRepository.save(theme2);
        Theme theme3 = themeRepository.save(Theme.of("공포", "저주받은 병원: 13호실의 비밀", "cursed_hospital.png"));
        Theme theme4 = Theme.of("모험", "잃어버린 문명: 마야의 유산", "maya_civilization.png");
        themeRepository.save(theme4);
        Theme theme5 = Theme.of("판타지", "드래곤의 동굴: 마법의 보물", "dragon_cave.png");
        themeRepository.save(theme5);
        Theme theme6 = Theme.of("역사", "타임머신: 조선 왕조의 비밀", "joseon_dynasty.png");
        themeRepository.save(theme6);
        Theme theme7 = Theme.of("SF", "화성 기지: 레드 플래닛의 음모", "mars_base.png");
        themeRepository.save(theme7);
        Theme theme8 = Theme.of("스릴러", "연쇄 살인마의 게임", "serial_killer_game.png");
        themeRepository.save(theme8);
        Theme theme9 = Theme.of("코미디", "유머 수사대: 웃음을 찾아서", "humor_detective.png");
        themeRepository.save(theme9);
        Theme theme10 = Theme.of("액션", "비밀 요원: 마지막 미션", "secret_agent.png");
        themeRepository.save(theme10);
        Theme theme11 = Theme.of("신비", "신화의 미궁: 그리스 신들의 시험", "greek_gods.png");
        themeRepository.save(theme11);
        Theme theme12 = Theme.of("음악", "멜로디 탐정: 잃어버린 노래", "lost_melody.png");
        themeRepository.save(theme12);

        ReservationTime reservationTime = TestFixture.makeReservationTime(LocalTime.of(10, 0));
        reservationTimeRepository.save(reservationTime);
        ReservationTime reservationTime2 = TestFixture.makeReservationTime(LocalTime.of(11, 0));
        reservationTimeRepository.save(reservationTime2);

        LocalDate futureDate = LocalDate.now();
        reservationRepository.save(
                TestFixture.makeReservation(futureDate.minusDays(1), reservationTime, member, theme1));

        reservationRepository.save(
                TestFixture.makeReservation(futureDate.minusDays(2), reservationTime, member, theme2));
        reservationRepository.save(
                TestFixture.makeReservation(futureDate.minusDays(2), reservationTime2, member, theme2));

        reservationRepository.save(
                TestFixture.makeReservation(futureDate.minusDays(3), reservationTime, member, theme4));
        reservationRepository.save(
                TestFixture.makeReservation(futureDate.minusDays(3), reservationTime2, member, theme5));
        reservationRepository.save(
                TestFixture.makeReservation(futureDate.minusDays(4), reservationTime, member, theme6));
        reservationRepository.save(
                TestFixture.makeReservation(futureDate.minusDays(4), reservationTime2, member, theme7));
        reservationRepository.save(
                TestFixture.makeReservation(futureDate.minusDays(5), reservationTime, member, theme8));
        reservationRepository.save(
                TestFixture.makeReservation(futureDate.minusDays(5), reservationTime2, member, theme9));
        reservationRepository.save(
                TestFixture.makeReservation(futureDate.minusDays(6), reservationTime, member, theme10));
        reservationRepository.save(
                TestFixture.makeReservation(futureDate.minusDays(6), reservationTime2, member, theme11));

        List<Theme> themes = themeRepository.findPopularThemes(futureDate.minusDays(7), futureDate.minusDays(1),
                PageRequest.of(0, 10)).getContent();

        Assertions.assertAll(
                () -> assertThat(themes.size()).isEqualTo(10),
                () -> assertThat(themes).doesNotContain(theme3, theme12),
                () -> assertThat(themes.getFirst()).isEqualTo(theme2)
        );
    }
}
