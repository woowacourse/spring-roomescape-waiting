package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.auth.dto.LoginMember;
import roomescape.exception.NotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.member.domain.Password;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.RoomEscapeInformation;
import roomescape.reservation.domain.WaitingReservation;
import roomescape.reservation.dto.WaitingReservationRequest;
import roomescape.reservation.dto.WaitingReservationResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.RoomEscapeInformationRepository;
import roomescape.reservation.repository.WaitingReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

@DataJpaTest
@Sql("/data.sql")
class WaitingReservationServiceTest {

    @Autowired
    private WaitingReservationRepository waitingReservationRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RoomEscapeInformationRepository roomEscapeInformationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    private WaitingReservationService service;
    private Member member;
    private Theme theme;
    private ReservationTime time;
    private RoomEscapeInformation roomEscapeInformation;

    @BeforeEach
    void setUp() {
        service = new WaitingReservationService(
                reservationRepository,
                waitingReservationRepository,
                memberRepository,
                roomEscapeInformationRepository
        );

        member = Member.builder()
                .name("대기자")
                .email("waiting@example.com")
                .password(Password.createForMember("pass123"))
                .role(MemberRole.MEMBER)
                .build();
        memberRepository.save(member);

        theme = Theme.of("대기테마", "대기설명", "대기썸네일");
        themeRepository.save(theme);

        time = ReservationTime.from(LocalTime.of(15, 0));
        reservationTimeRepository.save(time);

        roomEscapeInformation = RoomEscapeInformation.builder()
                .date(LocalDate.of(2999, 12, 31))
                .time(time)
                .theme(theme)
                .build();
        roomEscapeInformationRepository.save(roomEscapeInformation);
    }

    @Test
    void 대기_예약이_정상적으로_저장된다() {
        // given
        WaitingReservationRequest request = new WaitingReservationRequest(
                LocalDate.of(2999, 12, 31),
                time.getId(),
                theme.getId()
        );
        LoginMember loginMember = new LoginMember(member.getId(), member.getName(), member.getEmail(),
                member.getRole());

        // when
        WaitingReservationResponse response = service.save(request, loginMember);

        // then
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(response.id()).isNotNull();
            soft.assertThat(response.date()).isEqualTo(request.date());
            soft.assertThat(response.member().name()).isEqualTo(member.getName());
            soft.assertThat(response.theme().name()).isEqualTo(theme.getName());
        });
    }

    @Test
    void 존재하지_않는_회원으로_대기_예약시_예외발생() {
        // given
        WaitingReservationRequest request = new WaitingReservationRequest(
                LocalDate.of(2999, 12, 31),
                time.getId(),
                theme.getId()
        );
        LoginMember nonExistentMember = new LoginMember(9999L, "존재안함", "none@example.com", MemberRole.MEMBER);

        // when & then
        assertThatThrownBy(() -> service.save(request, nonExistentMember))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("존재하지 않는 멤버입니다.");
    }

    @Test
    void 대기_예약_삭제시_연관된_방탈출_정보도_삭제된다() {
        // given
        WaitingReservation waitingReservation = WaitingReservation.builder()
                .roomEscapeInformation(roomEscapeInformation)
                .member(member)
                .build();
        waitingReservationRepository.save(waitingReservation);

        // when
        service.deleteById(waitingReservation.getId());

        // then
        assertThat(waitingReservationRepository.findById(waitingReservation.getId())).isEmpty();
        assertThat(roomEscapeInformationRepository.findById(roomEscapeInformation.getId())).isEmpty();
    }

//    @Test
//    void ID로_대기_예약을_찾을_수_있다() {
//        // given
//        WaitingReservation waitingReservation = WaitingReservation.builder()
//                .roomEscapeInformation(roomEscapeInformation)
//                .member(member)
//                .build();
//        WaitingReservation saved = waitingReservationRepository.save(waitingReservation);
//
//        // when
//        WaitingReservation found = service.findWaitingReservationById(saved.getId());
//
//        // then
//        assertThat(found).isNotNull();
//        assertThat(found.getId()).isEqualTo(saved.getId());
//    }
//
//    @Test
//    void 존재하지_않는_대기_예약_ID로_조회시_예외발생() {
//        // when & then
//        assertThatThrownBy(() -> service.findWaitingReservationById(9999L))
//                .isInstanceOf(NotFoundException.class)
//                .hasMessage("존재하지 않는 대기 예약입니다.");
//    }
} 
