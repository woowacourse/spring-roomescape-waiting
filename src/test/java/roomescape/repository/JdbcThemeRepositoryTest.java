package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.repository.rowmapper.MemberRowMapper;
import roomescape.repository.rowmapper.ReservationRowMapper;
import roomescape.repository.rowmapper.ReservationTimeRowMapper;
import roomescape.repository.rowmapper.ThemeRowMapper;

@TestExecutionListeners(value = {
        DatabaseCleanupListener.class,
        DependencyInjectionTestExecutionListener.class
})
@JdbcTest
class JdbcThemeRepositoryTest {

    @Autowired
    DataSource dataSource;
    private JdbcThemeRepository themeRepository;
    private JdbcReservationRepository reservationRepository;
    private JdbcReservationTimeRepository timeRepository;
    private JdbcMemberRepository memberRepository;

    private final Theme theme1 = new Theme(null, "공포", "난이도 1", "hi.jpg");
    private final Theme theme2 = new Theme(null, "우테코", "난이도 2", "hi.jpg");

    private final ReservationTime time1 = new ReservationTime(null, "10:30");

    private final Member member1 = new Member(null, "t1@t1.com", "123", "러너덕", "MEMBER");
    private final Member member2 = new Member(null, "t2@t2.com", "124", "재즈", "MEMBER");
    private final Member member3 = new Member(null, "t3@t3.com", "125", "재즈덕", "MEMBER");

    @BeforeEach
    void setUp() {
        themeRepository = new JdbcThemeRepository(dataSource, new ThemeRowMapper());
        timeRepository = new JdbcReservationTimeRepository(dataSource, new ReservationTimeRowMapper());
        reservationRepository = new JdbcReservationRepository(dataSource, new ReservationRowMapper());
        memberRepository = new JdbcMemberRepository(dataSource, new MemberRowMapper());
    }

    @DisplayName("저장된 모든 테마 정보를 가져온다.")
    @Test
    void find_all_themes() {
        themeRepository.insertTheme(theme1);
        themeRepository.insertTheme(theme2);

        List<Theme> allReservationTimes = themeRepository.findAllThemes();

        assertThat(allReservationTimes.size()).isEqualTo(2);
    }

    @DisplayName("테마를 저장한다.")
    @Test
    void save_theme() {
        Theme theme = themeRepository.insertTheme(theme1);

        assertAll(
                () -> assertThat(theme.getId()).isEqualTo(1),
                () -> assertThat(theme.getName()).isEqualTo("공포"),
                () -> assertThat(theme.getDescription()).isEqualTo("난이도 1"),
                () -> assertThat(theme.getThumbnail()).isEqualTo("hi.jpg")
        );
    }

    @Test
    @DisplayName("테마를 id로 삭제한다.")
    void delete_theme_by_id() {
        themeRepository.insertTheme(theme1);
        int beforeSize = themeRepository.findAllThemes().size();

        themeRepository.deleteThemeById(1L);
        int afterSize = themeRepository.findAllThemes().size();

        assertAll(
                () -> assertThat(beforeSize).isEqualTo(1),
                () -> assertThat(afterSize).isEqualTo(0)
        );
    }

    @Test
    @DisplayName("테이블에 테마 존재 여부를 판단한다.")
    void is_Exist_theme() {
        themeRepository.insertTheme(theme1);

        boolean exist = themeRepository.isThemeExistsById(1L);
        boolean notExist = themeRepository.isThemeExistsById(2L);

        assertAll(
                () -> assertThat(exist).isTrue(),
                () -> assertThat(notExist).isFalse()
        );
    }

    @Test
    @DisplayName("특정 기간안에 예약이 많은 순서대로 정렬하여 특정 개수만큼 테마 정보를 가져온다.")
    void find_top_themes_desc_by_reservation_count_between_dates() {
        Theme savedTheme1 = themeRepository.insertTheme(theme1);
        Theme savedTheme2 = themeRepository.insertTheme(theme2);
        ReservationTime savedTime1 = timeRepository.insertReservationTime(time1);
        Member savedMember1 = memberRepository.insertMember(member1);
        Member savedMember2 = memberRepository.insertMember(member2);
        Member savedMember3 = memberRepository.insertMember(member3);

        Reservation reservation1 = new Reservation(
                null, savedMember1, savedTheme1, new ReservationDate("2024-05-01"), savedTime1);
        Reservation reservation2 = new Reservation(
                null, savedMember2, savedTheme2, new ReservationDate("2024-05-02"), savedTime1);
        Reservation reservation3 = new Reservation(
                null, savedMember3, savedTheme2, new ReservationDate("2024-05-03"), savedTime1);
        reservationRepository.insertReservation(reservation1);
        reservationRepository.insertReservation(reservation2);
        reservationRepository.insertReservation(reservation3);

        List<Theme> themes = themeRepository.findTopThemesDescendingByReservationCount(
                "2024-05-01",
                "2024-05-05",
                2
        );

        assertAll(
                () -> assertThat(themes.get(0).getId()).isEqualTo(2L),
                () -> assertThat(themes.get(0).getName()).isEqualTo("우테코"),
                () -> assertThat(themes.get(1).getId()).isEqualTo(1L),
                () -> assertThat(themes.get(1).getName()).isEqualTo("공포")
        );
    }
}
