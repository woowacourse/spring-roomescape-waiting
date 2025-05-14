package roomescape.unit.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.dto.request.CreateReservationRequest;
import roomescape.entity.LoginMember;
import roomescape.entity.Member;
import roomescape.entity.Reservation;
import roomescape.entity.ReservationTime;
import roomescape.entity.Theme;
import roomescape.exception.custom.InvalidReservationException;
import roomescape.global.ReservationStatus;
import roomescape.global.Role;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.ReservationService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReservationServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationTimeRepository reservationTimeRepository;

    @Mock
    private ThemeRepository themeRepository;

    @InjectMocks
    private ReservationService reservationService;

    private Member member;
    private LoginMember loginMember;
    private ReservationTime time;
    private Theme theme;

    @BeforeEach
    void setup() {
        member = new Member(1L, "test", "test@email.com", "1234", Role.USER);
        loginMember = new LoginMember(member.getId(), member.getName(), member.getRole());
        time = new ReservationTime(1L, LocalTime.of(10, 0));
        theme = new Theme(1L, "name", "description", "thumbnail");
    }

    @Test
    void 예약_전체를_조회할_수_있다() {
        //given

        Reservation reservation = new Reservation(1L, member, LocalDate.now(), time, theme, ReservationStatus.RESERVED);
        Reservation reservation2 = new Reservation(2L, new Member(2L, "test2", "test2@email.com", "1234", Role.USER), LocalDate.now(), time, theme, ReservationStatus.RESERVED);

        when(reservationRepository.findAll())
                .thenReturn(List.of(reservation, reservation2));
        //when
        List<Reservation> actual = reservationService.findAll();

        //then
        assertThat(actual).hasSize(2);
        assertThat(actual).extracting("name").containsExactlyInAnyOrder("test", "test2");
    }

    @Test
    void 예약을_추가한다() {
        //given
        when(reservationRepository.save(any(Reservation.class)))
                    .thenReturn(new Reservation(1L, member, LocalDate.now().plusDays(1), time, theme, ReservationStatus.RESERVED));

        CreateReservationRequest request = new CreateReservationRequest(LocalDate.now().plusDays(1), time.getId(), theme.getId());
        when(memberRepository.findById(any(Long.class))).thenReturn(Optional.of(member));
        when(reservationTimeRepository.findById(any(Long.class))).thenReturn(Optional.of(time));
        when(themeRepository.findById(any(Long.class))).thenReturn(Optional.of(theme));
        //when
        Reservation actual = reservationService.addReservation(request, loginMember);

        //then
        assertThat(actual.getReservationTime().getId()).isEqualTo(time.getId());
        assertThat(actual.getTheme().getId()).isEqualTo(theme.getId());
        assertThat(actual.getMember().getId()).isEqualTo(member.getId());
    }

    @Test
    void 예약을_삭제할_수_있다() {
        //when
        reservationService.deleteReservation(1L);

        //then
        verify(reservationRepository, times(1)).deleteById(1L);
    }

    @Test
    void 중복_예약은_불가능하다() {
        //given
        when(reservationRepository.existsByTimeIdAndThemeIdAndDate(any(Long.class), any(Long.class), any(LocalDate.class)))
                .thenReturn(true);
        when(memberRepository.findById(any(Long.class))).thenReturn(Optional.of(member));
        when(reservationTimeRepository.findById(any(Long.class))).thenReturn(Optional.of(time));
        when(themeRepository.findById(any(Long.class))).thenReturn(Optional.of(theme));

        //when & then
        assertThatThrownBy(() -> reservationService.addReservation(
                new CreateReservationRequest(LocalDate.now(), time.getId(), theme.getId()), loginMember))
                .isInstanceOf(InvalidReservationException.class);
    }

    @Test
    void 대상_유저의_예약_전체를_조회할_수_있다() {
        //given

        Reservation reservation = new Reservation(1L, member, LocalDate.now(), time, theme, ReservationStatus.RESERVED);
        Reservation reservation2 = new Reservation(2L, new Member(2L, "test2", "test2@email.com", "1234", Role.USER), LocalDate.now(), time, theme, ReservationStatus.RESERVED);

        when(reservationRepository.findAllByMemberId(1L))
                .thenReturn(List.of(reservation));
        //when
        List<Reservation> actual = reservationService.findAllReservationByMember(1L);

        //then
        assertThat(actual).hasSize(1);
        assertThat(actual).extracting("name").containsExactlyInAnyOrder("test");
    }

}
