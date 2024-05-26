package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.WaitingWithRank;
import roomescape.entity.Member;
import roomescape.entity.Reservation;
import roomescape.entity.ReservationTime;
import roomescape.entity.Theme;
import roomescape.entity.Waiting;
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
        ReservationTime time = reservationTimeRepository.findById(reservationRequest.timeId())
                .orElseThrow(() -> new CustomException(ExceptionCode.NOT_FOUND_RESERVATION_TIME));
        Theme theme = themeRepository.findById(reservationRequest.themeId())
                .orElseThrow(() -> new CustomException(ExceptionCode.NOT_FOUND_THEME));

        Reservation reservation = reservationRepository.findByDateAndTimeAndTheme(reservationRequest.date(), time,
                        theme)
                .orElseThrow(() -> new CustomException(ExceptionCode.NOT_FOUND_RESERVATION));
        Waiting waiting = new Waiting(member, reservation);

        if (waitingRepository.existsByReservationAndMember(reservation, member)) {
            throw new CustomException(ExceptionCode.DUPLICATE_WAITING);
        }
        if (reservationRepository.existsByMemberAndTimeAndDate(member, time, reservationRequest.date())) {
            throw new CustomException(ExceptionCode.DUPLICATE_WAITING);
        }
        validateIsPastTime(reservationRequest.date(), time);

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
                    Long rank = waitingRepository.countAllByReservationAndIdLessThanEqual(waiting.getReservation(),
                            waiting.getId());
                    return new WaitingWithRank(waiting, rank);
                })
                .toList();
    }
}
