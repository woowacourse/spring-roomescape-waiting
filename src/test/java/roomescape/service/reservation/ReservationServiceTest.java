package roomescape.service.reservation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.member.Role;
import roomescape.domain.reservation.*;
import roomescape.exception.InvalidReservationException;
import roomescape.service.reservation.dto.AdminReservationRequest;
import roomescape.service.reservation.dto.ReservationFindRequest;
import roomescape.service.reservation.dto.ReservationResponse;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Sql(scripts = {"classpath:truncate-with-guests.sql"})
class ReservationServiceTest {
    @Autowired
    private ReservationService reservationService;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private MemberRepository memberRepository;
    private ReservationTime reservationTime;
    private Theme theme;
    private Member member;

    @BeforeEach
    void setUp() {
        reservationTime = reservationTimeRepository.save(new ReservationTime("10:00"));
        theme = themeRepository.save(new Theme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"));
        member = memberRepository.save(new Member("lini", "lini@email.com", "lini123", Role.GUEST));

    }

    @DisplayName("새로운 예약을 저장한다.")
    @Test
    void create() {
        //given
        String date = "2024-10-04";
        AdminReservationRequest adminReservationRequest = new AdminReservationRequest(date, member.getId(), reservationTime.getId(), theme.getId());

        //when
        ReservationResponse result = reservationService.create(adminReservationRequest);

        //then
        assertAll(
                () -> assertThat(result.id()).isNotZero(),
                () -> assertThat(result.time().id()).isEqualTo(reservationTime.getId()),
                () -> assertThat(result.theme().id()).isEqualTo(theme.getId())
        );
    }

    @DisplayName("모든 예약 내역을 조회한다.")
    @Test
    void findAll() {
        //given
        Reservation reservation = new Reservation(LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_DATE), member, reservationTime, theme);
        reservationRepository.save(reservation);

        //when
        List<ReservationResponse> reservations = reservationService.findAll();

        //then
        assertThat(reservations).hasSize(1);
    }

    @DisplayName("사용자 조건으로 예약 내역을 조회한다.")
    @Test
    void findByMember() {
        //given
        Reservation reservation = new Reservation(LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_DATE), member, reservationTime, theme);
        reservationRepository.save(reservation);
        ReservationFindRequest reservationFindRequest = new ReservationFindRequest(member.getId(), null, null, null);

        //when
        List<ReservationResponse> reservations = reservationService.findByCondition(reservationFindRequest);

        //then
        assertThat(reservations).hasSize(1);
    }

    @DisplayName("사용자와 테마 조건으로 예약 내역을 조회한다.")
    @Test
    void findByMemberAndTheme() {
        //given
        Reservation reservation = new Reservation(LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_DATE), member, reservationTime, theme);
        reservationRepository.save(reservation);
        long notMemberthemeId = theme.getId() + 1;
        ReservationFindRequest reservationFindRequest = new ReservationFindRequest(member.getId(), notMemberthemeId, null, null);

        //when
        List<ReservationResponse> reservations = reservationService.findByCondition(reservationFindRequest);

        //then
        assertThat(reservations).hasSize(0);
    }

    @DisplayName("id로 예약을 삭제한다.")
    @Test
    void deleteById() {
        //given
        Reservation reservation = new Reservation(LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_DATE), member, reservationTime, theme);
        Reservation target = reservationRepository.save(reservation);

        //when
        reservationService.deleteById(target.getId());

        //then
        assertThat(reservationService.findAll()).hasSize(0);
    }

    @DisplayName("해당 테마와 일정으로 예약이 존재하면 예외를 발생시킨다.")
    @Test
    void duplicatedReservation() {
        //given
        String date = LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_DATE);
        Reservation reservation = new Reservation(date, member, reservationTime, theme);
        reservationRepository.save(reservation);

        AdminReservationRequest adminReservationRequest = new AdminReservationRequest(date, member.getId(), reservationTime.getId(), theme.getId());

        //when & then
        assertThatThrownBy(() -> reservationService.create(adminReservationRequest))
                .isInstanceOf(InvalidReservationException.class)
                .hasMessage("선택하신 테마와 일정은 이미 예약이 존재합니다.");
    }

    @DisplayName("존재하지 않는 시간으로 예약을 추가하면 예외를 발생시킨다.")
    @Test
    void cannotCreateByUnknownTime() {
        //given
        String date = "2024-10-04";
        AdminReservationRequest adminReservationRequest = new AdminReservationRequest(date, member.getId(), 0, theme.getId());

        //when & then
        assertThatThrownBy(() -> reservationService.create(adminReservationRequest))
                .isInstanceOf(InvalidReservationException.class)
                .hasMessage("더이상 존재하지 않는 시간입니다.");
    }

    @DisplayName("존재하지 않는 테마로 예약을 추가하면 예외를 발생시킨다.")
    @Test
    void cannotCreateByUnknownTheme() {
        //given
        String date = "2024-10-04";
        AdminReservationRequest adminReservationRequest = new AdminReservationRequest(date, member.getId(), reservationTime.getId(), 0);

        //when & then
        assertThatThrownBy(() -> reservationService.create(adminReservationRequest))
                .isInstanceOf(InvalidReservationException.class)
                .hasMessage("더이상 존재하지 않는 테마입니다.");
    }
}
