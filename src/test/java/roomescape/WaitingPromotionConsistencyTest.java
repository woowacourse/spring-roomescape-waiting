package roomescape;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.willThrow;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import roomescape.dao.MemberDao;
import roomescape.dao.ReservationDao;
import roomescape.dao.ThemeDao;
import roomescape.dao.TimeDao;
import roomescape.dao.WaitingDao;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.Store;
import roomescape.domain.Theme;
import roomescape.domain.Time;
import roomescape.domain.Waiting;
import roomescape.domain.vo.Name;
import roomescape.dto.request.WaitingRequestDto;
import roomescape.service.ReservationService;
import roomescape.service.WaitingService;

@SpringBootTest
@ActiveProfiles("test")
class WaitingPromotionConsistencyTest {

    @Autowired
    private ReservationService reservationService;
    @Autowired
    private WaitingService waitingService;
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

    @MockitoSpyBean
    private WaitingDao waitingDao;

    private Member owner;
    private Member waiter;
    private Time time;
    private Theme theme;
    private Store store;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO stores(name) VALUES (?)", "강남점");
        Long storeId = jdbcTemplate.queryForObject("SELECT id FROM stores WHERE name = ?", Long.class, "강남점");
        store = new Store(storeId, "강남점");

        jdbcTemplate.update(
                "INSERT INTO members(name, email, password, role) VALUES (?, ?, ?, ?)",
                "예약자", "owner@test.com", "password", "USER");
        owner = memberDao.findByEmail("owner@test.com").orElseThrow();
        jdbcTemplate.update(
                "INSERT INTO members(name, email, password, role) VALUES (?, ?, ?, ?)",
                "대기자", "waiter@test.com", "password", "USER");
        waiter = memberDao.findByEmail("waiter@test.com").orElseThrow();

        time = timeDao.insert(new Time(LocalTime.of(13, 0)));
        theme = themeDao.insert(new Theme(new Name("방탈출"), "http://url", "설명"));
        reservation = reservationDao.insert(
                Reservation.createByAdmin(owner, LocalDate.now().plusDays(1), time, theme, store));
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM waitings");
        jdbcTemplate.update("DELETE FROM reservations");
        jdbcTemplate.update("DELETE FROM times");
        jdbcTemplate.update("DELETE FROM themes");
        jdbcTemplate.update("DELETE FROM members");
        jdbcTemplate.update("DELETE FROM stores");
    }

    @Test
    @DisplayName("예약 취소 중 대기 승격이 실패하면, 취소와 승격이 모두 롤백되어 데이터 일관성이 유지된다")
    void rollbackWhenPromotionFails() {
        Waiting waiting = waitingService.create(
                new WaitingRequestDto(reservation.getDate(), time.getId(), theme.getId(), store.getId()),
                waiter);
        // 승격 흐름(예약 insert → 대기 delete)에서 delete 단계가 실패하는 상황을 주입한다.
        willThrow(new RuntimeException("승격 중 강제 실패")).given(waitingDao).delete(anyLong());

        assertThatThrownBy(() -> reservationService.cancel(reservation.getId(), owner))
                .isInstanceOf(RuntimeException.class);

        // 1. 예약 취소가 롤백되어 여전히 BOOKED 상태로 남아 있다.
        Reservation reloaded = reservationDao.findById(reservation.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(ReservationStatus.BOOKED);
        // 2. 승격으로 insert 되려던 예약이 롤백되어, 대기자는 예약을 갖지 않는다.
        assertThat(reservationDao.findAllByMemberId(waiter.getId())).isEmpty();
        // 3. 대기가 그대로 남아 있다.
        assertThat(waitingDao.existsById(waiting.getId())).isTrue();
    }
}
