package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.jdbc.Sql;
import roomescape.config.TestTimeConfig;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.dto.ReservationRequestDTO;
import roomescape.dto.ReservationUpdateRequest;
import roomescape.exception.RoomEscapeException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingRepository;

@SpringBootTest
@Import(TestTimeConfig.class)
@Sql(scripts = "/empty.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ReservationServiceTransactionTest {

    @Autowired
    private ReservationService reservationService;

    @SpyBean
    private ReservationRepository reservationRepository;

    @Autowired
    private WaitingRepository waitingRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Test
    void 예약_추가_성공시_저장된다() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.parse("10:00")));
        Theme theme = themeRepository.save(
                Theme.create("귀신찾기", "귀신을 찾는다", "example.com"));

        ReservationRequestDTO request = new ReservationRequestDTO(
                "코코",
                LocalDate.of(2030, 6, 7),
                reservationTime.getId(),
                theme.getId()
        );

        // when
        reservationService.addReservation(request);

        // then
        assertThat(reservationRepository.findAll()).hasSize(1);

        Reservation saved = reservationRepository.findAll().get(0);
        assertThat(saved.getName()).isEqualTo("코코");
        assertThat(saved.getReservationSlot().getDate()).isEqualTo(LocalDate.of(2030, 6, 7));
        assertThat(saved.getReservationSlot().getTime().getId()).isEqualTo(reservationTime.getId());
        assertThat(saved.getReservationSlot().getTheme().getId()).isEqualTo(theme.getId());
    }

    @Test
    void 예약_추가_중복이면_예외가_나고_기존데이터는_유지된다() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.parse("10:00")));
        Theme theme = themeRepository.save(
                Theme.create("귀신찾기", "귀신을 찾는다", "example.com"));
        ReservationSlot slot = ReservationSlot.of(
                LocalDate.of(2030, 6, 7),
                reservationTime,
                theme
        );
        reservationRepository.save(Reservation.create("기존예약", slot));

        ReservationRequestDTO request = new ReservationRequestDTO(
                "코코",
                LocalDate.of(2030, 6, 7),
                reservationTime.getId(),
                theme.getId()
        );

        // when & then
        assertThatThrownBy(() -> reservationService.addReservation(request))
                .isInstanceOf(RoomEscapeException.class);

        assertThat(reservationRepository.findAll()).hasSize(1);
        assertThat(reservationRepository.findAll().get(0).getName()).isEqualTo("기존예약");
    }

    @Test
    void 예약_수정_성공시_예약이_바뀌고_대기자가_승격된다() {
        // given
        ReservationTime time10 = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.parse("10:00")));
        ReservationTime time11 = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.parse("11:00")));
        Theme theme = themeRepository.save(
                Theme.create("귀신찾기", "귀신을 찾는다", "example.com"));

        LocalDate date = LocalDate.of(2030, 6, 7);

        ReservationSlot originalSlot = ReservationSlot.of(date, time10, theme);
        Reservation originalReservation = reservationRepository.save(
                Reservation.create("코코", originalSlot));

        waitingRepository.save(Waiting.create("브라운", originalSlot, 1L));

        ReservationUpdateRequest request = new ReservationUpdateRequest(
                date,
                time11.getId()
        );

        // when
        reservationService.updateReservation(originalReservation.getId(), request);

        // then
        assertThat(reservationRepository.findAll()).hasSize(2);

        Reservation updated = reservationRepository.findById(originalReservation.getId()).orElseThrow();
        assertThat(updated.getReservationSlot().getTime()).isEqualTo(time11);

        Optional<Reservation> promoted = reservationRepository.findAll().stream()
                .filter(r -> !r.getId().equals(originalReservation.getId()))
                .findFirst();

        assertThat(promoted).isPresent();
        assertThat(promoted.get().getName()).isEqualTo("브라운");
        assertThat(waitingRepository.findAll()).isEmpty();
    }

    @Test
    void 예약_수정_중_중복이면_예외가_나고_기존예약은_유지된다() {
        // given
        ReservationTime time10 = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.parse("10:00")));
        ReservationTime time11 = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.parse("11:00")));
        Theme theme = themeRepository.save(
                Theme.create("귀신찾기", "귀신을 찾는다", "example.com"));

        LocalDate date = LocalDate.of(2030, 6, 7);

        Reservation originalReservation = reservationRepository.save(
                Reservation.create("코코", ReservationSlot.of(date, time10, theme)));
        reservationRepository.save(
                Reservation.create("브라운", ReservationSlot.of(date, time11, theme)));

        ReservationUpdateRequest request = new ReservationUpdateRequest(
                date,
                time11.getId()
        );

        // when & then
        assertThatThrownBy(() -> reservationService.updateReservation(originalReservation.getId(), request))
                .isInstanceOf(RoomEscapeException.class);

        Reservation stillOriginal = reservationRepository.findById(originalReservation.getId()).orElseThrow();
        assertThat(stillOriginal.getReservationSlot().getTime()).isEqualTo(time10);
        assertThat(reservationRepository.findAll()).hasSize(2);
    }

    @Test
    void 예약_삭제_중_승격저장에서_예외가_나면_롤백된다() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.parse("10:00")));
        Theme theme = themeRepository.save(
                Theme.create("귀신찾기", "귀신을 찾는다", "example.com"));
        LocalDate date = LocalDate.of(2030, 6, 7);

        ReservationSlot slot = ReservationSlot.of(date, reservationTime, theme);
        Reservation existingReservation = reservationRepository.save(
                Reservation.create("코코", slot));

        waitingRepository.save(Waiting.create("브라운", slot, 1L));

        doAnswer(invocation -> {
            Reservation saved = invocation.getArgument(0);
            if ("브라운".equals(saved.getName())) {
                throw new RuntimeException("승격 저장 실패");
            }
            return invocation.callRealMethod();
        }).when(reservationRepository).save(any(Reservation.class));

        // when
        assertThatThrownBy(() -> reservationService.deleteReservation(existingReservation.getId()))
                .isInstanceOf(RuntimeException.class);

        // then
        assertThat(reservationRepository.findById(existingReservation.getId())).isPresent();
        assertThat(waitingRepository.findAll()).hasSize(1);
    }

    @Test
    void 예약_삭제_성공시_예약이_삭제되고_대기자가_승격된다() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.parse("10:00")));
        Theme theme = themeRepository.save(
                Theme.create("귀신찾기", "귀신을 찾는다", "example.com"));
        LocalDate date = LocalDate.of(2030, 6, 7);

        ReservationSlot slot = ReservationSlot.of(date, reservationTime, theme);
        Reservation existingReservation = reservationRepository.save(
                Reservation.create("코코", slot));

        waitingRepository.save(Waiting.create("브라운", slot, 1L));

        // when
        reservationService.deleteReservation(existingReservation.getId());

        // then
        assertThat(reservationRepository.findAll()).hasSize(1);
        assertThat(reservationRepository.findAll().get(0).getName()).isEqualTo("브라운");
        assertThat(waitingRepository.findAll()).isEmpty();
    }
}
