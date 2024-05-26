package roomescape.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.dto.request.CreateReservationRequest;
import roomescape.member.dto.response.CreateReservationResponse;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.dto.response.ConfirmReservationResponse;
import roomescape.reservation.model.Reservation;
import roomescape.reservation.model.ReservationTime;
import roomescape.reservation.model.Slot;
import roomescape.reservation.model.Theme;
import roomescape.reservation.model.Waiting;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.repository.ThemeRepository;
import roomescape.reservation.repository.WaitingRepository;
import roomescape.util.ServiceTest;

public class AdminServiceTest extends ServiceTest {

    @Autowired
    private AdminService adminService;

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
    private LocalDate date;
    private ReservationTime reservationTime;
    private Theme theme;

    @BeforeEach
    void setUp() {
        date = LocalDate.now().plusDays(1);
        member = memberRepository.save(new Member("몰리", Role.USER, "login@naver.com", "hihi"));
        reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(20, 0)));
        theme = themeRepository.save(new Theme("테마이름", "설명", "썸네일"));
    }

    @Test
    @DisplayName("관리자 권한으로 예약을 생성한다.")
    void createReservation() {
        date = LocalDate.now().plusDays(1);
        CreateReservationRequest request = new CreateReservationRequest(date, reservationTime.getId(), theme.getId(),
                member.getId());

        CreateReservationResponse response = adminService.createReservation(request);

        assertAll(
                () -> assertThat(response.id()).isNotNull(),
                () -> assertThat(response.member().name()).isEqualTo("몰리"),
                () -> assertThat(response.theme().name()).isEqualTo("테마이름"),
                () -> assertThat(response.date()).isEqualTo(LocalDate.now().plusDays(1)),
                () -> assertThat(response.time().startAt()).isEqualTo(LocalTime.of(20, 0))
        );
    }

    @Test
    @DisplayName("관리자 권한으로 예약 생성 시, 해당하는 테마가 없는 경우 예외를 반환한다.")
    void createReservation_WhenThemeNotExist() {
        date = LocalDate.now().plusDays(1);
        CreateReservationRequest request = new CreateReservationRequest(date, member.getId(), reservationTime.getId(),
                999L);

        assertThatThrownBy(() -> adminService.createReservation(request))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("해당하는 테마가 존재하지 않아 예약을 생성할 수 없습니다.");
    }

    @Test
    @DisplayName("관리자 권한으로 예약 생성 시, 해당하는 시간이 없는 경우 예외를 반환한다.")
    void createReservation_WhenTimeNotExist() {
        date = LocalDate.now().plusDays(1);
        CreateReservationRequest request = new CreateReservationRequest(date, member.getId(), 999L, theme.getId());

        assertThatThrownBy(() -> adminService.createReservation(request))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("해당하는 시간이 존재하지 않아 예약을 생성할 수 없습니다.");
    }

    @Test
    @DisplayName("관리자 권한으로 예약 생성 시, 해당하는 사용자가 없는 경우 예외를 반환한다.")
    void createReservation_WhenMemberNotExist() {
        date = LocalDate.now().plusDays(1);
        CreateReservationRequest request = new CreateReservationRequest(date, 999L, reservationTime.getId(),
                theme.getId());

        assertThatThrownBy(() -> adminService.createReservation(request))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("해당하는 사용자가 존재하지 않아 예약을 생성할 수 없습니다.");
    }

    @Test
    @DisplayName("관리자 권한으로 대기를 예약으로 변경한다.")
    void confirmWaiting() {
        Waiting waiting = waitingRepository.save(new Waiting(member, new Slot(date, reservationTime, theme)));

        ConfirmReservationResponse response = adminService.confirmWaiting(waiting.getId());

        assertAll(
                () -> assertThat(response.id()).isNotNull(),
                () -> assertThat(response.member().name()).isEqualTo("몰리"),
                () -> assertThat(response.date()).isEqualTo(date),
                () -> assertThat(response.time().startAt()).isEqualTo(LocalTime.of(20, 0)),
                () -> assertThat(response.theme().name()).isEqualTo("테마이름")
        );
    }

    @Test
    @DisplayName("관리자 권한으로 대기를 예약으로 변경 시, 이미 예약이 존재하는 경우 예외를 반환한다.")
    void confirmWaiting_WhenReservationExists() {
        Waiting waiting = waitingRepository.save(new Waiting(member, new Slot(date, reservationTime, theme)));
        reservationRepository.save(new Reservation(member, new Slot(date, reservationTime, theme)));

        assertThatThrownBy(() -> adminService.confirmWaiting(waiting.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 예약이 존재하여 대기를 예약으로 변경할 수 없습니다.");
    }

    @Test
    @DisplayName("관리자 권한으로 대기를 예약으로 변경 시, 앞선 대기가 존재하는 경우 예외를 반환한다.")
    void confirmWaiting_WhenEarlierWaitingExists() {
        Waiting earlierWaiting = waitingRepository.save(new Waiting(member, new Slot(date, reservationTime, theme)));
        Waiting laterWaiting = waitingRepository.save(new Waiting(member, new Slot(date, reservationTime, theme)));

        assertThatThrownBy(() -> adminService.confirmWaiting(laterWaiting.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(laterWaiting.getId() + "번 예약 대기보다 앞선 대기가 존재하여 예약으로 변경할 수 없습니다.");
    }

    @Test
    @DisplayName("관리자 권한으로 대기를 예약으로 변경 시, 대기가 존재하지 않는 경우 예외를 반환한다.")
    void confirmWaiting_WhenWaitingNotExist() {
        assertThatThrownBy(() -> adminService.confirmWaiting(999L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("식별자 999에 해당하는 대기가 존재하지 않아 예약으로 변경할 수 없습니다.");
    }
}
