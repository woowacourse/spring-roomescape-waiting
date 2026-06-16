package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import roomescape.common.exception.BusinessRuleViolationException;
import roomescape.common.exception.DuplicateEntityException;
import roomescape.common.exception.EntityNotFoundException;
import roomescape.dao.MemberDao;
import roomescape.dao.ReservationDao;
import roomescape.dao.ThemeDao;
import roomescape.dao.TimeDao;
import roomescape.dao.jdbc.MemberJdbcDao;
import roomescape.dao.jdbc.ReservationJdbcDao;
import roomescape.dao.jdbc.StoreJdbcDao;
import roomescape.dao.jdbc.ThemeJdbcDao;
import roomescape.dao.jdbc.TimeJdbcDao;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.store.Store;
import roomescape.domain.theme.Theme;
import roomescape.domain.time.Time;
import roomescape.domain.theme.ThemeService;
import roomescape.dto.request.PopularThemeRequestDto;
import roomescape.dto.request.ThemeRequestDto;
import roomescape.dto.response.AvailableTimeResponseDto;
import roomescape.dto.response.TimeResponseDto;

@JdbcTest
@Import({ThemeService.class, ThemeJdbcDao.class, ReservationJdbcDao.class, TimeJdbcDao.class, MemberJdbcDao.class, StoreJdbcDao.class})
@ActiveProfiles("test")
class ThemeServiceTest {

