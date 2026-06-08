package roomescape;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willThrow;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import roomescape.common.exception.DuplicateEntityException;
import roomescape.dao.MemberDao;
import roomescape.dao.ReservationDao;
import roomescape.dao.ThemeDao;
import roomescape.dao.TimeDao;
import roomescape.dao.WaitingDao;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.Theme;
import roomescape.domain.Time;
import roomescape.domain.Waiting;
import roomescape.domain.vo.Name;
import roomescape.dto.request.ReservationPatchDto;
import roomescape.service.ReservationService;
import roomescape.service.WaitingService;

@SpringBootTest
@ActiveProfiles("test")
class ReservationPromotionRollbackTest {

    @Autowired
    private ReservationService reservationService;
    @Autowired
    private WaitingService waitingService;
    @Autowired
    private MemberDao memberDao;
    @Autowired
    private TimeDao timeDao;
    @Autowired
    private ThemeDao themeDao;
    @Autowired
    private WaitingDao waitingDao;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @MockitoSpyBean
    private ReservationDao reservationDao;

    private Long storeId;
    private Member reserver;
    private Member waitingMember;
    private Time time1;
    private Time time2;
    private Theme theme;
    private LocalDate date;
    private Long reservationId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO stores(name) VALUES (?)", "강남점");
        storeId = jdbcTemplate.queryForObject("SELECT id FROM stores WHERE name = ?", Long.class, "강남점");
        jdbcTemplate.update("INSERT INTO members(name, email, password, role) VALUES (?, ?, ?, ?)",
                "예약자", "reserver@test.com", "password", "USER");
        jdbcTemplate.update("INSERT INTO members(name, email, password, role) VALUES (?, ?, ?, ?)",
                "대기자", "waiting-member@test.com", "password", "USER");
        reserver = memberDao.findByEmail("reserver@test.com").orElseThrow();
        waitingMember = memberDao.findByEmail("waiting-member@test.com").orElseThrow();
        time1 = timeDao.insert(new Time(LocalTime.of(13, 0)));
        time2 = timeDao.insert(new Time(LocalTime.of(14, 0)));
        theme = themeDao.insert(new Theme(new Name("방탈출"), "http://url", "설명"));
        date = LocalDate.now().plusDays(1);

        Reservation reservation = reservationDao.insert(
                Reservation.createByAdmin(reserver, date, time1, theme, storeId));
        reservationId = reservation.getId();
        waitingDao.insert(new Waiting(waitingMember, date, time1, theme, storeId));
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
    @DisplayName("취소 중 대기 승격 저장이 실패하면 예약 취소와 대기 삭제가 모두 롤백된다")
    void rollsBackWhenPromotionFailsOnCancel() {
        willThrow(new DuplicateKeyException("승격 저장 실패"))
                .given(reservationDao).insert(any(Reservation.class));

        assertThatThrownBy(() -> reservationService.cancel(reservationId, reserver.getId()))
                .isInstanceOf(DuplicateEntityException.class);

        Reservation reservation = reservationDao.findById(reservationId).orElseThrow();
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.BOOKED);
        assertThat(waitingService.findAllByMemberId(waitingMember.getId())).hasSize(1);
        assertThat(reservationService.findAllByMemberId(waitingMember.getId())).isEmpty();
    }

    @Test
    @DisplayName("변경 중 이전 슬롯의 대기 승격 저장이 실패하면 예약 변경과 대기 삭제가 모두 롤백된다")
    void rollsBackWhenPromotionFailsOnUpdate() {
        willThrow(new DuplicateKeyException("승격 저장 실패"))
                .given(reservationDao).insert(any(Reservation.class));

        assertThatThrownBy(() -> reservationService.updateByUser(reservationId, reserver.getId(),
                new ReservationPatchDto(date, time2.getId())))
                .isInstanceOf(DuplicateEntityException.class);

        Reservation reservation = reservationDao.findById(reservationId).orElseThrow();
        assertThat(reservation.getTime().getId()).isEqualTo(time1.getId());
        assertThat(waitingService.findAllByMemberId(waitingMember.getId())).hasSize(1);
        assertThat(reservationService.findAllByMemberId(waitingMember.getId())).isEmpty();
    }
}
