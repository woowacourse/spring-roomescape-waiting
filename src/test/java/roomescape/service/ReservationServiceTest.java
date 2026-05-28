package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWithStatus;
import roomescape.domain.Theme;
import roomescape.domain.exception.RoomEscapeException;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationUpdateRequest;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@SpringBootTest
@Transactional
class ReservationServiceTest {

    private static final LocalDate FUTURE_SECOND_DATE = LocalDate.now().plusDays(2);
    private static final LocalTime TEN = LocalTime.of(10, 0);

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationTimeRepository timeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Test
    void 빈_슬롯이면_예약으_등록된다() {
        ReservationTime reservationTime = createReservationTime(TEN);
        Theme theme = createTheme();

        ReservationWithStatus reservationWithStatus = reservationService.applyReservation(createReservationRequest(
                "브라운",
                FUTURE_SECOND_DATE,
                reservationTime,
                theme
        ));

        assertThat(reservationWithStatus.getId()).isNotNull();
        assertThat(reservationWithStatus.getName()).isEqualTo("브라운");
        assertThat(reservationWithStatus.getDate()).isEqualTo(FUTURE_SECOND_DATE);
        assertThat(reservationWithStatus.getTime().getId()).isEqualTo(reservationTime.getId());
        assertThat(reservationWithStatus.getTime().getStartAt()).isEqualTo(reservationTime.getStartAt());
        assertThat(reservationWithStatus.getTheme().getId()).isEqualTo(theme.getId());
        assertThat(reservationWithStatus.getTheme().getName()).isEqualTo(theme.getName());
    }

    @Test
    void 이미_예약된_슬롯이면_대기로_등록된다() {
        ReservationTime reservationTime = createReservationTime(TEN);
        Theme theme = createTheme();

        reservationService.applyReservation(createReservationRequest(
                "브라운",
                FUTURE_SECOND_DATE,
                reservationTime,
                theme
        ));
        reservationService.applyReservation(createReservationRequest(
                "워니",
                FUTURE_SECOND_DATE,
                reservationTime,
                theme
        ));

        ReservationWithStatus result = reservationService.applyReservation(createReservationRequest(
                "브리",
                FUTURE_SECOND_DATE,
                reservationTime,
                theme
        ));

        assertThat(result.getStatus()).isEqualTo(ReservationStatus.WAITING);
        assertThat(result.getWaitingOrder()).isEqualTo(2);
    }

