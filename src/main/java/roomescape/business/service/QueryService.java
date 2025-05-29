package roomescape.business.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.business.domain.Member;
import roomescape.business.domain.Reservation;
import roomescape.business.domain.ReservationTime;
import roomescape.business.domain.Theme;
import roomescape.exception.NotFoundException;
import roomescape.infrastructure.repository.MemberRepository;
import roomescape.infrastructure.repository.ReservationRepository;
import roomescape.infrastructure.repository.ReservationTimeRepository;
import roomescape.infrastructure.repository.ThemeRepository;

@Service
public class QueryService {

    private final MemberRepository memberRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public QueryService(final MemberRepository memberRepository,
                        final ReservationTimeRepository reservationTimeRepository,
                        final ThemeRepository themeRepository, final ReservationRepository reservationRepository) {

        this.memberRepository = memberRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public Reservation getReservationById(final Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("해당하는 예약을 찾을 수 없습니다. 예약 id: %d".formatted(id)));
    }

    public Member getMemberById(final Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("해당하는 사용자를 찾을 수 없습니다. 사용자 id: %d".formatted(memberId)));
    }

    public ReservationTime getReservationTimeById(final Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new NotFoundException("해당하는 방탈출 예약 시간을 찾을 수 없습니다. 방탈출 id: %d".formatted(timeId)));
    }

    public Theme getThemeById(final Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new NotFoundException("해당하는 테마를 찾을 수 없습니다. 테마 id: %d".formatted(themeId)));
    }

    public Reservation getReservationByDateAndTimeIdAndThemeId(final LocalDate date, final Long timeId,
                                                               final Long themeId) {
        return reservationRepository.findByDateAndTimeIdAndThemeId(date, timeId, themeId)
                .orElseThrow(() -> new NotFoundException("예약이 존재하지 않아 예약 대기를 할 수 없습니다."));
    }

    public List<Reservation> findByDateBetween(LocalDate startDate, LocalDate endDate) {
        return reservationRepository.findByDateBetween(startDate, endDate);
    }

    public List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId) {
        return reservationRepository.findByDateAndThemeId(date, themeId);
    }
}
