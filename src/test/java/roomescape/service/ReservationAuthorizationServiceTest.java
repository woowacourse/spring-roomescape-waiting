package roomescape.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import roomescape.common.exception.HiddenResourceException;
import roomescape.common.exception.UnauthorizedException;
import roomescape.dao.MemberDao;
import roomescape.dao.ReservationDao;
import roomescape.dao.ThemeDao;
import roomescape.dao.TimeDao;
import roomescape.dao.jdbc.MemberJdbcDao;
import roomescape.dao.jdbc.ReservationJdbcDao;
import roomescape.dao.jdbc.ThemeJdbcDao;
import roomescape.dao.jdbc.TimeJdbcDao;
import roomescape.domain.Member;
import roomescape.domain.MemberRole;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.domain.Time;
import roomescape.domain.vo.Name;

@JdbcTest
@Import({ReservationAuthorizationService.class, ReservationJdbcDao.class, TimeJdbcDao.class, ThemeJdbcDao.class,
        MemberJdbcDao.class})
@ActiveProfiles("test")
class ReservationAuthorizationServiceTest {

    @Autowired
    private ReservationAuthorizationService authorizationService;
    @Autowired
    private ReservationDao reservationDao;
    @Autowired
    private MemberDao memberDao;
    @Autowired
    private TimeDao timeDao;
    @Autowired
    private ThemeDao themeDao;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Member member;
    private Member manager;
    private Reservation reservation;
    private Long storeId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO stores(name) VALUES (?)", "강남점");
        storeId = jdbcTemplate.queryForObject("SELECT id FROM stores WHERE name = ?", Long.class, "강남점");
        jdbcTemplate.update(
                "INSERT INTO members(name, email, password, role) VALUES (?, ?, ?, ?)",
                "유저", "user@test.com", "password", "USER"
        );
        jdbcTemplate.update(
                "INSERT INTO members(name, email, password, role, store_id) VALUES (?, ?, ?, ?, ?)",
                "강남매니저", "manager@test.com", "password", "MANAGER", storeId
        );
        member = memberDao.findByEmail("user@test.com").orElseThrow();
        manager = memberDao.findByEmail("manager@test.com").orElseThrow();
        Time time = timeDao.insert(new Time(LocalTime.of(13, 0)));
        Theme theme = themeDao.insert(new Theme(new Name("방탈출"), "http://thumbnail_url", "설명"));
        reservation = reservationDao.insert(
                Reservation.createByAdmin(member, LocalDate.now().plusDays(1), time, theme, storeId));
    }

    @Nested
    class MemberAccess {

        @Test
        @DisplayName("예약 소유자는 예약에 접근할 수 있다")
        void allowsOwner() {
            assertThatCode(() -> authorizationService.validateMemberCanAccess(member.getId(), reservation.getId()))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("예약 소유자가 아니면 숨김 예외를 반환한다")
        void throwsWhenNotOwner() {
            assertThatThrownBy(() -> authorizationService.validateMemberCanAccess(-1L, reservation.getId()))
                    .isInstanceOf(HiddenResourceException.class);
        }
    }

    @Nested
    class ManagerAccess {

        @Test
        @DisplayName("매니저는 자신의 매장 예약에 접근할 수 있다")
        void allowsSameStoreManager() {
            assertThatCode(() -> authorizationService.validateManagerCanAccess(manager.getStoreId(), reservation.getId()))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("매니저는 다른 매장 예약에 접근할 수 없다")
        void throwsWhenDifferentStore() {
            Member otherStoreManager = new Member(99L, "홍대매니저", "manager2@test.com", "password",
                    MemberRole.MANAGER, storeId + 1);

            assertThatThrownBy(() -> authorizationService.validateManagerCanAccess(otherStoreManager.getStoreId(), reservation.getId()))
                    .isInstanceOf(UnauthorizedException.class);
        }
    }
}
