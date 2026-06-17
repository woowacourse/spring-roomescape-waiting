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
import roomescape.dao.jdbc.ThemeJdbcDao;
import roomescape.dao.jdbc.TimeJdbcDao;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.store.Store;
import roomescape.domain.theme.Theme;
import roomescape.domain.time.Time;
import roomescape.domain.time.TimeService;
import roomescape.common.vo.Name;
import roomescape.dto.request.TimeRequestDto;

@JdbcTest
@Import({TimeService.class, TimeJdbcDao.class, ReservationJdbcDao.class, ThemeJdbcDao.class, MemberJdbcDao.class})
@ActiveProfiles("test")
class TimeServiceTest {

    @Autowired
    private TimeService timeService;
    @Autowired
    private TimeDao timeDao;
    @Autowired
    private ReservationDao reservationDao;
    @Autowired
    private ThemeDao themeDao;
    @Autowired
    private MemberDao memberDao;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Member member;
    private TimeRequestDto timeRequestDto1;
    private TimeRequestDto timeRequestDto2;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update(
                "INSERT INTO members(name, email, password, role) VALUES (?, ?, ?, ?)",
                "유저", "user@test.com", "password", "USER"
        );
        member = memberDao.findByEmail("user@test.com").orElseThrow();
        timeRequestDto1 = new TimeRequestDto(LocalTime.of(10, 0));
        timeRequestDto2 = new TimeRequestDto(LocalTime.of(12, 0));
    }

    @Nested
    class FindAll {

        @Test
        @DisplayName("시간이 없으면 빈 목록을 반환한다")
        void returnsEmptyList() {
            assertThat(timeService.findAll()).isEmpty();
        }

        @Test
        @DisplayName("전체 시간 목록을 반환한다")
        void returnsAllTimes() {
            List<Time> saved = new ArrayList<>();
            saved.add(timeService.create(timeRequestDto1));
            saved.add(timeService.create(timeRequestDto2));

            assertThat(timeService.findAll()).isEqualTo(saved);
        }
    }

    @Nested
    class FindById {

        @Test
        @DisplayName("존재하는 id로 시간을 조회한다")
        void returnsTimeById() {
            Time saved = timeService.create(timeRequestDto1);

            assertThat(timeService.findById(saved.getId())).isEqualTo(saved);
        }

        @Test
        @DisplayName("존재하지 않는 id를 조회하면 예외를 반환한다")
        void throwsWhenIdNotFound() {
            assertThatThrownBy(() -> timeService.findById(-1L))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    class Create {

        @Test
        @DisplayName("유효한 요청으로 시간을 생성한다")
        void createsTime() {
            Time saved = timeService.create(timeRequestDto1);

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getStartAt()).isEqualTo(timeRequestDto1.startAt());
        }

        @Test
        @DisplayName("중복된 시간을 생성하면 예외를 반환한다")
        void throwsWhenDuplicateTime() {
            timeService.create(timeRequestDto1);

            assertThatThrownBy(() -> timeService.create(timeRequestDto1))
                    .isInstanceOf(DuplicateEntityException.class);
        }
    }

    @Nested
    class Delete {

        @Test
        @DisplayName("시간을 삭제한다")
        void deletesTime() {
            Time saved = timeService.create(timeRequestDto1);
            timeService.delete(saved.getId());

            assertThat(timeDao.existsById(saved.getId())).isFalse();
        }

        @Test
        @DisplayName("존재하지 않는 id를 삭제하면 예외를 반환한다")
        void throwsWhenDeletingNonExistentId() {
            assertThatThrownBy(() -> timeService.delete(-1L))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("예약이 있는 시간은 삭제할 수 없다")
        void throwsWhenTimeHasReservation() {
            Time savedTime = timeService.create(timeRequestDto1);
            Theme savedTheme = themeDao.insert(new Theme(new Name("테마"), "http://thumbnail_url", "설명"));
            jdbcTemplate.update("INSERT INTO stores(name) VALUES (?)", "강남점");
            Long storeId = jdbcTemplate.queryForObject("SELECT id FROM stores WHERE name = ?", Long.class, "강남점");
            reservationDao.insert(Reservation.createByAdmin(member, LocalDate.now(), savedTime, savedTheme, new Store(storeId, "강남점")));

            Long id = savedTime.getId();
            assertThatThrownBy(() -> timeService.delete(id))
                    .isInstanceOf(BusinessRuleViolationException.class);
        }
    }
}
