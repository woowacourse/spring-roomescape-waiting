package roomescape.theme.infrastructure;

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
import roomescape.common.config.TestConfig;
import roomescape.fixture.TestFixture;
import roomescape.member.domain.Member;
import roomescape.member.infrastructure.MemberRepository;
import roomescape.reservationslot.infrastructure.ReservationSlotRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.infrastructure.ReservationTimeRepository;
import roomescape.theme.domain.Theme;

@DataJpaTest
@Import(TestConfig.class)
class ThemeRepositoryTest {

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationSlotRepository reservationSlotRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void findPopular() {
        Member member = TestFixture.makeMember();
        memberRepository.save(member);

        Theme theme1 = new Theme("추리", "셜록 홈즈: 실종된 보석의 비밀", "sherlock_jewel.png");
        themeRepository.save(theme1);
        Theme theme2 = new Theme("논리", "양자 코드: 암호 해독 게임", "quantum_code.png");
        themeRepository.save(theme2);
        Theme theme3 = themeRepository.save(new Theme("공포", "저주받은 병원: 13호실의 비밀", "cursed_hospital.png"));
        Theme theme4 = new Theme("모험", "잃어버린 문명: 마야의 유산", "maya_civilization.png");
        themeRepository.save(theme4);
        Theme theme5 = new Theme("판타지", "드래곤의 동굴: 마법의 보물", "dragon_cave.png");
        themeRepository.save(theme5);
        Theme theme6 = new Theme("역사", "타임머신: 조선 왕조의 비밀", "joseon_dynasty.png");
        themeRepository.save(theme6);
        Theme theme7 = new Theme("SF", "화성 기지: 레드 플래닛의 음모", "mars_base.png");
        themeRepository.save(theme7);
        Theme theme8 = new Theme("스릴러", "연쇄 살인마의 게임", "serial_killer_game.png");
        themeRepository.save(theme8);
        Theme theme9 = new Theme("코미디", "유머 수사대: 웃음을 찾아서", "humor_detective.png");
        themeRepository.save(theme9);
        Theme theme10 = new Theme("액션", "비밀 요원: 마지막 미션", "secret_agent.png");
        themeRepository.save(theme10);
        Theme theme11 = new Theme("신비", "신화의 미궁: 그리스 신들의 시험", "greek_gods.png");
        themeRepository.save(theme11);
        Theme theme12 = new Theme("음악", "멜로디 탐정: 잃어버린 노래", "lost_melody.png");
        themeRepository.save(theme12);

        ReservationTime reservationTime = TestFixture.makeReservationTime(LocalTime.of(10, 0));
        reservationTimeRepository.save(reservationTime);
        ReservationTime reservationTime2 = TestFixture.makeReservationTime(LocalTime.of(11, 0));
        reservationTimeRepository.save(reservationTime2);

        LocalDate futureDate = LocalDate.now().plusDays(7);
        reservationSlotRepository.save(
                TestFixture.makeConfirmedReservation(futureDate.minusDays(1), reservationTime, member, theme1));

        reservationSlotRepository.save(
                TestFixture.makeConfirmedReservation(futureDate.minusDays(2), reservationTime, member, theme2));
        reservationSlotRepository.save(
                TestFixture.makeConfirmedReservation(futureDate.minusDays(2), reservationTime2, member, theme2));

        reservationSlotRepository.save(
                TestFixture.makeConfirmedReservation(futureDate.minusDays(3), reservationTime, member, theme4));
        reservationSlotRepository.save(
                TestFixture.makeConfirmedReservation(futureDate.minusDays(3), reservationTime2, member, theme5));
        reservationSlotRepository.save(
                TestFixture.makeConfirmedReservation(futureDate.minusDays(4), reservationTime, member, theme6));
        reservationSlotRepository.save(
                TestFixture.makeConfirmedReservation(futureDate.minusDays(4), reservationTime2, member, theme7));
        reservationSlotRepository.save(
                TestFixture.makeConfirmedReservation(futureDate.minusDays(5), reservationTime, member, theme8));
        reservationSlotRepository.save(
                TestFixture.makeConfirmedReservation(futureDate.minusDays(5), reservationTime2, member, theme9));
        reservationSlotRepository.save(
                TestFixture.makeConfirmedReservation(futureDate.minusDays(6), reservationTime, member, theme10));
        reservationSlotRepository.save(
                TestFixture.makeConfirmedReservation(futureDate.minusDays(6), reservationTime2, member, theme11));

        List<Theme> themes = themeRepository.findPopular(futureDate.minusDays(6), futureDate,
                PageRequest.of(0, 10)).getContent();

        Assertions.assertAll(
                () -> assertThat(themes.size()).isEqualTo(10),
                () -> assertThat(themes).doesNotContain(theme3, theme12),
                () -> assertThat(themes.getFirst()).isEqualTo(theme2)
        );
    }
}
