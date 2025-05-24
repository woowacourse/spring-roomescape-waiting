package roomescape.waiting.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.global.auth.dto.LoginMember;
import roomescape.global.error.exception.BadRequestException;
import roomescape.global.error.exception.ForbiddenException;
import roomescape.member.entity.Member;
import roomescape.member.entity.RoleType;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.entity.Reservation;
import roomescape.reservation.entity.ReservationSlot;
import roomescape.reservation.entity.ReservationTime;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationSlotRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.theme.entity.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.waiting.dto.request.WaitingCreateRequest;
import roomescape.waiting.entity.Waiting;
import roomescape.waiting.repository.WaitingRepository;
import roomescape.waiting.service.WaitingService;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class WaitingIntegrationTest {

    @Autowired
    private WaitingService waitingService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private WaitingRepository waitingRepository;

    @Autowired
    private ReservationSlotRepository reservationSlotRepository;

    private LocalDate date;
    private Theme theme;
    private ReservationTime time;
    private ReservationSlot reservationSlot;

    private Member member;
    private Member otherMember;
    private Member admin;
    private LoginMember loginMember;

    @BeforeEach
    void setUp() {
        theme = themeRepository.save(new Theme("테마", "설명", "썸네일"));
        time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        date = LocalDate.now().plusDays(1);
        reservationSlot = reservationSlotRepository.save(new ReservationSlot(date, time, theme));

        member = memberRepository.save(new Member("훌라", "hula@email.com", "password", RoleType.USER));
        otherMember = memberRepository.save(new Member("미소", "miso@email.com", "password", RoleType.USER));
        admin = memberRepository.save(new Member("어드민", "admin@email.com", "password", RoleType.ADMIN));
        loginMember = new LoginMember(member.getId(), member.getPassword(), member.getRole());
    }

    @Test
    @DisplayName("예약 대기를 생성할 수 있다.")
    void createWaiting() {
        //given
        // 타인의 예약 생성
        var reservation = new Reservation(reservationSlot, otherMember);
        reservationRepository.save(reservation);

        var request = new WaitingCreateRequest(date, time.getId(), theme.getId());

        //when
        var response = waitingService.createWaiting(loginMember, request);

        //then
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("예약이 존재하지 않는다면 예외를 던진다.")
    void cantCreateWaitingWhenNotReserved() {
        //given
        var request = new WaitingCreateRequest(date, time.getId(), theme.getId());

        //when & then
        assertThatThrownBy(() -> waitingService.createWaiting(loginMember, request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("중복된 본인의 예약이 존재한다면 예외를 던진다.")
    void cantCreateWaitingWhenAlreadyReserved() {
        //given
        var request = new WaitingCreateRequest(date, time.getId(), theme.getId());

        //when & then
        assertThatThrownBy(() -> waitingService.createWaiting(loginMember, request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("중복된 본인의 예약 대기가 존재한다면 예외를 던진다.")
    void cantCreateWaitingWhenAlreadyWaiting() {
        //given
        // 타인의 예약 생성
        var reservation = new Reservation(reservationSlot, otherMember);
        reservationRepository.save(reservation);

        // 본인의 예약 대기 생성
        var waiting = new Waiting(reservationSlot, member);
        waitingRepository.save(waiting);

        var request = new WaitingCreateRequest(date, time.getId(), theme.getId());

        //when & then
        assertThatThrownBy(() -> waitingService.createWaiting(loginMember, request))
                .isInstanceOf(BadRequestException.class);
    }

    @DisplayName("본인의 예약 대기가 아니라면, 삭제 시 예외를 발생한다.")
    @Test
    void cantDeleteWaitingWhenNotMine() {
        //given
        var waiting = new Waiting(reservationSlot, otherMember);
        var savedWaiting = waitingRepository.save(waiting);

        var loginMember = new LoginMember(member.getId(), member.getPassword(), member.getRole());

        //when & then
        assertThatThrownBy(() -> waitingService.deleteWaiting(savedWaiting.getId(), loginMember))
                .isInstanceOf(ForbiddenException.class);
    }

    @DisplayName("어드민이라면, 본인 외의 예약 대기를 삭제할 수 있다.")
    @Test
    void deleteWaitingWhenAdmin() {
        //given
        var waiting = new Waiting(reservationSlot, otherMember);
        var savedWaiting = waitingRepository.save(waiting);

        var loginMember = new LoginMember(admin.getId(), admin.getPassword(), admin.getRole());

        // when & then
        assertThatCode(() -> waitingService.deleteWaiting(savedWaiting.getId(), loginMember))
                .doesNotThrowAnyException();
    }

    @DisplayName("예약이 존재할 때, 예약 대기를 승인한다.")
    @Test
    void acceptWaitingWhenAlreadyReserved() {
        //given
        // 타인의 예약 생성
        var reservation = new Reservation(reservationSlot, otherMember);
        reservationRepository.save(reservation);

        var waiting = new Waiting(reservationSlot, member);
        var savedWaiting = waitingRepository.save(waiting);

        //when & then
        assertThatCode(() -> waitingService.acceptWaiting(savedWaiting.getId()))
                .doesNotThrowAnyException();
    }
}
