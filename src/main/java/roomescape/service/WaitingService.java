package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.domain.WaitingWithRank;
import roomescape.handler.exception.CustomException;
import roomescape.handler.exception.ExceptionCode;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingRepository;
import roomescape.service.dto.request.ReservationRequest;
import roomescape.service.dto.response.WaitingResponse;

@Service
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public WaitingService(final WaitingRepository waitingRepository, final ReservationRepository reservationRepository,
                          final ReservationTimeRepository reservationTimeRepository,
                          final ThemeRepository themeRepository,
                          final MemberRepository memberRepository) {
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public WaitingResponse createWaiting(final ReservationRequest reservationRequest, final Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ExceptionCode.NOT_FOUND_USER));
        ReservationTime reservationTime = reservationTimeRepository.findById(reservationRequest.timeId())
                .orElseThrow(() -> new CustomException(ExceptionCode.NOT_FOUND_RESERVATION_TIME));
        Theme theme = themeRepository.findById(reservationRequest.themeId())
                .orElseThrow(() -> new CustomException(ExceptionCode.NOT_FOUND_THEME));

        if (waitingRepository.existsByMemberAndTimeAndDateAndTheme(member, reservationTime, reservationRequest.date(), theme)) {
            throw new CustomException(ExceptionCode.DUPLICATE_WAITING);
        }
        if (reservationRepository.existsByMemberAndTimeAndDate(member, reservationTime, reservationRequest.date())) {
            throw new CustomException(ExceptionCode.DUPLICATE_WAITING);
        }
        validateIsPastTime(reservationRequest.date(), reservationTime);

        Waiting waiting = reservationRequest.toWaiting(member, reservationTime, theme);
        Waiting savedWaiting = waitingRepository.save(waiting);
        return WaitingResponse.from(savedWaiting);
    }

    private void validateIsPastTime(LocalDate date, ReservationTime time) {
        LocalDateTime reservationDateTime = LocalDateTime.of(date, time.getStartAt());
        if (LocalDateTime.now().isAfter(reservationDateTime)) {
            throw new CustomException(ExceptionCode.PAST_TIME_SLOT_WAITING);
        }
    }

    public List<WaitingResponse> findAllWaitings() {
        List<Waiting> waitings = waitingRepository.findAll();
        return waitings.stream()
                .map(WaitingResponse::from)
                .toList();
    }

    public void deleteWaiting(final Long id) {
        waitingRepository.deleteById(id);
    }

    public List<WaitingWithRank> findAllWithRankByMember(final Member member) {
        List<Waiting> waitings = waitingRepository.findAllByMember(member);
        return waitings.stream()
                .map(waiting -> {
                    Long rank = waitingRepository.countAllByDateAndTimeAndThemeAndIdLessThanEqual(
                            waiting.getDate(),
                            waiting.getTime(),
                            waiting.getTheme(),
                            waiting.getId());
                    return new WaitingWithRank(waiting, rank);
                })
                .toList();
    }

    public void convertFirstWaitingToReservation(final Reservation reservation) {
        if (waitingRepository.existsByDateAndTimeAndTheme(reservation.getDate(), reservation.getTime(), reservation.getTheme())) {
            Waiting waiting = waitingRepository.findFirstByDateAndTimeAndTheme(
                            reservation.getDate(),
                            reservation.getTime(),
                            reservation.getTheme())
                    .orElseThrow(() -> new CustomException(ExceptionCode.NOT_FOUND_RESERVATION));

            waitingRepository.delete(waiting);
            reservationRepository.save(new Reservation(waiting.getMember(), waiting.getDate(), waiting.getTime(), waiting.getTheme()));
        }
    }
}
