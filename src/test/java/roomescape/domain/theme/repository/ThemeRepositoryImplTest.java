package roomescape.domain.theme.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.domain.reservation.domain.reservation.ReservationStatus.RESERVED;
import static roomescape.fixture.LocalDateFixture.BEFORE_ONE_DAYS_DATE;
import static roomescape.fixture.LocalDateFixture.BEFORE_THREE_DAYS_DATE;
import static roomescape.fixture.LocalDateFixture.BEFORE_TWO_DAYS_DATE;
import static roomescape.fixture.MemberFixture.ADMIN_MEMBER;
import static roomescape.fixture.MemberFixture.MEMBER_MEMBER;
import static roomescape.fixture.MemberFixture.NULL_ID_MEMBER;
import static roomescape.fixture.ReservationTimeFixture.NULL_ID_RESERVATION_TIME;
import static roomescape.fixture.ReservationTimeFixture.TEN_RESERVATION_TIME;
import static roomescape.fixture.ThemeFixture.DUMMY_THEME;
import static roomescape.fixture.ThemeFixture.NULL_ID_DUMMY_THEME;
import static roomescape.fixture.TimestampFixture.TIMESTAMP_BEFORE_ONE_YEAR;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.RepositoryTest;
import roomescape.domain.member.repository.JpaMemberRepository;
import roomescape.domain.reservation.domain.reservation.Reservation;
import roomescape.domain.reservation.repository.reservation.JpaReservationRepository;
import roomescape.domain.reservation.repository.reservationTime.JpaReservationTimeRepository;
import roomescape.domain.theme.domain.Theme;

class ThemeRepositoryImplTest extends RepositoryTest {

    @Autowired
    private JpaReservationRepository jpaReservationRepository;
    @Autowired
    private JpaMemberRepository jpaMemberRepository;
    @Autowired
    private JpaReservationTimeRepository jpaReservationTimeRepository;
    @Autowired
    private JpaThemeRepository jpaThemeRepository;

    private ThemeRepository themeRepository;

    @BeforeEach
    void setUp() {
        themeRepository = new ThemeRepositoryImpl(jpaThemeRepository);
        jpaMemberRepository.save(NULL_ID_MEMBER);
        jpaThemeRepository.save(NULL_ID_DUMMY_THEME);
        jpaReservationTimeRepository.save(NULL_ID_RESERVATION_TIME);
        jpaReservationRepository.save(
                new Reservation(null, BEFORE_ONE_DAYS_DATE, TEN_RESERVATION_TIME, DUMMY_THEME, MEMBER_MEMBER, RESERVED,
                        TIMESTAMP_BEFORE_ONE_YEAR));
    }

    @AfterEach
    void setDown() {
        jpaReservationRepository.deleteAll();
    }

    @DisplayName("인기 테마 목록을 불러올 수 있습니다.")
    @Test
    void should_read_theme_ranking() {
        Theme saveTheme = jpaThemeRepository.save(new Theme(null, "테마2", "테마2설명", "url"));
        jpaReservationRepository.save(
                new Reservation(null, BEFORE_TWO_DAYS_DATE, TEN_RESERVATION_TIME, saveTheme, ADMIN_MEMBER, RESERVED,
                        TIMESTAMP_BEFORE_ONE_YEAR));
        jpaReservationRepository.save(
                new Reservation(null, BEFORE_ONE_DAYS_DATE, TEN_RESERVATION_TIME, saveTheme, ADMIN_MEMBER, RESERVED,
                        TIMESTAMP_BEFORE_ONE_YEAR));
        jpaReservationRepository.save(
                new Reservation(null, BEFORE_THREE_DAYS_DATE, TEN_RESERVATION_TIME, saveTheme, ADMIN_MEMBER, RESERVED,
                        TIMESTAMP_BEFORE_ONE_YEAR));

        List<Theme> themeRaking = themeRepository.findThemeOrderByReservationCount();

        assertAll(
                () -> assertThat(themeRaking).hasSize(2),
                () -> assertThat(themeRaking.get(0).getName()).isEqualTo("테마2")
        );

    }
}
