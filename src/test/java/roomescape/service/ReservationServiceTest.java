//package roomescape.service;
//
//import org.junit.jupiter.api.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import roomescape.domain.Reservation;
//import roomescape.domain.ReservationTime;
//import roomescape.domain.Theme;
//import roomescape.domain.member.Member;
//import roomescape.domain.member.Role;
//import roomescape.dto.reservation.ReservationResponseDto;
//import roomescape.dto.time.ReservationTimeResponseDto;
//import roomescape.exception.DuplicateContentException;
//import roomescape.exception.InvalidRequestException;
//import roomescape.exception.NotFoundException;
//import roomescape.repository.*;
//import roomescape.service.dto.ReservationCreateDto;
//
//import java.time.LocalDate;
//import java.time.LocalTime;
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
//
//@DataJpaTest
//class ReservationServiceTest {
//    ReservationService reservationService;
//
//    @Autowired
//    JpaReservationRepository reservationRepository;
//    @Autowired
//    JpaReservationTimeRepository reservationTimeRepository;
//    @Autowired
//    JpaMemberRepository memberRepository;
//    @Autowired
//    JpaThemeRepository themeRepository;
//
//    @BeforeEach
//    void setUp(){
//        reservationService = new ReservationService(reservationRepository, reservationTimeRepository, themeRepository,memberRepository);
//
//        reservationRepository.deleteAll();
//        reservationTimeRepository.deleteAll();
//        memberRepository.deleteAll();
//        themeRepository.deleteAll();
//
//        Theme theme = new Theme(null, "우테코", "방탈출", ".png");
//        Member member = new Member(null, "가이온", "email", Role.USER, "password");
//        ReservationTime time = new ReservationTime(null, LocalTime.now());
//        Reservation reservation = new Reservation(null, member, LocalDate.now(), time, theme);
//        themeRepository.save(theme);
//        memberRepository.save(member);
//        reservationTimeRepository.save(time);
//        reservationRepository.save(reservation);
//    }
//
//    @Nested
//    @DisplayName("예약 생성")
//    class CreateReservation {
//
//        @DisplayName("요청에 따라 Reservation을 생성 할 수 있다")
//        @Test
//        void createReservationTest() {
//            LocalTime startTime = LocalTime.now();
//
//            reservationService = new ReservationService(reservationRepository, reservationTimeRepository, themeRepository, memberRepository);
//            memberRepository.save(new Member(1L, "가이온", "hello@woowa.com", Role.USER, "password"));
//
//            ReservationCreateDto requestDto = new ReservationCreateDto(LocalDate.now().plusDays(7), 1L, 1L, 1L);
//            ReservationResponseDto responseDto = reservationService.createReservation(requestDto);
//
//            Long id = responseDto.id();
//            LocalDate date = responseDto.date();
//            Long memberId = requestDto.memberId();
//            String name = memberRepository.findById(memberId).get().getName();
//            ReservationTimeResponseDto time = responseDto.time();
//            Long timeId = time.id();
//            LocalTime localTime = time.startAt();
//
//            Assertions.assertAll(
//                    () -> assertThat(id).isEqualTo(1L),
//                    () -> assertThat(date).isEqualTo(requestDto.date()),
//                    () -> assertThat(name).isEqualTo("가이온"),
//                    () -> assertThat(timeId).isEqualTo(1L),
//                    () -> assertThat(localTime).isEqualTo(startTime)
//            );
//        }
//
//        @DisplayName("요청한 ReservationTime의 id가 존재하지 않으면 Reservation을 생성할 수 없다")
//        @Test
//        void createInvalidReservationIdTest() {
//            memberRepository.save(new Member(1L, "가이온", "hello@woowa.com", Role.USER, "password"));
//
//            reservationService = new ReservationService(reservationRepository, reservationTimeRepository, themeRepository, memberRepository);
//
//            ReservationCreateDto requestDto = new ReservationCreateDto(LocalDate.now(), 1L, 1L, 1L);
//
//            assertThatThrownBy(() -> reservationService.createReservation(requestDto)).isInstanceOf(NotFoundException.class);
//        }
//
//        @DisplayName("이미 동일한 날짜와 시간에 예약이 있으면 생성할 수 없다")
//        @Test
//        void createDuplicateReservationTest() {
//            ReservationCreateDto requestDto = new ReservationCreateDto(LocalDate.now().plusDays(7), 1L, 1L, 1L);
//
//            assertThatThrownBy(() -> reservationService.createReservation(requestDto)).isInstanceOf(DuplicateContentException.class);
//        }
//
//        @DisplayName("이미 지난 날짜의 경우 예약 생성이 불가능 하다")
//        @Test
//        void createInvalidDateTest() {
//
//
//            ReservationCreateDto requestDto = new ReservationCreateDto(LocalDate.of(2025, 1, 1), 1L, 1L, 1L);
//
//            assertThatThrownBy(() -> reservationService.createReservation(requestDto)).isInstanceOf(InvalidRequestException.class);
//        }
//
//        @DisplayName("예약 시간에 같은 테마가 이미 예약 중이지 않다면 예약이 가능하다")
//        @Test
//        void createSameTimeButOtherTheme() {
//            ReservationCreateDto requestDto = new ReservationCreateDto(LocalDate.now().plusDays(7), 1L, 2L, 1L);
//
//            assertDoesNotThrow(() -> reservationService.createReservation(requestDto));
//        }
//    }
//
//    @Nested
//    @DisplayName("예약 조회")
//    class FindReservation {
//
//        @DisplayName("모든 Reservation을 조회할 수 있다")
//        @Test
//        void findAllReservationResponsesTest() {
//            Member member = new Member(1L, "가이온", "hello@woowa.com", Role.USER, "password");
//            memberRepository.save(member);
//            List<ReservationResponseDto> responses = reservationService.findAllReservationResponses();
//
//            assertThat(responses).hasSize(2);
//        }
//    }
//
//    @Nested
//    @DisplayName("예약 삭제")
//    class DeleteReservation {
//
//        @DisplayName("Reservation을 삭제할 수 있다")
//        @Test
//        void deleteReservationTest() {
//            reservationService.deleteReservation(1L);
//
//            List<ReservationResponseDto> responses = reservationService.findAllReservationResponses();
//            assertThat(responses).isEmpty();
//        }
//
//        @DisplayName("존재하지 않는 Id의 Reservation을 삭제할 수 없다")
//        @Test
//        void deleteInvalidReservationIdTest() {
//            LocalTime startTime = LocalTime.of(10, 0);
//            ReservationTime reservationTime = new ReservationTime(1L, startTime);
//            Theme theme = new Theme(1L, "우테코", "방탈출", ".png");
//            Member member = new Member(1L, "가이온", "hello@woowa.com", Role.USER, "password");
//            Reservation reservation = new Reservation(1L, member, LocalDate.of(2025, 4, 24), reservationTime, theme);
//
//
//            assertThatThrownBy(() -> reservationService.deleteReservation(2L)).isInstanceOf(NotFoundException.class);
//        }
//    }
//}
