package roomescape.reservation.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.exception.DuplicateContentException;
import roomescape.exception.InvalidRequestException;
import roomescape.exception.NotFoundException;
import roomescape.fixture.FakeMemberRepositoryFixture;
import roomescape.fixture.FakeReservationTimeRepositoryFixture;
import roomescape.fixture.FakeThemeRepositoryFixture;
import roomescape.fixture.FakeWaitingRepositoryFixture;
import roomescape.fixture.LoginMemberFixture;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.dto.NameResponse;
import roomescape.reservation.dto.WaitingRequest;
import roomescape.reservation.dto.WaitingResponse;
import roomescape.reservation.repository.WaitingRepository;
import roomescape.reservationtime.dto.ReservationTimeResponse;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.dto.ThemeResponse;
import roomescape.theme.repository.ThemeRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class WaitingServiceTest {

    private final ReservationTimeRepository reservationTimeRepository = FakeReservationTimeRepositoryFixture.create();
    private final ThemeRepository themeRepository = FakeThemeRepositoryFixture.create();
    private final MemberRepository memberRepository = FakeMemberRepositoryFixture.create();
    private final ReservationChecker reservationChecker = new ReservationChecker(reservationTimeRepository, themeRepository, memberRepository);
    private final WaitingRepository waitingRepository = FakeWaitingRepositoryFixture.create();
    private final WaitingService waitingService = new WaitingService(waitingRepository, reservationChecker);

    @Nested
    @DisplayName("예약대기 조회")
    class FindWaiting {

        @DisplayName("모든 Waiting을 조회할 수 있다")
        @Test
        void findAllWaitingsTest() {
            // when
            List<WaitingResponse> responses = waitingService.findAll();

            // then
            assertAll(
                    () -> assertThat(responses).hasSize(1),
                    () -> assertThat(responses).extracting("member")
                            .containsExactly(new NameResponse(1L, "어드민")),
                    () -> assertThat(responses).extracting("date")
                            .containsExactly(LocalDate.now().plusDays(7)),
                    () -> assertThat(responses).extracting("theme")
                            .containsExactly(new ThemeResponse(1L, "우테코", "방탈출", "https://")),
                    () -> assertThat(responses).extracting("time")
                            .containsExactly(new ReservationTimeResponse(1L, LocalTime.of(10, 0)))
            );
        }
    }

    @Nested
    @DisplayName("예약대기 생성")
    class CreateWaiting {

        @DisplayName("요청에 따라 Waiting을 생성 할 수 있다")
        @Test
        void createWaitingTest() {
            // given
            LocalDate date = LocalDate.now().plusDays(7);
            Member member = LoginMemberFixture.getUser();
            WaitingRequest requestDto = new WaitingRequest(date, 1L, 1L);

            // when
            WaitingResponse responseDto = waitingService.createWaiting(requestDto, member);

            // then
            assertAll(
                    () -> assertThat(responseDto.id()).isEqualTo(2L),
                    () -> assertThat(responseDto.date()).isEqualTo(date),
                    () -> assertThat(responseDto.member().name()).isEqualTo("회원"),
                    () -> assertThat(responseDto.time().startAt()).isEqualTo(LocalTime.of(10, 0)),
                    () -> assertThat(responseDto.theme().name()).isEqualTo("우테코")
            );
        }

        @DisplayName("요청한 ReservationTime의 id가 존재하지 않으면 Waiting을 생성할 수 없다")
        @Test
        void invalidReservationTimeIdTest() {
            // given
            Member member = LoginMemberFixture.getUser();
            WaitingRequest requestDto = new WaitingRequest(LocalDate.now().plusDays(7), 10L, 1L);

            // when & then
            assertThatThrownBy(() -> waitingService.createWaiting(requestDto, member))
                    .isInstanceOf(NotFoundException.class);
        }

        @DisplayName("요청한 Theme의 id가 존재하지 않으면 Waiting을 생성할 수 없다")
        @Test
        void invalidThemeIdTest() {
            // given
            Member member = LoginMemberFixture.getUser();
            WaitingRequest requestDto = new WaitingRequest(LocalDate.now().plusDays(7), 1L, 10L);

            // when & then
            assertThatThrownBy(() -> waitingService.createWaiting(requestDto, member))
                    .isInstanceOf(NotFoundException.class);
        }

        @DisplayName("이미 동일한 날짜, 시간, 테마, 멤버 id에 예약대기가 있으면 생성할 수 없다")
        @Test
        void createDuplicateWaitingTest() {
            // given
            Member member = LoginMemberFixture.getUser();
            WaitingRequest requestDto = new WaitingRequest(LocalDate.now().plusDays(7), 1L, 1L);

            // when
            waitingService.createWaiting(requestDto, member);

            // then
            assertThatThrownBy(() -> waitingService.createWaiting(requestDto, member))
                    .isInstanceOf(DuplicateContentException.class);
        }

        @DisplayName("이미 지난 날짜의 경우 예약대기 생성이 불가능 하다")
        @Test
        void createInvalidDateTest() {
            // given
            Member member = LoginMemberFixture.getUser();
            WaitingRequest requestDto = new WaitingRequest(LocalDate.now().minusDays(7), 1L, 1L);

            // when & then
            assertThatThrownBy(() -> waitingService.createWaiting(requestDto, member))
                    .isInstanceOf(InvalidRequestException.class);
        }
    }

    @Nested
    @DisplayName("예약대기 삭제")
    class DeleteWaiting {

        @DisplayName("Waiting을 삭제할 수 있다")
        @Test
        void deleteWaitingTest() {
            // given
            Member member = LoginMemberFixture.getAdmin();
            long targetId = 1L;

            // when
            waitingService.deleteWaiting(targetId, member.getId());

            // then
            assertThat(waitingService.findAll()).isEmpty();
        }

        @DisplayName("존재하지 않는 Id의 Waiting을 삭제할 수 없다")
        @Test
        void deleteInvalidWaitingIdTest() {
            // given
            Member member = LoginMemberFixture.getAdmin();
            long targetId = 10L;

            // when & then
            assertThatThrownBy(() -> waitingService.deleteWaiting(targetId, member.getId()))
                    .isInstanceOf(NotFoundException.class);
        }
    }
}
