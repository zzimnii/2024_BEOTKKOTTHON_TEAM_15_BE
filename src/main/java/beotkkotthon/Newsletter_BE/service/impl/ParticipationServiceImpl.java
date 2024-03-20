package beotkkotthon.Newsletter_BE.service.impl;

import beotkkotthon.Newsletter_BE.config.security.util.SecurityUtil;
import beotkkotthon.Newsletter_BE.domain.Member;
import beotkkotthon.Newsletter_BE.domain.Team;
import beotkkotthon.Newsletter_BE.domain.enums.RequestRole;
import beotkkotthon.Newsletter_BE.domain.enums.Role;
import beotkkotthon.Newsletter_BE.domain.mapping.MemberTeam;
import beotkkotthon.Newsletter_BE.domain.mapping.Participation;
import beotkkotthon.Newsletter_BE.payload.exception.GeneralException;
import beotkkotthon.Newsletter_BE.payload.status.ErrorStatus;
import beotkkotthon.Newsletter_BE.repository.MemberTeamRepository;
import beotkkotthon.Newsletter_BE.repository.ParticipationRepository;
import beotkkotthon.Newsletter_BE.service.MemberService;
import beotkkotthon.Newsletter_BE.service.MemberTeamService;
import beotkkotthon.Newsletter_BE.service.ParticipationService;
import beotkkotthon.Newsletter_BE.service.TeamService;
import beotkkotthon.Newsletter_BE.web.dto.request.ParticipationRequestDto;
import beotkkotthon.Newsletter_BE.web.dto.response.ParticipationResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParticipationServiceImpl implements ParticipationService {

    private final ParticipationRepository participationRepository;
    private final MemberTeamRepository memberTeamRepository;
    private final TeamService teamService;
    private final MemberService memberService;
    private final MemberTeamService memberTeamService;


    @Override
    public Participation findByMemberAndTeam(Member member, Team team) {
        return participationRepository.findByMemberAndTeam(member, team).orElseThrow(
                () -> new GeneralException(ErrorStatus.PARTICIPATION_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    @Override
    public List<ParticipationResponseDto> findParticipationByTeam(Long teamId) {
        Team team = teamService.findById(teamId);
        List<Participation> participationList = participationRepository.findAllByTeam(team);

        List<ParticipationResponseDto> participationResponseDtoList = new ArrayList<>();
        for(int i=0; i<participationList.size(); i++) {
            Participation participation = participationList.get(i);
            Member member = participation.getMember();
            ParticipationResponseDto participationResponseDto = new ParticipationResponseDto(
                    participation.getId(), participation.getRequestRole(), participation.getCreatedTime(),
                    member.getId(), member.getEmail(), member.getUsername()
            );
            participationResponseDtoList.add(participationResponseDto);
        }

        participationResponseDtoList.sort(Comparator.comparing(ParticipationResponseDto::getRequestCreatedTime));  // 팀참여 신청 날짜최신순 정렬
        return participationResponseDtoList;
    }

    @Transactional
    @Override
    public void acceptParticipation(Long teamId, ParticipationRequestDto participationRequestDto) {  // 새멤버 수락/거절 결과 Participation에서 해당 데이터 삭제 후, 수락일 경우 MemberTeam에도 추가.
        // 참여승인 받고자 하는 사용자
        Team team = teamService.findById(teamId);
        Member member = memberService.findById(participationRequestDto.getMemberId());
        if(memberTeamRepository.existByMemberAndTeam(member, team)) {
            throw new GeneralException(ErrorStatus.BAD_REQUEST, "이미 해당 그룹에 가입되어있는 사용자입니다.");
        }

        // 현재 로그인된 사용자
        Long loginMemberId = SecurityUtil.getCurrentMemberId();
        Member loginMember = memberService.findById(loginMemberId);
        MemberTeam loginMemberTeam = memberTeamService.findByMemberAndTeam(loginMember, team);
        Role loginRole = loginMemberTeam.getRole();  // 로그인 사용자의 Role
        if(loginRole == Role.MEMBER) {
            throw new GeneralException(ErrorStatus.BAD_REQUEST, "참여신청 승인 권한 ERROR - Member는 승인 권한이 없습니다.");
        }

        Participation participation = findByMemberAndTeam(member, team);
        Role newRole = Role.MEMBER;
        if(participation.getRequestRole().name() == "LEADER") newRole = Role.LEADER;

        if(participationRequestDto.getIsAccept() == true) {  // 수락인 경우
            team.teamSizeUp();  // 인원수 체킹 및 teamSize를 +1하기 위해, 이 코드먼저 실행.
            MemberTeam memberTeam = MemberTeam.MemberTeamSaveBuilder()
                    .role(newRole)
                    .member(member)
                    .team(team)
                    .build();
            memberTeamRepository.save(memberTeam);
        }
        participationRepository.delete(participation);  // 수락이든 거절이든간에 Participation에서 제거해주어야함.
    }

    @Transactional
    @Override
    public ParticipationResponseDto createParticipation(Long teamId, String requestRole) {
        // 현재 로그인된 사용자
        Long loginMemberId = SecurityUtil.getCurrentMemberId();
        Member loginMember = memberService.findById(loginMemberId);
        Team team = teamService.findById(teamId);
        RequestRole newRequestRole = RequestRole.MEMBER;
        if(requestRole == "LEADER") newRequestRole = RequestRole.LEADER;

        Participation participation = Participation.ParticipationSaveBuilder()
                .requestRole(newRequestRole)
                .member(loginMember)
                .team(team)
                .build();
        participationRepository.save(participation);

        ParticipationResponseDto participationResponseDto = new ParticipationResponseDto(
                participation.getId(), participation.getRequestRole(), participation.getCreatedTime(),
                loginMember.getId(), loginMember.getEmail(), loginMember.getUsername()
        );

        return participationResponseDto;
    }
}
