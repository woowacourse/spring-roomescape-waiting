package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWithStatus;
import roomescape.domain.Theme;
import roomescape.domain.exception.RoomEscapeException;
import roomescape.dto.ReservationRequest;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class WaitlistServiceTest {

    private static final LocalDate FUTURE_SECOND_DATE = LocalDate.now().plusDays(2);
    private static final LocalTime TEN = LocalTime.of(10, 0);
    public static final String NEO = "네오";
    public static final String BRIE = "브리";
    public static final String BROWN = "브라운";
    public static final String POBI = "포비";

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationTimeRepository timeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private WaitlistService waitlistService;

    @Test
    void 예약_대기를_삭제한다() {
        LinkedHashMap<String, ReservationWithStatus> reservations = reserveSameSlotWithWaiters(
            BROWN,
            BRIE
        );

        ReservationWithStatus waiting = reservations.get(BRIE);

        waitlistService.cancelMyWaitlist(waiting.getId(), BRIE);

        assertThatThrownBy(() -> waitlistService.getWaitlist(waiting.getId()))
            .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void id가_존재하지_않으면_예외() {
        assertThatThrownBy(() -> waitlistService.cancelMyWaitlist(1L, BROWN))
            .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 본인의_대기가_아니면_취소할_수_없다() {
        LinkedHashMap<String, ReservationWithStatus> reservations = reserveSameSlotWithWaiters(
            BROWN,
            BRIE
        );

        ReservationWithStatus waitingReservation = reservations.get(BRIE);

        assertThatThrownBy(() -> waitlistService.cancelMyWaitlist(waitingReservation.getId(), BROWN))
            .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 여러_명이_대기할_때_내_대기_순번을_계산한다() {
        reserveSameSlotWithWaiters(
            BROWN,
            BRIE, POBI, NEO
        );

        List<ReservationWithStatus> neoReservations = reservationService.getMyReservations(NEO);

        assertThat(neoReservations.getFirst().getWaitingOrder()).isEqualTo(3);
    }

    private ReservationTime createReservationTime(LocalTime time) {
        ReservationTime reservationTime = new ReservationTime(time);
        Long id = timeRepository.save(reservationTime).getId();
        return new ReservationTime(id, reservationTime.getStartAt());
    }

    private Theme createTheme() {
        Theme theme = new Theme("방탈출 제목", "방탈출 설명", "thumbnail.png");
        Long id = themeRepository.save(theme).getId();
        return new Theme(
            id,
            theme.getName(),
            theme.getDescription(),
            theme.getThumbnailImageUrl()
        );
    }

    private LinkedHashMap<String, ReservationWithStatus> reserveSameSlotWithWaiters(
        String reservedName,
        String... waitingNames
    ) {
        LinkedHashMap<String, ReservationWithStatus> results = new LinkedHashMap<>();
        ReservationTime reservationTime = createReservationTime(TEN);
        Theme theme = createTheme();

        reserve(reservedName, reservationTime, theme, results);
        wait(waitingNames, reservationTime, theme, results);

        return results;
    }

    private void wait(String[] waitingNames,
        ReservationTime reservationTime,
        Theme theme,
        LinkedHashMap<String, ReservationWithStatus> results
    ) {
        for (String waitingName : waitingNames) {
            ReservationRequest waitingRequest = new ReservationRequest(
                waitingName,
                FUTURE_SECOND_DATE,
                reservationTime.getId(),
                theme.getId()
            );

            ReservationWithStatus waiting = reservationService.reserveOrWait(waitingRequest);
            results.put(waitingName, waiting);
        }
    }

    private void reserve(String reservedName,
        ReservationTime reservationTime,
        Theme theme,
        LinkedHashMap<String, ReservationWithStatus> results
    ) {
        ReservationRequest reservedRequest = new ReservationRequest(
            reservedName,
            FUTURE_SECOND_DATE,
            reservationTime.getId(),
            theme.getId()
        );

        ReservationWithStatus reserved = reservationService.reserveOrWait(reservedRequest);
        results.put(reservedName, reserved);
    }
}
