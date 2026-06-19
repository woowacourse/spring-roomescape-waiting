package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.domain.slot.SlotDomainService;
import roomescape.dto.reservation.ReservationRequest;
import roomescape.dto.reservationtime.ReservationTimeRequest;
import roomescape.dto.reservationtime.ReservationTimeResponse;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.exception.ReferencedDataException;
import roomescape.repository.JdbcReservationOrderRepository;
import roomescape.repository.JdbcReservationRepository;
import roomescape.repository.JdbcReservationTimeRepository;
import roomescape.repository.JdbcReservationWaitingRepository;
import roomescape.repository.JdbcSlotRepository;
import roomescape.repository.JdbcThemeRepository;

@JdbcTest
@Import({ReservationTimeService.class, JdbcReservationTimeRepository.class,
        ReservationService.class, SlotDomainService.class, JdbcSlotRepository.class, JdbcReservationRepository.class,
        JdbcThemeRepository.class, JdbcReservationWaitingRepository.class,
        ReservationOrderService.class, JdbcReservationOrderRepository.class})
class ReservationTimeServiceTest {

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ThemeRepository themeUpdatingDao;

    @Test
    void 예약_시간_생성_성공() {
        ReservationTimeRequest request = new ReservationTimeRequest(LocalTime.of(10, 0));

        ReservationTimeResponse saved = reservationTimeService.create(request);

        assertThat(saved.id()).isNotNull();
        assertThat(saved.startAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    void 전체_예약_시간_조회() {
        reservationTimeService.create(new ReservationTimeRequest(LocalTime.of(10, 0)));
        reservationTimeService.create(new ReservationTimeRequest(LocalTime.of(11, 0)));

        List<ReservationTimeResponse> result = reservationTimeService.readAll();

        assertThat(result).hasSize(2);
    }

    @Test
    void 예약_시간_삭제_성공() {
        ReservationTimeResponse saved = reservationTimeService.create(new ReservationTimeRequest(LocalTime.of(10, 0)));

        reservationTimeService.delete(saved.id());

        assertThat(reservationTimeService.readAll()).isEmpty();
    }

    @Test
    void 예약이_존재하는_시간_삭제시_예외가_발생한다() {
        ReservationTimeResponse savedTime = reservationTimeService.create(new ReservationTimeRequest(LocalTime.of(10, 0)));
        Long themeId = themeUpdatingDao.insert(new Theme(null,"테마", "설명", "http://example.com"));
        reservationService.create(new ReservationRequest("브라운", LocalDate.now().plusDays(1), savedTime.id(), themeId));

        assertThatThrownBy(() -> reservationTimeService.delete(savedTime.id()))
                .isInstanceOf(ReferencedDataException.class);
    }
}
