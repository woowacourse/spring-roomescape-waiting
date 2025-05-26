package roomescape.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.application.dto.ReservationCreateDto;
import roomescape.application.dto.ReservationDto;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Status;
import roomescape.domain.Theme;
import roomescape.domain.repository.ReservationRepository;
import roomescape.testFixture.StubHelper;

@ExtendWith(MockitoExtension.class)
public class ReservationCommandServiceTest {

    @InjectMocks
    private ReservationCommandService reservationService;

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private TimeService timeService;
    @Mock
    private ThemeService themeService;
    @Mock
    private MemberService memberService;
    @Mock
    private ClockProvider clockProvider;

    @DisplayName("사용자 예약 추가")
    @Test
    void registerReservation() {
        //given
        Theme theme = StubHelper.stubTheme(1L, themeService);
        ReservationTime time = StubHelper.stubTime(1L, timeService);
        Member member = StubHelper.stubMember(1L, memberService);
        Status status = Mockito.mock(Status.class);

        Reservation reservation = Reservation.of(1L, member, theme, LocalDate.now().plusDays(1), time, status);
        Mockito.doReturn(reservation).when(reservationRepository).save(Mockito.any(Reservation.class));
        Mockito.doReturn(LocalDateTime.now()).when(clockProvider).now();

        //when
        ReservationDto reservationDto = reservationService.registerReservation(new ReservationCreateDto(
                reservation.getDate(),
                theme.getId(),
                time.getId(),
                member.getId()
        ));

        //then
        Assertions.assertThat(reservationDto).isEqualTo(ReservationDto.from(reservation));
    }
}
