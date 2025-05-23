package roomescape.admin.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.DataExistException;
import roomescape.common.exception.DataNotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepositoryInterface;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.repository.ReservationRepositoryInterface;
import roomescape.reservation.repository.ReservationTimeRepositoryInterface;
import roomescape.reservation.repository.WaitingRepositoryInterface;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepositoryInterface;

@RequiredArgsConstructor
@Service
public class AdminService {

    private final ReservationRepositoryInterface reservationRepository;
    private final ReservationTimeRepositoryInterface reservationTimeRepository;
    private final ThemeRepositoryInterface themeRepository;
    private final MemberRepositoryInterface memberRepository;
    private final WaitingRepositoryInterface waitingRepository;

    @Transactional
    public Long saveByAdmin(final LocalDate date, final Long themeId, final Long timeId, final Long memberId) {
        final ReservationTime reservationTime = findReservationTimeById(timeId);
        final Theme theme = findThemeById(themeId);
        final Member member = findMemberById(memberId);

        validateExistReservation(date, reservationTime, theme);

        final Reservation reservation = new Reservation(member, date, reservationTime, theme);
        final Reservation savedReservation = reservationRepository.save(reservation);

        return savedReservation.getId();
    }

    @Transactional
    public void deleteWaitingById(final Long id) {
        waitingRepository.findById(id)
                .ifPresentOrElse(
                        waiting -> waitingRepository.deleteById(id),
                        () -> {
                            throw new DataNotFoundException("해당 대기 데이터가 존재하지 않습니다. id = " + id);
                        }
                );
    }

    @Transactional(readOnly = true)
    public Reservation getById(final Long id) {
        return findReservationById(id);
    }

    @Transactional(readOnly = true)
    public List<Reservation> findByInFromTo(final Long themeId, final Long memberId, final LocalDate dateFrom,
                                            final LocalDate dateTo) {
        final Theme theme = findThemeById(themeId);
        final Member member = findMemberById(memberId);
        final List<Reservation> searchedReservations = reservationRepository
                .findByThemeAndMemberAndDateBetween(theme, member, dateFrom, dateTo);

        return searchedReservations;
    }

    @Transactional(readOnly = true)
    public List<Waiting> findAllWaitingReservations() {
        return waitingRepository.findAll();
    }

    private ReservationTime findReservationTimeById(final Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new DataNotFoundException("해당 예약 시간 데이터가 존재하지 않습니다. id = " + timeId));
    }

    private Member findMemberById(final Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new DataNotFoundException("해당 회원 데이터가 존재하지 않습니다. id = " + memberId));
    }

    private Theme findThemeById(final Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new DataNotFoundException("해당 테마 데이터가 존재하지 않습니다. id = " + themeId));
    }

    private void validateExistReservation(final LocalDate date, final ReservationTime time, final Theme theme) {
        if (reservationRepository.existsByDateAndTimeAndTheme(date, time, theme)) {
            throw new DataExistException("해당 시간에 이미 예약된 테마입니다.");
        }
    }

    private Reservation findReservationById(final Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new DataNotFoundException("해당 예약 데이터가 존재하지 않습니다. id = " + reservationId));
    }
}
