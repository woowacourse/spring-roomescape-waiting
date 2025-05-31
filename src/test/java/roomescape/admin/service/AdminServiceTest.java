package roomescape.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

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
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

@DataJpaTest
class AdminServiceTest {

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

        private LocalDate now = LocalDate.now();

        @TestConfiguration
        static class TestConfig {
                @Bean
                public AdminService adminService(
                                final ReservationRepository reservationRepository,
                                final ReservationTimeRepository reservationTimeRepository,
                                final ThemeRepository themeRepository,
                                final MemberRepository memberRepository) {
                        return new AdminService(reservationRepository, reservationTimeRepository, themeRepository,
                                        memberRepository);
                }
        }

        @Test
        void 어드민이_예약을_생성한다() {
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
                Long reservationId = adminService.saveByAdmin(date, theme.getId(), time.getId(), member.getId());

                // then
                Reservation savedReservation = reservationRepository.findById(reservationId).get();
                assertThat(savedReservation.getMember().getId()).isEqualTo(member.getId());
                assertThat(savedReservation.getSlot().getDate()).isEqualTo(date);
                assertThat(savedReservation.getSlot().getTime().getId()).isEqualTo(time.getId());
                assertThat(savedReservation.getSlot().getTheme().getId()).isEqualTo(theme.getId());
                assertThat(savedReservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        }

        @Test
        void 어드민이_예약을_생성할_때_이미_예약된_시간이면_예외가_발생한다() {
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

                // 첫 번째 예약 생성
                adminService.saveByAdmin(date, theme.getId(), time.getId(), member1.getId());

                // when & then
                assertThatThrownBy(() -> adminService.saveByAdmin(date, theme.getId(), time.getId(), member2.getId()))
                                .isInstanceOf(DataExistException.class);
        }

        @Test
        void 어드민이_예약을_생성할_때_존재하지_않는_회원이면_예외가_발생한다() {
                // given
                ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
                Theme theme = themeRepository.save(new Theme("테마1", "테마1 설명", "https://example.com/theme1.jpg"));
                LocalDate date = now.plusDays(1);

                // when & then
                assertThatThrownBy(() -> adminService.saveByAdmin(date, theme.getId(), time.getId(), 999L))
                                .isInstanceOf(DataNotFoundException.class);
        }

        @Test
        void 어드민이_예약을_생성할_때_존재하지_않는_테마면_예외가_발생한다() {
                // given
                Member member = memberRepository.save(new Member(
                                new MemberName("재즈"),
                                new Email("t1@test.com"),
                                new Password("password"),
                                Role.USER));
                ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
                LocalDate date = now.plusDays(1);

                // when & then
                assertThatThrownBy(() -> adminService.saveByAdmin(date, 999L, time.getId(), member.getId()))
                                .isInstanceOf(DataNotFoundException.class);
        }

        @Test
        void 어드민이_예약을_생성할_때_존재하지_않는_시간이면_예외가_발생한다() {
                // given
                Member member = memberRepository.save(new Member(
                                new MemberName("재즈"),
                                new Email("t1@test.com"),
                                new Password("password"),
                                Role.USER));
                Theme theme = themeRepository.save(new Theme("테마1", "테마1 설명", "https://example.com/theme1.jpg"));
                LocalDate date = now.plusDays(1);

                // when & then
                assertThatThrownBy(() -> adminService.saveByAdmin(date, theme.getId(), 999L, member.getId()))
                                .isInstanceOf(DataNotFoundException.class);
        }

        @Test
        void 어드민이_예약을_조회한다() {
                // given
                Member member = memberRepository.save(new Member(
                                new MemberName("재즈"),
                                new Email("t1@test.com"),
                                new Password("password"),
                                Role.USER));
                ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
                Theme theme = themeRepository.save(new Theme("테마1", "테마1 설명", "https://example.com/theme1.jpg"));
                LocalDate date = now.plusDays(1);

                Long reservationId = adminService.saveByAdmin(date, theme.getId(), time.getId(), member.getId());

                // when
                Reservation foundReservation = adminService.getById(reservationId);

                // then
                assertThat(foundReservation.getId()).isEqualTo(reservationId);
                assertThat(foundReservation.getMember().getId()).isEqualTo(member.getId());
                assertThat(foundReservation.getSlot().getDate()).isEqualTo(date);
                assertThat(foundReservation.getSlot().getTime().getId()).isEqualTo(time.getId());
                assertThat(foundReservation.getSlot().getTheme().getId()).isEqualTo(theme.getId());
        }

        @Test
        void 어드민이_존재하지_않는_예약을_조회하면_예외가_발생한다() {
                // when & then
                assertThatThrownBy(() -> adminService.getById(999L)).isInstanceOf(DataNotFoundException.class);
        }

        @Test
        void 어드민이_조건에_맞는_예약을_조회한다() {
                // given
                Member member = memberRepository.save(new Member(
                                new MemberName("재즈"),
                                new Email("t1@test.com"),
                                new Password("password"),
                                Role.USER));
                ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
                Theme theme = themeRepository.save(new Theme("테마1", "테마1 설명", "https://example.com/theme1.jpg"));
                LocalDate date = now.plusDays(1);

                adminService.saveByAdmin(date, theme.getId(), time.getId(), member.getId());

                // when
                List<Reservation> reservations = adminService.findReservationsByThemeMemberAndDateRange(
                                theme.getId(),
                                member.getId(),
                                date.minusDays(1),
                                date.plusDays(1));

                // then
                assertThat(reservations).hasSize(1);
                Reservation foundReservation = reservations.get(0);
                assertThat(foundReservation.getMember().getId()).isEqualTo(member.getId());
                assertThat(foundReservation.getSlot().getDate()).isEqualTo(date);
                assertThat(foundReservation.getSlot().getTime().getId()).isEqualTo(time.getId());
                assertThat(foundReservation.getSlot().getTheme().getId()).isEqualTo(theme.getId());
        }

        @Test
        void 어드민이_조건에_맞지_않는_예약을_조회하면_빈_리스트를_반환한다() {
                // given
                Member member = memberRepository.save(new Member(
                                new MemberName("재즈"),
                                new Email("t1@test.com"),
                                new Password("password"),
                                Role.USER));
                ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
                Theme theme = themeRepository.save(new Theme("테마1", "테마1 설명", "https://example.com/theme1.jpg"));
                LocalDate date = now.plusDays(1);

                adminService.saveByAdmin(date, theme.getId(), time.getId(), member.getId());

                // when
                List<Reservation> reservations = adminService.findReservationsByThemeMemberAndDateRange(
                                theme.getId(),
                                member.getId(),
                                date.plusDays(2),
                                date.plusDays(3));

                // then
                assertThat(reservations).isEmpty();
        }
}