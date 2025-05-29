package roomescape.reservation.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.error.exception.BadRequestException;
import roomescape.global.error.exception.ConflictException;
import roomescape.global.error.exception.NotFoundException;
import roomescape.member.entity.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.dto.request.ReservationAdminCreateRequest;
import roomescape.reservation.dto.request.ReservationCreateRequest;
import roomescape.reservation.dto.request.ReservationFindFilteredRequest;
import roomescape.reservation.dto.response.ReservationAndWaitingResponse;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.entity.Reservation;
import roomescape.reservation.entity.ReservationTime;
import roomescape.reservation.entity.Waiting;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.repository.WaitingRepository;
import roomescape.theme.entity.Theme;
import roomescape.theme.repository.ThemeRepository;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final WaitingRepository waitingRepository;

    @Transactional
    public ReservationResponse createReservation(Long memberId, ReservationCreateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 멤버 입니다."));
        ReservationTime time = reservationTimeRepository.findById(request.timeId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 시간 입니다."));
        Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 테마 입니다."));

        Reservation newReservation = new Reservation(request.date(), time, theme, member);
        validateDateTime(newReservation);
        validateDuplicated(newReservation);

        Reservation reservation = reservationRepository.save(newReservation);
        return ReservationResponse.from(reservation);
    }

    @Transactional
    public ReservationResponse createReservationByAdmin(ReservationAdminCreateRequest request) {
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 멤버 입니다."));
        ReservationTime time = reservationTimeRepository.findById(request.timeId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 시간 입니다."));
        Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 테마 입니다."));

        Reservation newReservation = new Reservation(request.date(), time, theme, member);
        validateDateTime(newReservation);
        validateDuplicated(newReservation);

        Reservation reservation = reservationRepository.save(newReservation);
        return ReservationResponse.from(reservation);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getFilteredReservations(ReservationFindFilteredRequest request) {
        List<Reservation> reservations = reservationRepository.findAllFiltered(
                request.themeId(), request.memberId(), request.dateFrom(), request.dateTo()
        );
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationAndWaitingResponse> getReservationsByMember(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 멤버 입니다."));

        List<ReservationAndWaitingResponse> responses = new ArrayList<>();

        reservationRepository.findAllByMember(member).stream()
                .map(ReservationAndWaitingResponse::from)
                .forEach(responses::add);

        List<Waiting> memberWaitings = waitingRepository.findAllByMemberOrderByCreatedAt(member);

        Map<String, List<Waiting>> grouped = memberWaitings.stream()
                .collect(Collectors.groupingBy(w ->
                                w.getDate() + "-" + w.getTime().getId() + "-" + w.getTheme().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        for (List<Waiting> group : grouped.values()) {
            for (int i = 0; i < group.size(); i++) {
                Waiting w = group.get(i);
                if (w.getMember().equals(member)) {
                    responses.add(ReservationAndWaitingResponse.from(w, i));
                }
            }
        }

        responses.sort(Comparator
                .comparing(ReservationAndWaitingResponse::date)
                .thenComparing(ReservationAndWaitingResponse::time));

        return responses;
    }

    @Transactional
    public void deleteReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElse(null);
        if (reservation == null) {
            return;
        }

        reservationRepository.deleteById(id);

        List<Waiting> candidates = waitingRepository.findAllByDateAndTimeIdAndThemeIdOrderByCreatedAt(
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId()
        );
        if (candidates.isEmpty()) {
            return;
        }

        Waiting nextWaiting = candidates.get(0);
        Reservation newReservation = new Reservation(
                nextWaiting.getDate(),
                nextWaiting.getTime(),
                nextWaiting.getTheme(),
                nextWaiting.getMember()
        );
        reservationRepository.save(newReservation);
        waitingRepository.delete(nextWaiting);
    }

    private void validateDateTime(Reservation reservation) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reservationDateTime = reservation.getDateTime();
        if (reservationDateTime.isBefore(now)) {
            throw new BadRequestException("과거 날짜는 예약할 수 없습니다.");
        }
    }

    private void validateDuplicated(Reservation reservation) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeId(
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId()
        )) {
            throw new ConflictException("중복된 예약입니다.");
        }
    }
}
