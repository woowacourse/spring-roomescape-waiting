package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import roomescape.common.exception.DataExistException;
import roomescape.common.exception.DataNotFoundException;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberName;
import roomescape.member.domain.Password;
import roomescape.member.domain.Role;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.dto.ReservationsWithRankResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

@DataJpaTest
class ReservationServiceTest {

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

        private LocalDate now = LocalDate.now();

        @TestConfiguration
        static class TestConfig {
                @Bean
                public ReservationService reservationService(
                                final ReservationRepository reservationRepository,
                                final ReservationTimeRepository reservationTimeRepository,
                                final ThemeRepository themeRepository,
                                final ReservationSlotService reservationSlotService) {
                        return new ReservationService(
                                        reservationSlotService,
                                        reservationRepository,
                                        reservationTimeRepository,
                                        themeRepository);
                }

                @Bean
                public ReservationSlotService reservationSlotService(
                                final ReservationTimeRepository reservationTimeRepository,
                                final ThemeRepository themeRepository,
                                final ReservationRepository reservationRepository) {
                        return new ReservationSlotService(reservationTimeRepository, themeRepository,
                                        reservationRepository);
                }
        }

        @Test
        void 예약_정보_목록을_조회한다() {
                // given
                Member member = memberRepository.save(new Member(
                                new MemberName("재즈"),
                                new Email("t1@test.com"),
                                new Password("password"),
                                Role.USER));
                ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
                Theme theme = themeRepository.save(new Theme("테마1", "테마1 설명", "https://example.com/theme1.jpg"));
                LocalDate date = now.plusDays(1);

                ReservationSlot slot = new ReservationSlot(date, time, theme);
                Reservation reservation = new Reservation(member, slot, ReservationStatus.CONFIRMED);
                reservationRepository.save(reservation);

                // when
                List<ReservationsWithRankResponse> responses = reservationService.findReservationsByMember(member);

                // then
                assertThat(responses).hasSize(1);
                ReservationsWithRankResponse response = responses.get(0);
                assertThat(response.reservationId()).isEqualTo(reservation.getId());
                assertThat(response.theme()).isEqualTo(theme.getName());
                assertThat(response.date()).isEqualTo(date);
                assertThat(response.time()).isEqualTo(time.getStartAt());
                assertThat(response.status()).isEqualTo(ReservationStatus.CONFIRMED.getDescription());
        }