    @Test
    void 예약을_추가할_때_예약시간이_없는_경우_예외() {
        Theme theme = createTheme();

        ReservationRequest request = new ReservationRequest(
                "브라운",
                FUTURE_SECOND_DATE,
                1L,
                theme.getId()
        );

        assertThatThrownBy(() -> reservationService.applyReservation(request))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 예약을_추가할_때_테마가_없는_경우_예외() {
        ReservationTime reservationTime = createReservationTime(TEN);

        ReservationRequest request = new ReservationRequest(
                "브라운",
                FUTURE_SECOND_DATE,
                reservationTime.getId(),
                1L
        );

        assertThatThrownBy(() -> reservationService.applyReservation(request))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 걑은_사용자가_이미_예약한_슬롯이면_대기_순번으로_넘어가지_않고_예외() {
        ReservationTime reservationTime = createReservationTime(TEN);
        Theme theme = createTheme();

        ReservationRequest request = new ReservationRequest(
                "브라운",
                FUTURE_SECOND_DATE,
                reservationTime.getId(),
                theme.getId()
        );

        reservationService.applyReservation(request);

        assertThatThrownBy(() -> reservationService.applyReservation(request))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 다른_사용자가_이미_예약한_슬롯에서_사용자가_중복_대기할_수_없다() {
        ReservationTime reservationTime = createReservationTime(TEN);
        Theme theme = createTheme();

        ReservationRequest request = new ReservationRequest(
                "브라운",
                FUTURE_SECOND_DATE,
                reservationTime.getId(),
                theme.getId()
        );

        reservationService.applyReservation(request);

        ReservationRequest waitlistRequest = new ReservationRequest(
                "브리",
                FUTURE_SECOND_DATE,
                reservationTime.getId(),
                theme.getId()
        );
        reservationService.applyReservation(waitlistRequest);

        assertThatThrownBy(() -> reservationService.applyReservation(waitlistRequest))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 예약을_삭제한다() {
        ReservationTime reservationTime = createReservationTime(TEN);
        Theme theme = createTheme();

        ReservationRequest request = new ReservationRequest(
                "브라운",
                FUTURE_SECOND_DATE,
                reservationTime.getId(),
                theme.getId()
        );

        ReservationWithStatus savedReservation = reservationService.applyReservation(request);

        reservationService.deleteReservation(savedReservation.getId());

        assertThatThrownBy(() -> reservationService.getReservation(savedReservation.getId()))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 없는_예약을_삭제할_수_없다() {
        assertThatThrownBy(() -> reservationService.deleteReservation(1L))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 내_예약을_취소한다() {
        ReservationTime reservationTime = createReservationTime(TEN);
        Theme theme = createTheme();
        String name = "브라운";

        ReservationRequest request = new ReservationRequest(
                name,
                FUTURE_SECOND_DATE,
                reservationTime.getId(),
                theme.getId()
        );
        ReservationWithStatus reservation = reservationService.applyReservation(request);

        reservationService.cancelMyReservation(reservation.getId(), name);

        assertThatThrownBy(() -> reservationService.getReservation(reservation.getId()))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 사용자_예약을_취소할_때_존재하지_않는_예약이면_예외() {
        assertThatThrownBy(() -> reservationService.cancelMyReservation(1L, "브라운"))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    @Sql("/data_relative_dates.sql")
    void 예약을_취소할_때_이미_지난_예약이면_예외() {
        String name = "김민수";
        ReservationWithStatus pastReservation = reservationService.getMyReservations(name).getFirst();

        LocalDateTime dateTime = LocalDateTime.of(
                pastReservation.getDate(),
                pastReservation.getTime().getStartAt()
        );

        assertThat(dateTime.isBefore(LocalDateTime.now())).isTrue();
        assertThatThrownBy(() -> reservationService.cancelMyReservation(pastReservation.getId(), name))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 선택한_예약에_내_이름이_일치하면_예약의_날짜와_시간을_수정할_수_있다() {
        ReservationTime reservationTime = createReservationTime(TEN);
        Theme theme = createTheme();
        String name = "브라운";
        ReservationRequest request = new ReservationRequest(
                name,
                FUTURE_SECOND_DATE,
                reservationTime.getId(),
                theme.getId()
        );
        ReservationWithStatus reservation = reservationService.applyReservation(request);

        ReservationTime updateTime = createReservationTime(LocalTime.of(12, 0));
        LocalDate updateDate = FUTURE_SECOND_DATE.plusDays(1);
        ReservationUpdateRequest updateRequest = new ReservationUpdateRequest(
                updateDate,
                updateTime.getId()
        );

        Reservation updatedReservation = reservationService.updateReservation(reservation.getId(), name, updateRequest);

        assertThat(updatedReservation.getId()).isNotNull();
        assertThat(updatedReservation.getName()).isEqualTo(name);
        assertThat(updatedReservation.getDate()).isEqualTo(updateDate);
        assertThat(updatedReservation.getTime().getId()).isEqualTo(updateTime.getId());
        assertThat(updatedReservation.getTime().getStartAt()).isEqualTo(updateTime.getStartAt());
        assertThat(updatedReservation.getTheme().getId()).isEqualTo(theme.getId());
        assertThat(updatedReservation.getTheme().getName()).isEqualTo(theme.getName());
    }

    @Test
    void 예약을_수정할_때_존재하지_않는_예약이면_예외() {
        ReservationTime updateTime = createReservationTime(TEN);
        ReservationUpdateRequest updateRequest = new ReservationUpdateRequest(
                FUTURE_SECOND_DATE,
                updateTime.getId()
        );

        assertThatThrownBy(() -> reservationService.updateReservation(1L, "브라운", updateRequest))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 예약을_수정할_때_존재하지_않는_시간_ID이면_예외() {
        ReservationTime reservationTime = createReservationTime(TEN);
        Theme theme = createTheme();
        String name = "브라운";
        ReservationRequest request = new ReservationRequest(
                name,
                FUTURE_SECOND_DATE,
                reservationTime.getId(),
                theme.getId()
        );
        ReservationWithStatus reservation = reservationService.applyReservation(request);

        ReservationUpdateRequest updateRequest = new ReservationUpdateRequest(
                FUTURE_SECOND_DATE.plusDays(1),
                999L
        );

        assertThatThrownBy(() -> reservationService.updateReservation(reservation.getId(), name, updateRequest))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 예약을_수정할_때_변경하려는_예약_시간이_이미_차_있으면_예외() {
        ReservationTime tenClock = createReservationTime(TEN);
        ReservationTime twelveClock = createReservationTime(LocalTime.of(12, 0));
        Theme theme = createTheme();

        String name = "브라운";
        ReservationRequest request = new ReservationRequest(
                name,
                FUTURE_SECOND_DATE,
                tenClock.getId(),
                theme.getId()
        );
        ReservationWithStatus reservation = reservationService.applyReservation(request);

        ReservationRequest anotherRequest = new ReservationRequest(
                "브리",
                FUTURE_SECOND_DATE.plusDays(1),
                twelveClock.getId(),
                theme.getId()
        );
        reservationService.applyReservation(anotherRequest);

        ReservationUpdateRequest updateRequest = new ReservationUpdateRequest(
                FUTURE_SECOND_DATE.plusDays(1),
                twelveClock.getId()
        );

        assertThatThrownBy(() -> reservationService.updateReservation(reservation.getId(), name, updateRequest))
                .isInstanceOf(RoomEscapeException.class);
    }

    private ReservationTime createReservationTime(LocalTime time) {
        ReservationTime reservationTime = new ReservationTime(time);
        Long id = timeRepository.save(reservationTime);
        return new ReservationTime(id, reservationTime.getStartAt());
    }

    private Theme createTheme() {
        Theme theme = new Theme("방탈출 제목", "방탈출 설명", "thumbnail.png");
        Long id = themeRepository.save(theme);
        return new Theme(
                id,
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnailImageUrl()
        );
    }

    private ReservationRequest createReservationRequest(
            String name,
            LocalDate date,
            ReservationTime time,
            Theme theme
    ) {
        return new ReservationRequest(
                name,
                date,
                time.getId(),
                theme.getId()
        );
    }
}
