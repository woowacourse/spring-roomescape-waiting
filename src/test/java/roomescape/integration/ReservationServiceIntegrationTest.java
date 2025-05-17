package roomescape.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import roomescape.CurrentDateTime;
import roomescape.fake.TestCurrentDateTime;
import roomescape.member.repository.MemberRepository;
import roomescape.member.repository.jpa.MemberRepositoryImpl;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.repository.ThemeRepository;
import roomescape.reservation.repository.jpa.ReservationRepositoryImpl;
import roomescape.reservation.repository.jpa.ReservationTimeRepositoryImpl;
import roomescape.reservation.repository.jpa.ThemeRepositoryImpl;
import roomescape.reservation.service.ReservationService;
import roomescape.reservation.service.dto.ReservationCreateCommand;
import roomescape.reservation.service.dto.ReservationInfo;
import roomescape.reservation.service.dto.ReservationSearchCondition;

@DataJpaTest
@Import(value = {ReservationRepositoryImpl.class, ReservationTimeRepositoryImpl.class,
        ThemeRepositoryImpl.class, MemberRepositoryImpl.class})
@Sql({"/schema.sql", "/test-data.sql"})
public class ReservationServiceIntegrationTest {

    @Autowired
    ReservationRepository reservationRepository;
    @Autowired
    ReservationTimeRepository reservationTimeRepository;
    @Autowired
    ThemeRepository themeRepository;
    @Autowired
    MemberRepository memberRepository;
    @MockitoBean
    CurrentDateTime currentDateTime;
    ReservationService reservationService;

    @BeforeEach
    void init() {
        currentDateTime = new TestCurrentDateTime(LocalDateTime.of(2025, 5, 1, 10, 0));
        reservationService = new ReservationService(reservationRepository, reservationTimeRepository, themeRepository,
                memberRepository, currentDateTime);
    }

    @DisplayName("새로운 예약을 추가할 수 있다")
    @Test
    void createReservation() {
        // given
        LocalDate date = currentDateTime.getDate().plusDays(1);
        ReservationCreateCommand request = new ReservationCreateCommand(date, 1L, 1L, 1L);

        // when
        ReservationInfo result = reservationService.createReservation(request);

        // then
        assertAll(
                () -> assertThat(result.id()).isNotNull(),
                () -> assertThat(result.member().name()).isEqualTo("레오"),
                () -> assertThat(result.date()).isEqualTo(date),
                () -> assertThat(result.time().id()).isNotNull(),
                () -> assertThat(result.time().startAt()).isEqualTo(LocalTime.of(10, 0))
        );
    }

    @DisplayName("날짜와 시간과 테마가 같은 예약이 이미 존재하면 예외가 발생한다")
    @Test
    void should_ThrowException_WhenDuplicateReservation() {
        // given
        ReservationCreateCommand request = new ReservationCreateCommand(LocalDate.of(2025, 5, 5), 1L, 1L, 11L);
        reservationService.createReservation(request);

        // when
        // then
        assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 시간에 이미 예약이 존재합니다.");
    }

    @DisplayName("날짜와 시간이 같아도 테마가 다르면 중복 예외가 발생하지 않는다")
    @Test
    void shouldNot_ThrowException_WhenThemeIsDifferent() {
        // given
        LocalDate date = LocalDate.of(2025, 5, 5);
        ReservationCreateCommand request = new ReservationCreateCommand(date, 1L, 1L, 11L);
        reservationService.createReservation(request);
        ReservationCreateCommand request2 = new ReservationCreateCommand(date, 1L, 1L, 10L);

        // when
        // then
        assertThatCode(() -> reservationService.createReservation(request2))
                .doesNotThrowAnyException();
    }

    @DisplayName("현재 혹은 과거 시간에 새로운 예약을 추가할 경우 예외가 발생한다")
    @Test
    void should_ThrowException_WhenNotFuture() {
        // given
        LocalDate date = currentDateTime.getDate().minusDays(1);
        ReservationCreateCommand request = new ReservationCreateCommand(date, 1L, 1L, 3L);

        // when
        // then
        assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("지나간 날짜와 시간은 예약 불가합니다.");
    }

    @DisplayName("모든 예약을 조회할 수 있다")
    @Test
    void getReservations() {
        // given
        ReservationSearchCondition condition = new ReservationSearchCondition(null, null, null, null);

        // when
        List<ReservationInfo> result = reservationService.getReservations(condition);

        // then
        assertThat(result).hasSize(13);
    }

    @DisplayName("id를 기반으로 예약을 취소할 수 있다")
    @Test
    void cancelReservationById() {
        // when
        reservationService.cancelReservationById(1L);

        // then
        List<Reservation> reservations = reservationRepository.findAll();
        assertThat(reservations).hasSize(12);
    }
}
