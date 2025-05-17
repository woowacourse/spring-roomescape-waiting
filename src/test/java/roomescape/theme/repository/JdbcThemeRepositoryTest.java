package roomescape.theme.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.reservation.fixture.ReservationFixture;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.fixture.ReservationTimeFixture;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.dto.PopularThemeRequestDto;
import roomescape.theme.fixture.ThemeFixture;
import roomescape.user.MemberTestDataConfig;
import roomescape.user.domain.User;

@DataJpaTest
@Import({MemberTestDataConfig.class})
class JdbcThemeRepositoryTest {

    @Autowired
    private ThemeRepository repository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private MemberTestDataConfig memberTestDataConfig;

    @BeforeEach
    public void beforeEach() {
        User savedUser = memberTestDataConfig.getSavedUser();

        Theme savedTheme1 = repository.save(new Theme("name1", "dd1", "tt1"));
        Theme savedTheme2 = repository.save(new Theme("name2", "dd2", "tt2"));
        Theme savedTheme3 = repository.save(new Theme("name3", "dd3", "tt3"));
        Theme savedTheme4 = repository.save(new Theme("name4", "dd4", "tt4"));
        Theme savedTheme5 = repository.save(new Theme("name5", "dd5", "tt5"));
        Theme savedTheme6 = repository.save(new Theme("name6", "dd6", "tt6"));
        Theme savedTheme7 = repository.save(new Theme("name7", "dd7", "tt7"));
        Theme savedTheme8 = repository.save(new Theme("name8", "dd8", "tt8"));
        Theme savedTheme9 = repository.save(new Theme("name9", "dd9", "tt9"));
        Theme savedTheme10 = repository.save(new Theme("name10", "dd10", "tt10"));
        Theme savedTheme11 = repository.save(new Theme("name11", "dd11", "tt11"));

        ReservationTime savedTime2 = reservationTimeRepository.save(
                ReservationTimeFixture.create(LocalTime.of(11, 30)));
        ReservationTime savedTime1 = reservationTimeRepository.save(ReservationTimeFixture.create(LocalTime.of(11, 0)));

        // theme1을 사용한 예약 9개
        reservationRepository.save(
                ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(8), savedTime1, savedTheme1,
                        savedUser));
        reservationRepository.save(
                ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(7), savedTime1, savedTheme1,
                        savedUser));
        reservationRepository.save(
                ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(6), savedTime1, savedTheme1,
                        savedUser));
        reservationRepository.save(
                ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(5), savedTime1, savedTheme1,
                        savedUser));
        reservationRepository.save(
                ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(4), savedTime1, savedTheme1,
                        savedUser));
        reservationRepository.save(
                ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(3), savedTime1, savedTheme1,
                        savedUser));
        reservationRepository.save(
                ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(2), savedTime1, savedTheme1,
                        savedUser));
        reservationRepository.save(
                ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(1), savedTime1, savedTheme1,
                        savedUser));
        reservationRepository.save(
                ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(1), savedTime1, savedTheme1,
                        savedUser));

        // theme2를 사용한 예약 8개
        reservationRepository.save(
                ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(8), savedTime2, savedTheme2,
                        savedUser));
        reservationRepository.save(
                ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(7), savedTime2, savedTheme2,
                        savedUser));
        reservationRepository.save(
                ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(6), savedTime2, savedTheme2,
                        savedUser));
        reservationRepository.save(
                ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(5), savedTime2, savedTheme2,
                        savedUser));
        reservationRepository.save(
                ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(4), savedTime2, savedTheme2,
                        savedUser));
        reservationRepository.save(
                ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(3), savedTime2, savedTheme2,
                        savedUser));
        reservationRepository.save(
                ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(2), savedTime2, savedTheme2,
                        savedUser));
        reservationRepository.save(
                ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(1), savedTime2, savedTheme2,
                        savedUser));

// theme3: 예약 8개
        reservationRepository.save(
                ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(8), savedTime1, savedTheme3,
                        savedUser));
        reservationRepository.save(
                ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(7), savedTime1, savedTheme3,
                        savedUser));
        reservationRepository.save(
                ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(6), savedTime1, savedTheme3,
                        savedUser));
        reservationRepository.save(
                ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(5), savedTime1, savedTheme3,
                        savedUser));
        reservationRepository.save(
                ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(4), savedTime1, savedTheme3,
                        savedUser));
        reservationRepository.save(
                ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(3), savedTime1, savedTheme3,
                        savedUser));
        reservationRepository.save(
                ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(2), savedTime1, savedTheme3,
                        savedUser));
        reservationRepository.save(
                ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(1), savedTime1, savedTheme3,
                        savedUser));