    @Autowired
    private ThemeService themeService;
    @Autowired
    private ThemeDao themeDao;
    @Autowired
    private TimeDao timeDao;
    @Autowired
    private ReservationDao reservationDao;
    @Autowired
    private MemberDao memberDao;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Member member;
    private Store store;
    private ThemeRequestDto requestDto1;
    private ThemeRequestDto requestDto2;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO stores(name) VALUES (?)", "강남점");
        Long storeId = jdbcTemplate.queryForObject("SELECT id FROM stores WHERE name = ?", Long.class, "강남점");
        store = new Store(storeId, "강남점");
        jdbcTemplate.update(
                "INSERT INTO members(name, email, password, role) VALUES (?, ?, ?, ?)",
                "유저", "user@test.com", "password", "USER"
        );
        member = memberDao.findByEmail("user@test.com").orElseThrow();
        requestDto1 = new ThemeRequestDto("테마1", "http://thumbnail_url", "설명1", 30000L);
        requestDto2 = new ThemeRequestDto("테마2", "http://thumbnail_url", "설명2", 35000L);
    }

    @Nested
    class FindAll {

        @Test
        @DisplayName("테마가 없으면 빈 목록을 반환한다")
        void returnsEmptyList() {
            assertThat(themeService.findAll()).isEmpty();
        }

        @Test
        @DisplayName("전체 테마 목록을 반환한다")
        void returnsAllThemes() {
            List<Theme> saved = new ArrayList<>();
            saved.add(themeService.create(requestDto1));
            saved.add(themeService.create(requestDto2));

            assertThat(themeService.findAll()).isEqualTo(saved);
        }
    }

    @Nested
    class FindById {

        @Test
        @DisplayName("존재하는 id로 테마를 조회한다")
        void returnsThemeById() {
            Theme saved = themeService.create(requestDto1);

            assertThat(themeService.findById(saved.getId())).isEqualTo(saved);
        }

        @Test
        @DisplayName("존재하지 않는 id를 조회하면 예외를 반환한다")
        void throwsWhenIdNotFound() {
            assertThatThrownBy(() -> themeService.findById(-1L))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    class Create {

        @Test
        @DisplayName("유효한 요청으로 테마를 생성한다")
        void createsTheme() {
            Theme saved = themeService.create(requestDto1);

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getName().getValue()).isEqualTo(requestDto1.name());
        }

        @Test
        @DisplayName("중복된 테마 이름으로 생성하면 예외를 반환한다")
        void throwsWhenDuplicateName() {
            themeService.create(requestDto1);

            assertThatThrownBy(() -> themeService.create(requestDto1))
                    .isInstanceOf(DuplicateEntityException.class);
        }
    }

    @Nested
    class Delete {

        @Test
        @DisplayName("테마를 삭제한다")
        void deletesTheme() {
            Theme saved = themeService.create(requestDto1);
            assertThat(themeDao.existsById(saved.getId())).isTrue();

            themeService.delete(saved.getId());

            assertThat(themeDao.existsById(saved.getId())).isFalse();
        }

        @Test
        @DisplayName("존재하지 않는 id를 삭제하면 예외를 반환한다")
        void throwsWhenDeletingNonExistentId() {
            assertThatThrownBy(() -> themeService.delete(-1L))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("예약이 있는 테마는 삭제할 수 없다")
        void throwsWhenThemeHasReservation() {
            Theme savedTheme = themeService.create(requestDto1);
            Time savedTime = timeDao.insert(new Time(LocalTime.of(13, 0)));
            reservationDao.insert(Reservation.createByAdmin(member, LocalDate.now(), savedTime, savedTheme, store));

            Long id = savedTheme.getId();
            assertThatThrownBy(() -> themeService.delete(id))
                    .isInstanceOf(BusinessRuleViolationException.class);
        }
    }

    @Nested
    class FindAvailableTimesById {

        @Test
        @DisplayName("테마의 이용 가능한 시간 목록을 반환한다")
        void returnsAvailableTimes() {
            Theme savedTheme = themeService.create(requestDto1);
            Time bookedTime = timeDao.insert(new Time(LocalTime.of(13, 0)));
            Time availableTime = timeDao.insert(new Time(LocalTime.of(14, 0)));
            LocalDate date = LocalDate.of(2026, 5, 10);
            reservationDao.insert(Reservation.createByAdmin(member, date, bookedTime, savedTheme, store));

            List<AvailableTimeResponseDto> result = themeService.findAvailableTimesById(savedTheme.getId(), date);

            assertThat(result).containsExactlyInAnyOrder(
                    new AvailableTimeResponseDto(TimeResponseDto.from(bookedTime), true),
                    new AvailableTimeResponseDto(TimeResponseDto.from(availableTime), false)
            );
        }
    }

    @Nested
    class FindPopulars {

        @Test
        @DisplayName("인기 테마 목록을 limit만큼 반환한다")
        void returnsPopularThemesByLimit() {
            Theme popularTheme = themeService.create(requestDto1);
            themeService.create(requestDto2);
            Time savedTime = timeDao.insert(new Time(LocalTime.of(13, 0)));
            reservationDao.insert(Reservation.createByAdmin(member, LocalDate.now(), savedTime, popularTheme, store));

            List<Theme> result = themeService.findPopulars(new PopularThemeRequestDto(1, 7));

            assertThat(result)
                    .hasSize(1)
                    .containsExactly(popularTheme);
        }

        @Test
        @DisplayName("예약 수가 많은 테마가 먼저 반환된다")
        void returnsThemesOrderedByReservationCount() {
            Theme lessPopular = themeService.create(requestDto2);
            Theme morePopular = themeService.create(requestDto1);
            Time savedTime1 = timeDao.insert(new Time(LocalTime.of(13, 0)));
            Time savedTime2 = timeDao.insert(new Time(LocalTime.of(14, 0)));

            reservationDao.insert(Reservation.createByAdmin(member, LocalDate.now(), savedTime1, morePopular, store));
            reservationDao.insert(Reservation.createByAdmin(member, LocalDate.now(), savedTime2, morePopular, store));
            reservationDao.insert(Reservation.createByAdmin(member, LocalDate.now(), savedTime1, lessPopular, store));

            List<Theme> result = themeService.findPopulars(new PopularThemeRequestDto(2, 7));

            assertThat(result).containsExactly(morePopular, lessPopular);
        }

        @Test
        @DisplayName("기간 밖의 예약은 집계하지 않는다")
        void excludesReservationsOutsidePeriod() {
            Theme inPeriodTheme = themeService.create(requestDto1);
            Theme outOfPeriodTheme = themeService.create(requestDto2);
            Time savedTime = timeDao.insert(new Time(LocalTime.of(13, 0)));

            reservationDao.insert(Reservation.createByAdmin(member, LocalDate.now().minusDays(1), savedTime, outOfPeriodTheme, store));

            List<Theme> result = themeService.findPopulars(new PopularThemeRequestDto(2, 1));

            assertThat(result).containsExactly(inPeriodTheme, outOfPeriodTheme);
        }
    }
}
