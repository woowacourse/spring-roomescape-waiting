package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.domain.AuthInfo;
import roomescape.fixture.MemberFixture;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.dto.request.CreateReservationRequest;
import roomescape.reservation.dto.response.CreateReservationResponse;
import roomescape.reservation.dto.response.FindAvailableTimesResponse;
import roomescape.reservation.dto.response.FindReservationResponse;
import roomescape.reservation.model.Reservation;
import roomescape.reservation.model.ReservationTime;
import roomescape.reservation.model.Theme;
import roomescape.reservation.model.Waiting;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.repository.ThemeRepository;
import roomescape.reservation.repository.WaitingRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private WaitingRepository waitingRepository;

    private Member member;

    private AuthInfo authInfo;
    private LocalDate date;
    private ReservationTime reservationTime;
    private Theme theme;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(new Member("몰리", Role.USER, "login@naver.com", "hihi"));
        authInfo = new AuthInfo(member.getId(), member.getName(), member.getRole());
        reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(20, 0)));
        theme = themeRepository.save(new Theme("테마이름", "설명", "썸네일"));
    }

    @Test
    @DisplayName("방탈출 예약 생성 성공 시, 생성된 예약의 정보를 반환한다.")
    void createReservation() {
        date = LocalDate.now().plusDays(1);
        CreateReservationRequest request = new CreateReservationRequest(date, reservationTime.getId(), theme.getId());

        CreateReservationResponse response = reservationService.createReservation(authInfo, request);

        assertAll(
                () -> assertThat(response.id()).isNotNull(),
                () -> assertThat(response.member().name()).isEqualTo("몰리"),
                () -> assertThat(response.theme().name()).isEqualTo("테마이름"),
                () -> assertThat(response.date()).isEqualTo(LocalDate.now().plusDays(1)),
                () -> assertThat(response.time().startAt()).isEqualTo(LocalTime.of(20, 0))
        );
    }

    @Test
    @DisplayName("방탈출 예약 생성 시, 날짜가 과거인 경우 예외를 반환한다.")
    void createReservation_WhenDateIsPast() {
        date = LocalDate.now().minusDays(1);
        CreateReservationRequest request = new CreateReservationRequest(date, reservationTime.getId(), theme.getId());

        assertThatThrownBy(() -> reservationService.createReservation(authInfo, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("지나간 시간임으로 예약 생성이 불가능합니다. 현재 이후 날짜로 재예약해주세요.");
    }

    @Test
    @DisplayName("예약 생성 시 해당하는 테마가 없는 경우 예외를 반환한다.")
    void createReservation_WhenThemeNotExist() {
        date = LocalDate.now().plusDays(1);
        CreateReservationRequest request = new CreateReservationRequest(date, reservationTime.getId(), 999L);

        assertThatThrownBy(() -> reservationService.createReservation(authInfo, request))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("식별자 999에 해당하는 테마가 존재하지 않아 예약을 생성할 수 없습니다.");
    }

    @Test
    @DisplayName("예약 생성 시 해당하는 시간이 없는 경우 예외를 반환한다.")
    void createReservation_WhenTimeNotExist() {
        date = LocalDate.now().plusDays(1);
        CreateReservationRequest request = new CreateReservationRequest(date, 999L, theme.getId());

        assertThatThrownBy(() -> reservationService.createReservation(authInfo, request))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("식별자 999에 해당하는 시간이 존재하지 않아 예약을 생성할 수 없습니다.");
    }

    @Test
    @DisplayName("예약 생성 시 이미 같은 테마, 같은 날짜, 같은 시간에 예약이 있는 경우 예외를 반환한다.")
    void createReservation_WhenTimeAndDateAndThemeExist() {
        date = LocalDate.now().plusDays(1);
        reservationRepository.save(new Reservation(member, date, reservationTime, theme));
        CreateReservationRequest request = new CreateReservationRequest(date, reservationTime.getId(), theme.getId());

        assertThatThrownBy(() -> reservationService.createReservation(authInfo, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 예약이 존재하여 예약을 생성할 수 없습니다.");
    }

    @Test
    @DisplayName("예약시 같은 테마, 같은 날짜, 같은 시간에 이미 예약 대기가 있는 경우 예외를 반환한다.")
    void createReservationWhenAlreadyHasWaiting() {
        date = LocalDate.now().plusDays(1);
        Member anotherMember = memberRepository.save(MemberFixture.getOne());
        waitingRepository.save(new Waiting(anotherMember, date, reservationTime, theme));
        CreateReservationRequest request = new CreateReservationRequest(date, reservationTime.getId(), theme.getId());

        assertThatThrownBy(() -> reservationService.createReservation(authInfo, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("대기자가 있어 예약을 생성할 수 없습니다.");
    }

    @Test
    @DisplayName("방탈출 예약 목록을 조회한다.")
    void getReservations() {
        reservationRepository.save(new Reservation(member, LocalDate.parse("2024-11-23"), reservationTime, theme));
        reservationRepository.save(new Reservation(member, LocalDate.parse("2024-12-23"), reservationTime, theme));

        List<FindReservationResponse> response = reservationService.getReservations();

        assertAll(
                () -> assertThat(response).hasSize(2),
                () -> assertThat(response).extracting("date")
                        .containsExactlyInAnyOrder(LocalDate.parse("2024-11-23"), LocalDate.parse("2024-12-23"))
        );
    }

    @Test
    @DisplayName("방탈출 예약 하나를 조회한다.")
    void getReservation() {
        Reservation reservation = reservationRepository.save(
                new Reservation(member, LocalDate.parse("2024-11-23"), reservationTime, theme));

        FindReservationResponse response = reservationService.getOneReservation(reservation.getId());

        assertAll(
                () -> assertThat(response.id()).isEqualTo(reservation.getId()),
                () -> assertThat(response.member().name()).isEqualTo(member.getName()),
                () -> assertThat(response.date()).isEqualTo("2024-11-23"),
                () -> assertThat(response.time().startAt()).isEqualTo("20:00"),
                () -> assertThat(response.theme().name()).isEqualTo("테마이름")
        );
    }

    @Test
    @DisplayName("방탈출 예약 조회 시, 조회하려는 예약이 없는 경우 예외를 반환한다.")
    void getReservation_WhenReservationNotExist() {
        assertThatThrownBy(() -> reservationService.getOneReservation(999L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("식별자 999에 해당하는 예약이 존재하지 않아 예약을 조회할 수 없습니다.");
    }

    @Test
    @DisplayName("해당 날짜와 테마를 통해 예약 가능한 시간 조회한다.")
    void getAvailableTimes() {
        reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.parse("10:00")));
        date = LocalDate.parse("2024-11-23");
        reservationRepository.save(new Reservation(member, date, reservationTime, theme));

        List<FindAvailableTimesResponse> availableTimes = reservationService.getAvailableTimes(date, theme.getId());

        assertAll(
                () -> assertThat(availableTimes).hasSize(2),
                () -> assertThat(availableTimes).extracting("startAt")
                        .containsExactlyInAnyOrder(LocalTime.parse("10:00"), LocalTime.parse("20:00")),
                () -> assertThat(availableTimes).extracting("alreadyBooked").containsExactlyInAnyOrder(true, false)
        );
    }

    @Test
    @DisplayName("해당 날짜와 테마, 기간에 해당하는 예약을 검색한다.")
    void searchReservations() {
        reservationRepository.save(new Reservation(member, LocalDate.parse("2024-12-22"), reservationTime, theme));
        reservationRepository.save(new Reservation(member, LocalDate.parse("2024-12-23"), reservationTime, theme));
        reservationRepository.save(new Reservation(member, LocalDate.parse("2024-12-24"), reservationTime, theme));
        reservationRepository.save(new Reservation(member, LocalDate.parse("2024-12-25"), reservationTime, theme));

        LocalDate fromDate = LocalDate.parse("2024-12-23");
        LocalDate toDate = LocalDate.parse("2024-12-24");

        List<FindReservationResponse> response =
                reservationService.searchBy(theme.getId(), member.getId(), fromDate, toDate);

        assertAll(
                () -> assertThat(response).hasSize(2),
                () -> assertThat(response).extracting("date")
                        .containsExactlyInAnyOrder(LocalDate.parse("2024-12-23"), LocalDate.parse("2024-12-24"))
        );
    }

    @Test
    @DisplayName("방탈출 예약 하나를 삭제한다.")
    void deleteReservation() {
        date = LocalDate.parse("2024-11-23");
        Reservation reservation = reservationRepository.save(new Reservation(member, date, reservationTime, theme));

        reservationService.deleteReservation(reservation.getId());

        assertThat(reservationRepository.findById(reservation.getId())).isEmpty();
    }

    @Test
    @DisplayName("방탈출 예약 조회 시, 조회하려는 예약이 없는 경우 예외를 반환한다.")
    void deleteReservation_WhenReservationNotExist() {
        assertThatThrownBy(() -> reservationService.deleteReservation(999L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("식별자 999에 해당하는 예약이 존재하지 않습니다. 삭제가 불가능합니다.");
    }
}




