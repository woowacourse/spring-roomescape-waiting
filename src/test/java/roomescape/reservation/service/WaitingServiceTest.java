package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
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
import roomescape.reservation.dto.request.CreateWaitingRequest;
import roomescape.reservation.dto.response.CreateWaitingResponse;
import roomescape.reservation.dto.response.FindWaitingResponse;
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
public class WaitingServiceTest {

    @Autowired
    private WaitingService waitingService;

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
    @DisplayName("예약 대기 생성 성공 시, 생성된 시간대의 정보를 반환한다.")
    void createWaiting() {
        date = LocalDate.now().plusDays(1);
        CreateWaitingRequest request = new CreateWaitingRequest(date, reservationTime.getId(), theme.getId());
        Member otherMember = memberRepository.save(MemberFixture.getOne("login@google.com"));
        reservationRepository.save(new Reservation(otherMember, date, reservationTime, theme));

        CreateWaitingResponse response = waitingService.createWaiting(authInfo, request);

        assertAll(
                () -> assertThat(response.member().name()).isEqualTo("몰리"),
                () -> assertThat(response.date()).isEqualTo(date),
                () -> assertThat(response.time().startAt()).isEqualTo(LocalTime.of(20, 0)),
                () -> assertThat(response.theme().name()).isEqualTo("테마이름")
        );
    }

    @Test
    @DisplayName("예약 대기 생성 시, 날짜가 과거인 경우 예외를 반환한다.")
    void createWaitingWhenTimeIsPast() {
        date = LocalDate.now().minusDays(1);
        CreateWaitingRequest request = new CreateWaitingRequest(date, reservationTime.getId(), theme.getId());

        assertThatThrownBy(() -> waitingService.createWaiting(authInfo, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("지나간 시간임으로 예약 대기 생성이 불가능합니다. 현재 이후 날짜로 재예약해주세요.");
    }

    @Test
    @DisplayName("예약 대기 생성 시 같은 테마, 같은 날짜, 같은 시간에 예약과 예약 대기가 없는 경우 예외를 반환한다.")
    void createWaitingWhenTimeAndDateAndThemeNotExist() {
        LocalDate date = LocalDate.parse("2024-11-29");
        CreateWaitingRequest request = new CreateWaitingRequest(date, reservationTime.getId(), theme.getId());

        assertThatThrownBy(() -> waitingService.createWaiting(authInfo, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("테마이름 테마는 바로 예약 가능하여 대기가 불가능합니다.");
    }

    @Test
    @DisplayName("예약 대기 생성 시 같은 테마, 같은 날짜, 같은 시간에 내 예약이 있는 경우 예외를 반환한다.")
    void createWaitingWhenAlreadyHasReservation() {
        LocalDate date = LocalDate.parse("2024-11-23");
        reservationRepository.save(new Reservation(member, date, reservationTime, theme));
        CreateWaitingRequest request = new CreateWaitingRequest(date, reservationTime.getId(), theme.getId());

        assertThatThrownBy(() -> waitingService.createWaiting(authInfo, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 본인의 예약이 존재하여 대기를 생성할 수 없습니다.");
    }

    @Test
    @DisplayName("예약 대기 생성 시 같은 테마, 같은 날짜, 같은 시간에 내 예약 대기가 있는 경우 예외를 반환한다.")
    void createWaitingWhenAlreadyHasWaiting() {
        LocalDate date = LocalDate.parse("2024-11-23");
        waitingRepository.save(new Waiting(member, date, reservationTime, theme));
        CreateWaitingRequest request = new CreateWaitingRequest(date, reservationTime.getId(), theme.getId());

        assertThatThrownBy(() -> waitingService.createWaiting(authInfo, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 본인의 대기가 존재하여 대기를 생성할 수 없습니다.");
    }

    @Test
    @DisplayName("방탈출 예약 목록을 조회한다.")
    void getReservationTimes() {
        LocalDate date = LocalDate.parse("2024-11-23");
        Member anotherMember = memberRepository.save(MemberFixture.getOne());
        waitingRepository.save(new Waiting(member, date, reservationTime, theme));
        waitingRepository.save(new Waiting(anotherMember, date, reservationTime, theme));

        List<FindWaitingResponse> waitings = waitingService.getWaitings();

        assertAll(
                () -> assertThat(waitings).hasSize(2),
                () -> assertThat(waitings.get(0).member().name()).isEqualTo("몰리"),
                () -> assertThat(waitings.get(1).member().name()).isEqualTo("name")
        );
    }

    @Test
    @DisplayName("방탈출 예약 대기 하나를 삭제한다.")
    void deleteWaiting() {
        LocalDate date = LocalDate.parse("2024-11-23");
        Waiting waiting = waitingRepository.save(new Waiting(member, date, reservationTime, theme));

        waitingService.deleteWaiting(waiting.getId());

        List<Waiting> waitings = waitingRepository.findAll();
        assertThat(waitings).isEmpty();
    }
}
