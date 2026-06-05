package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.repository.TimeRepository;

@SpringBootTest
@Sql("/truncate.sql")
class ReservationTransactionTest {

    private static final LocalDateTime START = LocalDateTime.of(2030, 6, 1, 10, 0);
    private static final LocalDateTime END = LocalDateTime.of(2030, 6, 1, 12, 0);

    @Autowired
    ReservationService reservationService;

    @Autowired
    ThemeRepository themeRepository;

    @Autowired
    TimeRepository timeRepository;

    @MockitoSpyBean
    ReservationRepository reservationRepository;

    @DisplayName("예약 취소 시, 대기가 있으면 대기가 RESERVED로 승격되고 예약은 삭제된다.")
    @Test
    void cancelForUser_정상_동작() {
        // given
        ReservationTime time = timeRepository.save(START, END);
        Theme theme = themeRepository.save(new Theme("테마", "설명", "https://img.test/a.png"));
        Reservation reserved = reservationRepository.save(
                new Reservation("라이", time, theme, Status.RESERVED, LocalDateTime.now()));
        Reservation waiting = reservationRepository.save(
                new Reservation("어셔", time, theme, Status.WAITING, LocalDateTime.now()));

        // when
        reservationService.cancelForUser(reserved.getId(), "라이");

        // then
        assertThat(reservationRepository.findById(reserved.getId())).isEmpty();
        Reservation promoted = reservationRepository.findById(waiting.getId()).get();
        assertThat(promoted.getStatus()).isEqualTo(Status.RESERVED);
        assertThat(promoted.getId()).isEqualTo(waiting.getId());
    }

    @DisplayName("deleteById가 실패하는 경우, promoteToReserved가 롤백되어 대기 상태가 유지된다.")
    @Test
    void cancelForUser_중간_실패_시_롤백() {
        // given
        ReservationTime time = timeRepository.save(START, END);
        Theme theme = themeRepository.save(new Theme("테마", "설명", "https://img.test/a.png"));
        Reservation reserved = reservationRepository.save(
                new Reservation("라이", time, theme, Status.RESERVED, LocalDateTime.now()));
        Reservation waiting = reservationRepository.save(
                new Reservation("어셔", time, theme, Status.WAITING, LocalDateTime.now()));

        doThrow(new RuntimeException("강제 실패"))
                .when(reservationRepository).deleteById(anyLong());

        // when
        // 예외 강제 발생
        assertThatThrownBy(() -> reservationService.cancelForUser(reserved.getId(), "라이"))
                .isInstanceOf(RuntimeException.class);

        // then
        // promoteToReserved가 롤백되어 대기 상태 그대로
        Reservation stillWaiting = reservationRepository.findById(waiting.getId()).get();
        assertThat(stillWaiting.getStatus()).isEqualTo(Status.WAITING);
        assertThat(stillWaiting.getId()).isEqualTo(waiting.getId());
    }
}
