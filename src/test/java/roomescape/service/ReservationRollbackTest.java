package roomescape.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import roomescape.ServiceTest;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.ThemeDao;
import roomescape.dao.WaitingDao;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.dto.request.ReservationRequest;
import roomescape.dto.response.ReservationResponse;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;

public class ReservationRollbackTest extends ServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationTimeDao reservationTimeDao;

    @Autowired
    private ThemeDao themeDao;

    @MockitoBean
    private WaitingDao waitingDao;

    @Autowired
    private Clock clock;
    @Autowired
    private ReservationDao reservationDao;

    @Test
    void 예약_삭제_중_대기_승격이_실패하면_대기_승격_작업_전부가_롤백된다() {
        // given
        ReservationTime reservationTime = saveReservationTime(LocalTime.of(13, 0));
        Theme theme = saveTheme("테마1", "로지와 러키의 방탈출", "https:fsof/ommff");

        ReservationRequest request = createReservationRequest(reservationTime.getId(), theme.getId(), LocalDate.of(2026, 5, 8));
        ReservationResponse response = reservationService.create(request);
        Slot slot = reservationDao.findById(response.id()).orElseThrow().getSlot();
        String fakeUserName = "fake";

        Mockito.when(waitingDao.findFirstBySlotIdOrderByCreatedAt(slot.getId())).
                thenReturn(Optional.of(new Waiting(LocalDateTime.of(2040, 12, 12, 12, 12, 12),
                        slot.getId(), fakeUserName)));

        doThrow(new RuntimeException("waitingDao 강제 실패")).when(waitingDao).delete(anyLong());

        // when & then
        assertAll(
                () -> assertThatThrownBy(() -> reservationService.delete(response.id()))
                        .isInstanceOf(RuntimeException.class),
                () -> assertThat(reservationDao.findAll()).hasSize(1),
                () -> assertThat(reservationDao.findById(response.id())).isPresent(),
                () -> assertThat(reservationDao.findAllByName(fakeUserName)).isEmpty()
        );
    }

    private ReservationTime saveReservationTime(LocalTime startAt) {
        ReservationTime reservationTime = new ReservationTime(startAt);
        return reservationTimeDao.save(reservationTime);
    }

    private Theme saveTheme(String name, String description, String thumbnail) {
        Theme theme = new Theme(name, description, thumbnail);
        return themeDao.save(theme);
    }

    private void saveWaiting(String userName, Slot slot) {
        waitingDao.save(new Waiting(LocalDateTime.now(clock), slot.getId(), userName));
    }

    private ReservationRequest createReservationRequest(long timeId, long themeId, LocalDate localDate) {
        return new ReservationRequest(
                "예약1",
                localDate,
                timeId,
                themeId
        );
    }
}