// theme4: 예약 7개
        reservationRepository.save(
                ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(8), savedTime2, savedTheme4,
                        savedUser));
        reservationRepository.save(
                ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(7), savedTime2, savedTheme4,
                        savedUser));
        reservationRepository.save(
                ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(6), savedTime2, savedTheme4,
                        savedUser));
        reservationRepository.save(
                ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(5), savedTime2, savedTheme4,
                        savedUser));
        reservationRepository.save(
                ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(4), savedTime2, savedTheme4,
                        savedUser));
        reservationRepository.save(
                ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(3), savedTime2, savedTheme4,
                        savedUser));
        reservationRepository.save(
                ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(2), savedTime2, savedTheme4,
                        savedUser));

// theme5: 예약 6개
        reservationRepository.save(
                ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(8), savedTime1, savedTheme5,
                        savedUser));
        reservationRepository.save(
                ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(7), savedTime1, savedTheme5,
                        savedUser));
        reservationRepository.save(
                ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(6), savedTime1, savedTheme5,
                        savedUser));
        reservationRepository.save(
                ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(5), savedTime1, savedTheme5,
                        savedUser));
        reservationRepository.save(
                ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(4), savedTime1, savedTheme5,
                        savedUser));
        reservationRepository.save(
                ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(3), savedTime1, savedTheme5,
                        savedUser));
        reservationRepository.save(
                ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(3), savedTime1, savedTheme5,
                        savedUser));

        // theme6: 예약 5개
        reservationRepository.save(
                ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(8), savedTime2, savedTheme6,
                        savedUser));
        reservationRepository.save(
                ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(7), savedTime2, savedTheme6,
                        savedUser));
        reservationRepository.save(
                ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(6), savedTime2, savedTheme6,
                        savedUser));
        reservationRepository.save(
                ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(5), savedTime2, savedTheme6,
                        savedUser));
        reservationRepository.save(
                ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(4), savedTime2, savedTheme6,
                        savedUser));

        // theme7: 예약 4개
        reservationRepository.save(
                ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(8), savedTime1, savedTheme7,
                        savedUser));
        reservationRepository.save(
                ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(7), savedTime1, savedTheme7,
                        savedUser));
        reservationRepository.save(
                ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(6), savedTime1, savedTheme7,
                        savedUser));
        reservationRepository.save(
                ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(5), savedTime1, savedTheme7,
                        savedUser));

        // theme8: 예약 3개
        reservationRepository.save(
                ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(8), savedTime2, savedTheme8,
                        savedUser));
        reservationRepository.save(
                ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(7), savedTime2, savedTheme8,
                        savedUser));
        reservationRepository.save(
                ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(6), savedTime2, savedTheme8,
                        savedUser));

        // theme9: 예약 2개
        reservationRepository.save(
                ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(8), savedTime1, savedTheme9,
                        savedUser));
        reservationRepository.save(
                ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(7), savedTime1, savedTheme9,
                        savedUser));

        // theme10: 예약 1개
        reservationRepository.save(
                ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(8), savedTime2, savedTheme10,
                        savedUser));
    }

    @DisplayName("ID에 해당하는 테마를 조회할 수 있다")
    @Test
    void testMethodNameHere() {
        // given
        Theme theme = ThemeFixture.create("테마", "설명", "섬네일");
        Theme expectTheme = repository.save(theme);

        // when
        Optional<Theme> actualTheme = repository.findById(expectTheme.getId());

        // then
        SoftAssertions.assertSoftly(s -> {
                    s.assertThat(actualTheme).isPresent();
                    s.assertThat(actualTheme.get()).isEqualTo(expectTheme);
                }
        );
    }

    @Nested
    @DisplayName("인기 있는 테마를 갯수, 정렬 방식에 따라 처리할 수 있다.")
    class findThemesOrderByReservationTime {

        @DisplayName("dto 기본 값을 사용하여 조회한다.")
        @Test
        void findThemesOrderByReservationTime_expected() {

            // given
            LocalDate now = LocalDate.now();
            LocalDate from = now.plusDays(1);
            LocalDate to = now.plusDays(8);

            // when
            List<Theme> actual = repository.findThemesOrderByReservationCount(from, to,
                    new PopularThemeRequestDto().size());

            // then
            SoftAssertions.assertSoftly(s -> {
                        s.assertThat(actual).isNotNull();
                        s.assertThat(actual).hasSize(10);
                    }
            );
        }
    }
}
