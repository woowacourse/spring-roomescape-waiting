package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import roomescape.DatabaseInitializer;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ReservationWaitingRepository;
import roomescape.repository.ThemeRepository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Theme;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
public class ReservationTransactionTest {

    @Autowired
    private DatabaseInitializer databaseInitializer;

    @Autowired
    private ReservationService reservationService;

    @MockitoSpyBean
    private ReservationWaitingService waitingService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationWaitingRepository waitingDao;

    @Autowired
    private ReservationTimeRepository timeDao;

    @Autowired
    private ThemeRepository themeRepository;

    @BeforeEach
    void setUp() {
        databaseInitializer.clear();
    }

    @Test
    void 승격_실패_시_예약_삭제도_롤백된다() {
        // given
        ReservationTime time = timeDao.save(ReservationTime.createWithoutId(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(Theme.createWithoutId("방탈출1", "설명", "https://thumb.com"));
        LocalDate date = LocalDate.now().plusDays(1);
        Reservation reservation = reservationRepository.save(
                Reservation.createWithoutId("브라운", new ReservationSlot(date, time, theme)));
        waitingDao.save(
                ReservationWaiting.createWithoutId("로지", LocalDateTime.now(),
                        new ReservationSlot(date, time, theme)));

        doThrow(new RuntimeException("승격 실패 강제 주입"))
                .when(waitingService).promoteFirstWaiting(any());

        // when
        assertThatThrownBy(() ->
                reservationService.delete(reservation.getId()))
                .isInstanceOf(RuntimeException.class);

        // then
        assertThat(reservationRepository.findById(reservation.getId())).isPresent();
        assertThat(waitingDao.findAll()).hasSize(1);
    }
}
