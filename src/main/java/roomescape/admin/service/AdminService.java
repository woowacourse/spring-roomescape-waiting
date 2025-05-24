package roomescape.admin.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.DataExistException;
import roomescape.common.exception.DataNotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class AdminService {

        private final ReservationRepository reservationRepository;
        private final ReservationTimeRepository reservationTimeRepository;
        private final ThemeRepository themeRepository;
        private final MemberRepository memberRepository;

        @Transactional
        public Long saveByAdmin(final LocalDate date, final Long themeId, final Long timeId, final Long memberId) {
                Member member = memberRepository.findById(memberId)
                                .orElseThrow(() -> new DataNotFoundException("해당 회원 데이터가 존재하지 않습니다. id = " + memberId));
                ReservationTime time = reservationTimeRepository.findById(timeId)
                                .orElseThrow(() -> new DataNotFoundException("해당 예약 시간이 존재하지 않습니다."));
                Theme theme = themeRepository.findById(themeId)
                                .orElseThrow(() -> new DataNotFoundException("해당 테마가 존재하지 않습니다."));

                ReservationSlot slot = new ReservationSlot(date, time, theme);

                if (reservationRepository.existsConfirmedReservationBySlot(slot)) {
                        throw new DataExistException("해당 시간에 이미 예약된 테마입니다.");
                }

                Reservation reservation = new Reservation(member, slot, ReservationStatus.CONFIRMED);
                Reservation savedReservation = reservationRepository.save(reservation);

                return savedReservation.getId();
        }

        public Reservation getById(final Long id) {
                return reservationRepository.findById(id)
                                .orElseThrow(() -> new DataNotFoundException("해당 예약 데이터가 존재하지 않습니다. id = " + id));
        }

        public List<Reservation> findByInFromTo(final Long themeId, final Long memberId, final LocalDate dateFrom,
                        final LocalDate dateTo) {
                Theme theme = themeRepository.findById(themeId)
                                .orElseThrow(() -> new DataNotFoundException("해당 테마 데이터가 존재하지 않습니다. id = " + themeId));
                Member member = memberRepository.findById(memberId)
                                .orElseThrow(() -> new DataNotFoundException("해당 회원 데이터가 존재하지 않습니다. id = " + memberId));

                return reservationRepository.findByThemeAndMemberAndDateBetween(theme, member, dateFrom, dateTo);
        }
}