        @Test
        void 예약_정보를_저장한다() {
                // given
                Member member = memberRepository.save(new Member(
                                new MemberName("재즈"),
                                new Email("t1@test.com"),
                                new Password("password"),
                                Role.USER));
                ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
                Theme theme = themeRepository.save(new Theme("테마1", "테마1 설명", "https://example.com/theme1.jpg"));
                LocalDate date = now.plusDays(1);

                // when
                Reservation savedReservation = reservationService.saveConfirm(member, date, time.getId(),
                                theme.getId());

                // then
                assertThat(savedReservation.getId()).isNotNull();
                assertThat(savedReservation.getMember()).isEqualTo(member);
                assertThat(savedReservation.getSlot().getDate()).isEqualTo(date);
                assertThat(savedReservation.getSlot().getTime()).isEqualTo(time);
                assertThat(savedReservation.getSlot().getTheme()).isEqualTo(theme);
                assertThat(savedReservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        }

        @Test
        void 예약_정보를_삭제한다() {
                // given
                Member member = memberRepository.save(new Member(
                                new MemberName("재즈"),
                                new Email("t1@test.com"),
                                new Password("password"),
                                Role.USER));
                ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 10)));
                Theme theme = themeRepository.save(new Theme("테마1", "테마1 설명", "https://example.com/theme1.jpg"));
                LocalDate date = now.plusDays(1);

                ReservationSlot slot = new ReservationSlot(date, time, theme);
                Reservation reservation = new Reservation(member, slot, ReservationStatus.CONFIRMED);
                reservationRepository.save(reservation);

                // when
                reservationService.cancelById(reservation.getId(), member);

                // then
                assertThat(reservationRepository.findById(reservation.getId()).get().getStatus())
                                .isEqualTo(ReservationStatus.CANCELED);
        }

        @Test
        void 예약_정보를_저장할_때_이미_예약된_시간이면_예외가_발생한다() {
                // given
                final Member member = new Member(
                                new MemberName("이스트"),
                                new Email("east@email.com"),
                                new Password("1234"),
                                Role.ADMIN);
                memberRepository.save(member);
                final LocalTime time = LocalTime.parse("20:00");
                final LocalDate date = LocalDate.parse("2025-11-28");
                final ReservationTime savedTime = reservationTimeRepository.save(new ReservationTime(time));

                final String themeName = "공포";
                final String description = "무섭다";
                final String thumbnail = "귀신사진";
                final Theme savedTheme = themeRepository.save(new Theme(themeName, description, thumbnail));

                reservationRepository.save(
                                new Reservation(
                                                member,
                                                new ReservationSlot(date, savedTime, savedTheme)));

                // when & then
                Assertions.assertThatThrownBy(
                                () -> reservationService.saveConfirm(member, date, savedTime.getId(),
                                                savedTheme.getId()))
                                .isInstanceOf(DataExistException.class);
        }

        @Test
        void 예약_정보를_저장할_때_예약시간이_존재하지않으면_예외가_발생한다() {
                // given
                final Member member = new Member(
                                new MemberName("이스트"),
                                new Email("east@email.com"),
                                new Password("1234"),
                                Role.ADMIN);
                final Member savedMember = memberRepository.save(member);
                final LocalDate date = LocalDate.parse("2025-11-28");
                final Long timeId = Long.MAX_VALUE;

                final String themeName = "공포";
                final String description = "무섭다";
                final String thumbnail = "귀신사진";
                final Theme savedTheme = themeRepository.save(new Theme(themeName, description, thumbnail));

                // when & then
                Assertions.assertThatThrownBy(
                                () -> reservationService.saveConfirm(savedMember, date, timeId, savedTheme.getId()))
                                .isInstanceOf(DataNotFoundException.class);
        }

        @Test
        void 한_테마의_날짜와_시간이_중복_될_수_없다() {
                // given
                final Member member = new Member(
                                new MemberName("이스트"),
                                new Email("east@email.com"),
                                new Password("1234"),
                                Role.ADMIN);
                final Member savedMember = memberRepository.save(member);
                final LocalTime time = LocalTime.parse("20:00");
                final LocalDate date = LocalDate.parse("2025-11-28");
                final ReservationTime savedTime = reservationTimeRepository.save(new ReservationTime(time));

                final String themeName = "공포";
                final String description = "무섭다";
                final String thumbnail = "귀신사진";
                final Theme savedTheme = themeRepository.save(new Theme(themeName, description, thumbnail));

                reservationRepository.save(
                                new Reservation(
                                                member, new ReservationSlot(date, savedTime, savedTheme)));

                // when & then
                Assertions.assertThatThrownBy(() -> reservationService.saveConfirm(
                                new Member(
                                                new MemberName("WooGa"),
                                                new Email("bowook316@gmail.com"),
                                                new Password("1234"),
                                                Role.USER),
                                date, savedTime.getId(), savedTheme.getId()))
                                .isInstanceOf(DataExistException.class);
        }

        @Test
        void 예약_대기_정보를_저장한다() {
                // given
                Member member = memberRepository.save(new Member(
                                new MemberName("재즈"),
                                new Email("t1@test.com"),
                                new Password("password"),
                                Role.USER));
                ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
                Theme theme = themeRepository.save(new Theme("테마1", "테마1 설명", "https://example.com/theme1.jpg"));
                LocalDate date = now.plusDays(1);

                // 이미 예약된 시간이 있는 상태
                Member otherMember = memberRepository.save(new Member(
                                new MemberName("우가"),
                                new Email("t2@test.com"),
                                new Password("password"),
                                Role.USER));
                reservationService.saveConfirm(otherMember, date, time.getId(), theme.getId());

                // when
                Reservation savedReservation = reservationService.saveWaiting(member, date, time.getId(),
                                theme.getId());

                // then
                assertThat(savedReservation.getId()).isNotNull();
                assertThat(savedReservation.getMember()).isEqualTo(member);
                assertThat(savedReservation.getSlot().getDate()).isEqualTo(date);
                assertThat(savedReservation.getSlot().getTime()).isEqualTo(time);
                assertThat(savedReservation.getSlot().getTheme()).isEqualTo(theme);
                assertThat(savedReservation.getStatus()).isEqualTo(ReservationStatus.WAITING);
        }

        @Test
        void 대기_상태의_예약을_취소한다() {
                // given
                Member member = memberRepository.save(new Member(
                                new MemberName("재즈"),
                                new Email("t1@test.com"),
                                new Password("password"),
                                Role.USER));
                Member member2 = memberRepository.save(new Member(
                        new MemberName("재즈2"),
                        new Email("t1@test.com"),
                        new Password("password"),
                        Role.USER));
                ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
                Theme theme = themeRepository.save(new Theme("테마1", "테마1 설명", "https://example.com/theme1.jpg"));
                LocalDate date = now.plusDays(1);
                Reservation confirmedReservation = reservationService.saveConfirm(member, date, time.getId(), theme.getId());

                // 대기 상태의 예약 생성
                Reservation waitingReservation = reservationService.saveWaiting(member2, date, time.getId(), theme.getId());

                // when
                reservationService.cancelWaitingById(waitingReservation.getId());

                // then
                Reservation canceledReservation = reservationRepository.findById(waitingReservation.getId()).get();
                assertThat(canceledReservation.getStatus()).isEqualTo(ReservationStatus.WAITING_CANCELED);
        }

        @Test
        void 대기_순서를_포함한_예약_목록을_조회한다() {
                // given
                Member member1 = memberRepository.save(new Member(
                                new MemberName("재즈"),
                                new Email("t1@test.com"),
                                new Password("password"),
                                Role.USER));
                Member member2 = memberRepository.save(new Member(
                                new MemberName("우가"),
                                new Email("t2@test.com"),
                                new Password("password"),
                                Role.USER));
                ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
                Theme theme = themeRepository.save(new Theme("테마1", "테마1 설명", "https://example.com/theme1.jpg"));
                LocalDate date = now.plusDays(1);

                // 첫 번째 예약 (확정)
                Reservation confirmedReservation = reservationService.saveConfirm(member1, date, time.getId(),
                                theme.getId());
                // 두 번째 예약 (대기)
                Reservation waitingReservation = reservationService.saveWaiting(member2, date, time.getId(),
                                theme.getId());

                // when
                List<ReservationsWithRankResponse> responses = reservationService.findReservationsByMember(member2);

                // then
                assertThat(responses).hasSize(1);
                ReservationsWithRankResponse response = responses.get(0);
                assertThat(response.reservationId()).isEqualTo(waitingReservation.getId());
                assertThat(response.theme()).isEqualTo(theme.getName());
                assertThat(response.date()).isEqualTo(date);
                assertThat(response.time()).isEqualTo(time.getStartAt());
                assertThat(response.status()).isEqualTo(ReservationStatus.WAITING.getDescription());
                assertThat(response.rank()).isEqualTo(1); // 첫 번째 대기 순서
        }
}
