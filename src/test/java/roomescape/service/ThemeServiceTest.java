package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.dto.request.ThemeRequest;
import roomescape.entity.Member;
import roomescape.entity.Reservation;
import roomescape.entity.ReservationTime;
import roomescape.entity.Theme;
import roomescape.exception.custom.DuplicatedException;
import roomescape.repository.jpa.JpaMemberRepository;
import roomescape.repository.jpa.JpaReservationRepository;
import roomescape.repository.jpa.JpaReservationTimeRepository;
import roomescape.repository.jpa.JpaThemeRepository;

@DataJpaTest
public class ThemeServiceTest {

    @PersistenceContext
    private EntityManager em;
    @Autowired
    private JpaReservationTimeRepository reservationTimeRepository;
    @Autowired
    private JpaMemberRepository memberRepository;
    @Autowired
    private JpaReservationRepository reservationRepository;
    @Autowired
    private JpaThemeRepository themeRepository;

    private ThemeService themeService;

    @BeforeEach
    void setUp() {
        themeService = new ThemeService(themeRepository);
    }

    @Test
    @DisplayName("최근 일주일의 탑10 테마를 조회할 수 있다.")
    void findTopReservedThemes() {
        Member member = saveMember(1L);
        ReservationTime time = saveTime(LocalTime.of(10, 0));
        LocalDate date = LocalDate.now(ZoneId.of("Asia/Seoul"));

        for(long i=1; i<=11; i++) {
            Theme theme = saveTheme(i);
            Reservation reservation = new Reservation(member, date, time, theme);
            reservationRepository.save(reservation);
        }

        em.flush();
        em.clear();

        assertThat(themeService.findTopReservedThemes()).hasSize(10);
    }

    @Test
    @DisplayName("테마를 추가할 수 있다.")
    void addTheme() {
        ThemeRequest request = new ThemeRequest("이름", "설명", "썸네일");

        assertThat(themeService.addTheme(request)).isNotNull();
    }

    @Test
    @DisplayName("중복된 테마를 추가하면 예외가 발생한다.")
    void addDuplicatedTheme() {
        Theme theme = new Theme("이름", "설명", "썸네일");
        themeRepository.save(theme);

        ThemeRequest request = new ThemeRequest("이름", "설명", "썸네일");

        assertThatThrownBy(() -> themeService.addTheme(request))
            .isInstanceOf(DuplicatedException.class)
            .hasMessageContaining("theme");
    }

    private Member saveMember(Long tmp) {
        Member member = Member.createUser("이름" + tmp, "이메일" + tmp, "비밀번호" + tmp);
        memberRepository.save(member);

        return member;
    }

    private Theme saveTheme(Long tmp) {
        Theme theme = new Theme("이름" + tmp, "설명" + tmp, "썸네일" + tmp);
        themeRepository.save(theme);

        return theme;
    }

    private ReservationTime saveTime(LocalTime reservationTime) {
        ReservationTime time = new ReservationTime(reservationTime);
        reservationTimeRepository.save(time);

        return time;
    }
}
