//package roomescape.reservation.service;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.LocalTime;
//import java.util.List;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import roomescape.member.repository.FakeMemberRepository;
//import roomescape.member.repository.MemberRepository;
//import roomescape.reservation.dto.response.ReservationResponse;
//import roomescape.reservation.exception.ReservationAlreadyExistsException;
//import roomescape.reservation.exception.ReservationNotFoundException;
//import roomescape.reservation.fixture.TestFixture;
//import roomescape.reservation.repository.FakeReservationRepository;
//import roomescape.reservation.repository.ReservationRepository;
//import roomescape.reservationtime.domain.ReservationTime;
//import roomescape.reservationtime.repository.FakeReservationTimeRepository;
//import roomescape.reservationtime.repository.ReservationTimeRepository;
//import roomescape.theme.repository.FakeThemeRepository;
//import roomescape.theme.repository.ThemeRepository;
//
//class ReservationServiceTest {
//
//    private static final LocalDate futureDate = TestFixture.makeFutureDate();
//    private static final LocalDateTime afterOneHour = TestFixture.makeTimeAfterOneHour();
//    private static final Long themeId = 1L;
//    private static final Long memberId = 1L;
//
//    private ReservationService reservationService;
//    private ReservationTimeRepository reservationTimeRepository;
//
//    @BeforeEach
//    void setUp() {
//        ReservationRepository reservationRepository = new FakeReservationRepository();
//        reservationTimeRepository = new FakeReservationTimeRepository();
//        ThemeRepository themeRepository = new FakeThemeRepository();
//        MemberRepository memberRepository = new FakeMemberRepository();
//
//        reservationService = new ReservationService(reservationRepository, reservationTimeRepository, themeRepository,
//                memberRepository);
//
//        ReservationTime time1 = ReservationTime.of(1L, LocalTime.of(10, 0));
//        reservationTimeRepository.save(time1);
//        ReservationTime time2 = ReservationTime.of(2L, LocalTime.of(10, 0));
//        reservationTimeRepository.save(time2);
//        themeRepository.save(TestFixture.makeTheme(1L));
//        memberRepository.save(TestFixture.makeMember());
//    }
//
//    @Test
//    void createReservation_shouldReturnResponseWhenSuccessful() {
//        ReservationResponse response = reservationService.create(futureDate, 1L, 1L, memberId, afterOneHour);
//
//        Assertions.assertAll(
//                () -> assertThat(response.member().name()).isEqualTo("Mint"),
//                () -> assertThat(response.date()).isEqualTo(futureDate),
//                () -> assertThat(response.time().startAt()).isEqualTo(LocalTime.of(10, 0))
//        );
//    }
//
//    @Test
//    void getReservations_shouldReturnAllCreatedReservations() {
//        reservationTimeRepository.save(ReservationTime.of(2L, LocalTime.of(10, 0)));
//        reservationService.create(futureDate, 1L, 1L, memberId, afterOneHour);
//        reservationService.create(futureDate, 2L, 1L, memberId, afterOneHour);
//
//        List<ReservationResponse> result = reservationService.findReservations(null, null, null, null);
//        assertThat(result).hasSize(2);
//    }
//
//    @Test
//    void deleteReservation_shouldThrowException_WhenIdNotFound() {
//        assertThatThrownBy(() -> reservationService.delete(999L))
//                .isInstanceOf(ReservationNotFoundException.class)
//                .hasMessageContaining("요청한 id와 일치하는 예약 정보가 없습니다.");
//    }
//
//    @Test
//    void deleteReservation_shouldRemoveSuccessfully() {
//        ReservationResponse response = reservationService.create(futureDate, 1L, 1L, memberId, afterOneHour);
//        reservationService.delete(response.id());
//
//        List<ReservationResponse> result = reservationService.findReservations(themeId, memberId, futureDate,
//                futureDate.plusDays(1));
//        assertThat(result).isEmpty();
//    }
//
//    @Test
//    void createReservation_shouldThrowException_WhenDuplicated() {
//        reservationService.create(futureDate, 1L, 1L, 1L, afterOneHour);
//
//        assertThatThrownBy(() -> reservationService.create(futureDate, 1L, 1L, memberId, afterOneHour))
//                .isInstanceOf(ReservationAlreadyExistsException.class)
//                .hasMessageContaining("해당 시간에 이미 예약이 존재합니다.");
//    }
//
//    @Test
//    void createReservation_shouldThrowException_WhenTimeIdNotFound() {
//        assertThatThrownBy(() -> reservationService.create(futureDate, 3L, 1L, memberId, afterOneHour))
//                .isInstanceOf(ReservationNotFoundException.class)
//                .hasMessageContaining("요청한 id와 일치하는 예약 시간 정보가 없습니다.");
//    }
//}
