package roomescape.reservation.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.repository.TimeRepository;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ReservationAtomicityTest {

    @Autowired
    private ReservationService reservationService;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private TimeRepository timeRepository;

    @MockitoSpyBean
    private ReservationRepository reservationRepository;

    private long reservedId;
    private long waitingId;
    private long newTimeId;
    private long oldTimeId;

    @BeforeEach
    void setUp() {
        Theme theme = themeRepository.save(
                new Theme("원자성테마", "테스트", "https://test.test/x.png"));

        LocalDateTime oldStart = LocalDateTime.now().plusDays(7);
        ReservationTime oldTime = timeRepository.save(oldStart, oldStart.plusHours(2));
        oldTimeId = oldTime.getId();

        LocalDateTime newStart = LocalDateTime.now().plusDays(8);
        ReservationTime newTime = timeRepository.save(
                newStart, newStart.plusHours(2));
        newTimeId = newTime.getId();
        Reservation reserved = reservationRepository.save(
                new Reservation("예약중임", oldTime, theme, Status.RESERVED,
                        LocalDateTime.now().minusHours(2)));
        reservedId = reserved.getId();

        Reservation waiting = reservationRepository.save(
                new Reservation("예약대기중임", oldTime, theme, Status.WAITING,
                        LocalDateTime.now().minusHours(1)));
        waitingId = waiting.getId();

        clearInvocations(reservationRepository);
    }

    @DisplayName("cancelForUser 중 deleteById가 실패하면 예약 승격도 함께 롤백 됨")
    @Test
    void cancelForUser_원자성_롤백_테스트() {
        // given
        doThrow(new RuntimeException("db 에러 발생"))
                .when(reservationRepository).deleteById(reservedId);

        // when & then
        assertThatThrownBy(() -> reservationService.cancelForUser(reservedId, "예약중임"))
                .isInstanceOf(RuntimeException.class);

        verify(reservationRepository).update(any());
        String waitingStatus = jdbcTemplate.queryForObject(
                "SELECT status FROM reservation WHERE id = ?",
                String.class, waitingId);
        assertThat(Status.valueOf(waitingStatus)).isEqualTo(Status.WAITING);

        Integer reservedExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation WHERE id = ?",
                Integer.class, reservedId);
        assertThat(reservedExists).isEqualTo(1);
    }

    @DisplayName("사용자 예약 update를 실패하면 자동 승격도 함께 롤백된다.")
    @Test
    void update_원자성_롤백_테스트() {
        // given
        doThrow(new RuntimeException("DB 장애"))
                .when(reservationRepository).update(argThat(r ->
                        r != null && r.getId() != null && r.getId() == reservedId));

        // when
        assertThatThrownBy(() -> reservationService.update(reservedId, newTimeId, "예약중임"))
                .isInstanceOf(RuntimeException.class);

        verify(reservationRepository).update(argThat(r ->
                r != null && r.getId() != null && r.getId() == waitingId
                        && r.getStatus() == Status.RESERVED));

        String status = jdbcTemplate.queryForObject(
                "SELECT status FROM reservation WHERE id = ?", String.class, waitingId);
        assertThat(Status.valueOf(status)).isEqualTo(Status.WAITING);

        Long timeId = jdbcTemplate.queryForObject(
                "SELECT time_id FROM reservation WHERE id = ?", Long.class, reservedId);
        assertThat(timeId).isEqualTo(oldTimeId);
    }
}
