package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationTimeStatus;
import roomescape.domain.Theme;
import roomescape.dto.request.ReservationTimeRequest;
import roomescape.dto.response.ReservationTimeResponse;
import roomescape.dto.response.TimeSlotResponse;
import roomescape.exception.AlreadyExistsException;
import roomescape.exception.AlreadyInUseException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class ReservationTimeServiceTest {

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Test
    void 전체_예약_시간_순서_확인() {
        List<ReservationTimeResponse> result = reservationTimeService.findAll();

        List<LocalTime> startTimes = result.stream()
                .map(ReservationTimeResponse::startAt)
                .toList();

        assertThat(startTimes).contains(
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                LocalTime.of(12, 0)
        );
    }

    @Test
    void 중복되지_않는_시간_저장() {
        LocalTime newTime = LocalTime.of(23, 59);
        ReservationTimeRequest request = new ReservationTimeRequest(newTime);

        ReservationTimeResponse result = reservationTimeService.save(request);

        assertThat(result.startAt()).isEqualTo(newTime);
    }

    @Test
    void 중복_시간_저장_시_예외() {
        ReservationTimeRequest request = new ReservationTimeRequest(LocalTime.of(10, 0));

        assertThatThrownBy(() -> reservationTimeService.save(request))
                .isInstanceOf(AlreadyExistsException.class);
    }

    @Test
    void 예약_없는_시간_삭제() {
        ReservationTimeResponse saved = reservationTimeService.save(new ReservationTimeRequest(LocalTime.of(22, 0)));

        reservationTimeService.delete(saved.id());

        List<ReservationTimeResponse> all = reservationTimeService.findAll();
        assertThat(all).noneMatch(t -> t.id().equals(saved.id()));
    }

    @Test
    void 예약_존재하는_시간_삭제_시_예외() {
        ReservationTimeResponse timeResponse = reservationTimeService.save(
                new ReservationTimeRequest(LocalTime.of(21, 0)));
        Theme theme = themeRepository.save(new Theme("테스트테마", "설명", "url"));
        reservationRepository.save(
                new Reservation("브라운", LocalDate.now(), new ReservationTime(timeResponse.id(), timeResponse.startAt()),
                        theme, ReservationStatus.CONFIRMED));

        assertThatThrownBy(() -> reservationTimeService.delete(timeResponse.id()))
                .isInstanceOf(AlreadyInUseException.class);
    }

    @Test
    void 예약된_시간_제외_가용_시간_조회() {
        LocalDate date = LocalDate.now().plusDays(1);
        Theme theme = themeRepository.save(new Theme("가용시간테마", "설명", "url"));

        List<ReservationTimeResponse> allTimes = reservationTimeService.findAll();
        ReservationTimeResponse targetTime = allTimes.get(0);
        reservationRepository.save(
                new Reservation("브라운", date, new ReservationTime(targetTime.id(), targetTime.startAt()), theme,
                        ReservationStatus.CONFIRMED));

        List<TimeSlotResponse> result = reservationTimeService.findAvailableTime(theme.getId(), date);

        TimeSlotResponse occupiedSlot = result.stream()
                .filter(r -> r.id().equals(targetTime.id()))
                .findFirst()
                .orElseThrow();

        assertThat(occupiedSlot.status()).isEqualTo(ReservationTimeStatus.RESERVED);
    }

    @Test
    void 예약_없는_날짜의_전체_가용_시간_조회() {
        LocalDate date = LocalDate.now().plusDays(30);
        Theme theme = themeRepository.save(new Theme("빈날짜테마", "설명", "url"));

        List<TimeSlotResponse> result = reservationTimeService.findAvailableTime(theme.getId(), date);

        assertThat(result).allMatch(r -> r.status() == ReservationTimeStatus.AVAILABLE);
    }
}
