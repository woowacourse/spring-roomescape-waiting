package roomescape.service.waiting;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.domain.waiting.Waiting;
import roomescape.dto.waiting.WaitingResponseDto;
import roomescape.exception.DuplicateContentException;
import roomescape.repository.*;
import roomescape.service.dto.WaitingCreateDto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class WaitingCommandServiceTest {

    @InjectMocks
    private WaitingCommandService waitingCommandService;

    @Mock
    private JpaWaitingRepository waitingRepository;
    @Mock
    private JpaThemeRepository themeRepository;
    @Mock
    private JpaReservationTimeRepository timeRepository;
    @Mock
    private JpaMemberRepository memberRepository;
    @Mock
    private JpaReservationRepository reservationRepository;

    @DisplayName("예약 대기 생성")
    @Nested
    class WaitingRegisterTest{

        ReservationTime time = new ReservationTime(1L, LocalTime.of(10,0));
        Theme theme = new Theme(1L, "테마", "설명", "썸네일");
        Member member = new Member(1L, "이름", "email@domain.com", Role.USER, "password");
        Waiting saved = new Waiting(1L, member, LocalDate.now(), time, theme);

        @AfterEach
        void tearDown(){
            timeRepository.deleteAll();
            themeRepository.deleteAll();
            memberRepository.deleteAll();
            waitingRepository.deleteAll();
        }

        @Test
        @DisplayName("예약 대기를 생성할 수 있다")
        void createWaitingTest(){
            //given
            WaitingCreateDto dto = new WaitingCreateDto(
                    LocalDate.now(),
                    1L,
                    1L,
                    1L
            );

            when(timeRepository.findById(1L)).thenReturn(Optional.of(time));
            when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
            when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));
            when(waitingRepository.save(any(Waiting.class))).thenReturn(saved);
            when(reservationRepository.existsFor(any(LocalDate.class), any(Long.class), any(Long.class), any(Long.class))).thenReturn(false);
            when(waitingRepository.existsFor(any(LocalDate.class), any(Long.class), any(Long.class), any(Long.class))).thenReturn(false);

            //when
            WaitingResponseDto waitingResponseDto = waitingCommandService.registerWaiting(dto);

            //then
            assertThat(waitingResponseDto).isNotNull();
            assertThat(waitingResponseDto.id()).isEqualTo(1L);
        }

        @Test
        @DisplayName("이미 예약대기를 했다면 예약대기 신청을 할 수 없다")
        void duplicateWaitingTest(){
            //given
            WaitingCreateDto dto = new WaitingCreateDto(
                    LocalDate.now(),
                    1L,
                    1L,
                    1L
            );

            when(reservationRepository.existsFor(any(LocalDate.class), any(Long.class), any(Long.class), any(Long.class))).thenReturn(false);
            when(waitingRepository.existsFor(any(LocalDate.class), any(Long.class), any(Long.class), any(Long.class)))
                    .thenReturn(true);

            //when, then
            Assertions.assertThatThrownBy(() -> waitingCommandService.registerWaiting(dto))
                    .isInstanceOf(DuplicateContentException.class);
        }

        @DisplayName("이미 예약을 했다면 예약대기 신청을 할 수 없다")
        @Test
        void duplicateReservationTest(){
            //given
            WaitingCreateDto dto = new WaitingCreateDto(
                    LocalDate.now(),
                    1L,
                    1L,
                    1L
            );

            when(reservationRepository.existsFor(any(LocalDate.class), any(Long.class), any(Long.class), any(Long.class))).thenReturn(true);

            //when, then
            Assertions.assertThatThrownBy(() -> waitingCommandService.registerWaiting(dto))
                    .isInstanceOf(DuplicateContentException.class);
        }
    }
}
