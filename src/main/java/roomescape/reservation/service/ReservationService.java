package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.DataExistException;
import roomescape.common.exception.DataNotFoundException;
import roomescape.common.exception.PastDateException;
import roomescape.common.exception.ReservationNotAllowedException;
import roomescape.common.exception.WaitingNotAllowedException;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.dto.AvailableReservationTime;
import roomescape.reservation.repository.ReservationRepositoryInterface;
import roomescape.reservation.repository.ReservationTimeRepositoryInterface;
import roomescape.reservation.repository.WaitingRepositoryInterface;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepositoryInterface;

@RequiredArgsConstructor
@Service
public class ReservationService {

    private final ReservationRepositoryInterface reservationRepository;
    private final ReservationTimeRepositoryInterface reservationTimeRepository;
    private final ThemeRepositoryInterface themeRepository;
    private final WaitingRepositoryInterface waitingRepository;

    @Transactional
    public Reservation save(final Member member, final LocalDate date, final Long timeId, final Long themeId) {
        validatePastDate(date);
        final ReservationTime reservationTime = findReservationTimeById(timeId);
        final Theme theme = findThemeById(themeId);
        validateExistReservation(date, reservationTime, theme);
        validateExistWaiting(date, reservationTime, theme);
        final Reservation reservation = new Reservation(member, date, reservationTime, theme);

        return reservationRepository.save(reservation);
    }

    //TODO : 어드민만 사용하는 메서드임 사실상, 추후에 어드민 서비스로 분리 하는 것이 적절해 보인다.
    @Transactional
    public void deleteById(final Long id) {
        //TODO : 대기 정보가 있는지 파악하기 O
        //TODO : 대기 정보가 있으면 첫 번째 대기 정보를 예약으로 바꾸기 O
        //TODO : 대기 삭제하기
        final Reservation reservation = findReservationById(id);
        final Theme theme = reservation.getTheme();
        final LocalDate date = reservation.getDate();
        final ReservationTime time = reservation.getTime();
        if (waitingRepository.existsByDateAndTimeAndTheme(
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme()
        )) {
            //대기 정보가 있는 것임
            waitingRepository.findFirstByThemeAndDateAndTimeOrderByIdAsc(theme, date, time)
                    .ifPresent(waiting -> {
                        //TODO : 대기 정보를 예약으로 바꾸기
                        reservationRepository.save(new Reservation(
                                waiting.getMember(),
                                waiting.getDate(),
                                waiting.getTime(),
                                waiting.getTheme()
                        ));
                        //TODO : 대기 삭제하기
                        waitingRepository.deleteById(waiting.getId());
                    });
        }

        reservationRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<AvailableReservationTime> findAvailableReservationTimes(final LocalDate date, final Long themeId) {
        final List<AvailableReservationTime> availableReservationTimes = new ArrayList<>();
        final List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        final Theme theme = findThemeById(themeId);

        for (ReservationTime reservationTime : reservationTimes) {
            availableReservationTimes.add(new AvailableReservationTime(
                    reservationTime.getId(),
                    reservationTime.getStartAt(),
                    reservationRepository.existsByDateAndTimeAndTheme(
                            date,
                            reservationTime,
                            theme
                    ))
            );
        }

        return availableReservationTimes;
    }

    @Transactional(readOnly = true)
    public List<Reservation> findByMember(final Member member) {
        return reservationRepository.findByMember(member);
    }

    @Transactional(readOnly = true)
    public List<Waiting> findWaitingByMember(final Member member) {
        return waitingRepository.findByMember(member);
    }

    @Transactional
    public void deleteWaitingById(final Long id) {
        final Waiting waiting = waitingRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("해당 대기 데이터가 존재하지 않습니다. id = " + id));

        waitingRepository.deleteById(waiting.getId());
    }

    @Transactional(readOnly = true)
    public long getRankInWaiting(final Waiting waiting) {
        return waitingRepository.countBefore(
                waiting.getTheme(),
                waiting.getDate(),
                waiting.getTime(),
                waiting.getId()
        ) + 1;
    }

    @Transactional
    public Waiting createWaitingReservation(
            final Member member,
            final LocalDate date,
            final Long timeId,
            final Long themeId) {
        validatePastDate(date);
        final ReservationTime reservationTime = findReservationTimeById(timeId);
        final Theme theme = findThemeById(themeId);

        boolean reservationExists = reservationRepository.existsByDateAndTimeAndTheme(date, reservationTime, theme);

        if (reservationExists) {
            final Waiting waiting = new Waiting(member, reservationTime, theme, date);
            return waitingRepository.save(waiting);
        }

        throw new WaitingNotAllowedException("예약이 존재하지 않아 대기열에 등록 할 수 없습니다.");
    }

    private void validateExistWaiting(final LocalDate date, final ReservationTime reservationTime, final Theme theme) {
        if (waitingRepository.existsByDateAndTimeAndTheme(date, reservationTime, theme)) {
            throw new ReservationNotAllowedException("예약 대기가 존재해 예약을 할 수 없습니다.");
        }
    }

    private void validateExistReservation(final LocalDate date, final ReservationTime time, final Theme theme) {
        if (reservationRepository.existsByDateAndTimeAndTheme(date, time, theme)) {
            throw new DataExistException("해당 시간에 이미 예약된 테마입니다.");
        }
    }

    private void validatePastDate(final LocalDate date) {
        if (!date.isAfter(LocalDate.now())) {
            throw new PastDateException(date);
        }
    }

    private ReservationTime findReservationTimeById(final Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new DataNotFoundException("해당 예약 시간 데이터가 존재하지 않습니다. id = " + timeId));
    }

    private Theme findThemeById(final Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new DataNotFoundException("해당 테마 데이터가 존재하지 않습니다. id = " + themeId));
    }

    private Reservation findReservationById(final Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new DataNotFoundException("해당 예약 데이터가 존재하지 않습니다. id = " + reservationId));
    }
}
