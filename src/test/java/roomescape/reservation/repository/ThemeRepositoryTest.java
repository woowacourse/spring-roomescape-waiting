package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
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
    @DisplayName("최근 1주일을 기준하여 예약이 많은 순으로 10개의 테마를 조회한다.")
    void findTopTenThemesDescendingOfLastWeekTest() {
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.now()));

        Theme theme1 = themeRepository.save(
                new Theme("공포", "무서운 테마", "https://i.pinimg.com/236x.jpg")
        );
        Theme theme2 = themeRepository.save(
                new Theme("액션", "액션 테마", "https://i.pinimg.com/236x.jpg")
        );
        Theme theme3 = themeRepository.save(
                new Theme("SF", "미래 테마", "https://i.pinimg.com/236x.jpg")
        );

        Member member1 = memberRepository.save(new Member("호기", "hogi@email.com", "ㅁㄴㅇ"));
        Member member2 = memberRepository.save(new Member("카키", "kaki@email.com", "ㅁㄴㅇ"));
        Member member3 = memberRepository.save(new Member("솔라", "solar@email.com", "ㅁㄴㅇ"));
        Member member4 = memberRepository.save(new Member("네오", "neo@email.com", "ㅁㄴㅇ"));

        reservationRepository.save(new Reservation(member1, LocalDate.now(), theme1, reservationTime));
        reservationRepository.save(new Reservation(member2, LocalDate.now(), theme2, reservationTime));
        reservationRepository.save(new Reservation(member3, LocalDate.now(), theme2, reservationTime));
        reservationRepository.save(new Reservation(member4, LocalDate.now(), theme3, reservationTime));

        LocalDate today = LocalDate.now();
        LocalDate sevenDaysBefore = today.minusDays(7);
        List<Theme> themes = themeRepository.findPopularThemesWithPagination(sevenDaysBefore, today, PageRequest.of(0,
                10));

        assertAll(
                () -> assertThat(themes.get(0).getName()).isEqualTo("액션"),
                () -> assertThat(themes.size()).isEqualTo(3)
        );
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
