package roomescape.service;

import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.dto.projection.WaitingWithRank;
import roomescape.dto.request.WaitingRequest;
import roomescape.dto.response.ReservationOrderResponse;
import roomescape.exception.DuplicateReservationException;
import roomescape.exception.IdNotFoundException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingRepository;

@Service
@Transactional(readOnly = true)
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final MemberRepository memberRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public WaitingService(WaitingRepository waitingRepository, MemberRepository memberRepository,
                           ReservationTimeRepository reservationTimeRepository, ThemeRepository themeRepository) {
        this.waitingRepository = waitingRepository;
        this.memberRepository = memberRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    public List<ReservationOrderResponse> findByMemberName(String name) {
        Member member = memberRepository.findByName(name)
                .orElseThrow(() -> new IdNotFoundException("요청하신 회원을 찾을 수 없습니다."));

        return waitingRepository.findWithRankByMember_Id(member.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    private ReservationOrderResponse toResponse(WaitingWithRank waitingWithRank) {
        Waiting waiting = waitingWithRank.waiting();
        long order = waitingWithRank.getRank() + 1;
        return ReservationOrderResponse.from(waiting, order);
    }

    @Transactional
    public ReservationOrderResponse save(WaitingRequest request) {
        Member member = memberRepository.findByName(request.name())
                .orElseGet(() -> memberRepository.save(new Member(request.name())));
        ReservationTime time = getValidReservationTime(request.timeId());
        Theme theme = getValidTheme(request.themeId());

        Waiting waiting = new Waiting(member, request.date(), time, theme);

        try {
            Waiting saved = waitingRepository.save(waiting);
            return ReservationOrderResponse.from(saved, calculateOrder(saved));
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateReservationException("이미 동일한 예약 또는 대기 신청이 존재합니다.");
        }
    }

    @Transactional
    public void delete(Long id) {
        waitingRepository.deleteById(id);
    }

    private long calculateOrder(Waiting waiting) {
        return waitingRepository.findWithRankByMember_Id(waiting.getMember().getId()).stream()
                .filter(w -> w.getId().equals(waiting.getId()))
                .map(w -> w.getRank() + 1)
                .findFirst()
                .orElse(1L);
    }

    private ReservationTime getValidReservationTime(Long timeId) {
        try {
            return reservationTimeRepository.findReservationTimeById(timeId);
        } catch (EmptyResultDataAccessException e) {
            throw new IdNotFoundException("요청하신 시간 정보를 찾을 수 없습니다. 선택하신 시간이 정확한지 다시 한번 확인해 주세요.");
        }
    }

    private Theme getValidTheme(Long themeId) {
        try {
            return themeRepository.findThemeById(themeId);
        } catch (EmptyResultDataAccessException e) {
            throw new IdNotFoundException("요청하신 테마를 찾을 수 없습니다. 선택하신 테마가 정확한지 다시 한번 확인해 주세요.");
        }
    }
}
