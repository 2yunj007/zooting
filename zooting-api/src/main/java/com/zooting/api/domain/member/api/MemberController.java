package com.zooting.api.domain.member.api;

import com.zooting.api.domain.block.entity.Block;
import com.zooting.api.domain.member.application.MemberService;
import com.zooting.api.domain.member.dto.request.*;
import com.zooting.api.domain.member.dto.response.MemberRes;
import com.zooting.api.domain.member.dto.response.PointRes;
import com.zooting.api.domain.member.entity.Member;
import com.zooting.api.global.common.BaseResponse;
import com.zooting.api.global.common.code.SuccessCode;
import com.zooting.api.global.security.user.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.List;

@Log4j2
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostAuthorize("hasRole('ANONYMOUS')")
    @GetMapping("/test")
    public String test(@AuthenticationPrincipal CustomOAuth2User user,
            @AuthenticationPrincipal UserDetails userDetails,
            Authentication auth) {
        log.info("CustomOAuth2User 출력: " + user + "\n");
        log.info("UserDetails 출력: " + userDetails + "\n");
        log.info("Auth 출력: " + auth + "\n");
        return "TEST";
    }

    @GetMapping("/nickname/check")
    public ResponseEntity<BaseResponse<Boolean>> checkNicknameDuplicate(
            @RequestParam(name = "nickname") String nickname) {
        var result = memberService.existNickname(nickname);
        return BaseResponse.success(
                SuccessCode.CHECK_SUCCESS,
                result
        );
    }

    @PutMapping("/")
    public ResponseEntity<BaseResponse<String>> saveAdditionalInfo(@RequestBody MemberReq memberReq)
            throws ParseException {
        memberService.updateMemberInfo(memberReq);
        return BaseResponse.success(
                SuccessCode.UPDATE_SUCCESS,
                "추가 정보 저장 성공"
        );
    }

    @PutMapping("/interests")
    public ResponseEntity<BaseResponse<String>> updateInterests(@RequestBody InterestsReq interestsReq) {
        memberService.updateInterestsandIdeal(interestsReq);
        return BaseResponse.success(
                SuccessCode.UPDATE_SUCCESS,
                "관심사, 이상형 수정 완료"
        );
    }

    @PutMapping("/introduce")
    public ResponseEntity<BaseResponse<String>> updateIntroduce(@RequestBody IntroduceReq introduceReq) {
        memberService.updateIntroduce(introduceReq);
        return BaseResponse.success(
                SuccessCode.UPDATE_SUCCESS,
                "자기소개 수정 완료"
        );
    }

    @GetMapping("")
    public ResponseEntity<BaseResponse<List<MemberRes>>> findMemberList(@RequestParam(name = "email") String email,
            @RequestParam(name = "nickname") String nickname) {
        List<MemberRes> memberResList = memberService.findMemberList(email, nickname);
        return BaseResponse.success(
                SuccessCode.CHECK_SUCCESS,
                memberResList
        );
    }

    @PutMapping("/characters")
    public ResponseEntity<BaseResponse<String>> updatePersonality(@RequestBody PersonalityReq personalityReq) {
        memberService.updatePersonality(personalityReq);
        return BaseResponse.success(
                SuccessCode.UPDATE_SUCCESS,
                "성격 수정 완료"
        );
    }

    @PostMapping("/block")
    public ResponseEntity<BaseResponse<String>> saveBlockMember(@RequestBody BlockReq blockReq) {
        memberService.insertBlockList(blockReq);
        return BaseResponse.success(
                SuccessCode.UPDATE_SUCCESS,
                "멤버 차단 완료"
        );
    }

    @DeleteMapping("/block")
    public ResponseEntity<BaseResponse<String>> deleteBlockMember(@RequestBody BlockReq blockReq) {
        memberService.deleteBlock(blockReq);
        return BaseResponse.success(
                SuccessCode.DELETE_SUCCESS,
                "멤버 차단 해제 완료"
        );
    }

    @PostMapping("/reports")
    public ResponseEntity<BaseResponse<String>> insertReport(@RequestBody ReportReq reportReq) {
        memberService.insertReport(reportReq);
        return BaseResponse.success(
                SuccessCode.INSERT_SUCCESS,
                reportReq.email() + "에 대한 신고 완료"
        );
    }

    @GetMapping("/points")
    public ResponseEntity<BaseResponse<PointRes>> findPoints(@RequestParam(name = "nickname") String nickname) {
        PointRes result = memberService.findPoints(nickname);
        return BaseResponse.success(
                SuccessCode.CHECK_SUCCESS,
                result
        );
    }

    @PostMapping("/animal")
    public ResponseEntity<BaseResponse<String>> buyAnimalChangeItem(@RequestBody PointReq pointReq) {
        Long price = 300L; // 동물상 변경권 가격
        boolean isDeducted = memberService.deductPoints(pointReq.email(), price);
        if (isDeducted) {
            return BaseResponse.success(
                    SuccessCode.UPDATE_SUCCESS,
                    "변경 허용"
            );
        } else {
            return BaseResponse.success(
                    SuccessCode.UPDATE_SUCCESS,
                    "변경 불가"
            );
        }

    }

    @PostMapping("/nickname")
    public ResponseEntity<BaseResponse<String>> buyNicknameChangeItem(@RequestBody PointReq pointReq) {
        Long price = 600L; // 닉네임 변경권 가격
        boolean isDeducted = memberService.deductPoints(pointReq.email(), price);
        if (isDeducted) {
            return BaseResponse.success(
                    SuccessCode.UPDATE_SUCCESS,
                    "변경 허용"
            );
        } else {
            return BaseResponse.success(
                    SuccessCode.UPDATE_SUCCESS,
                    "변경 불가"
            );
        }
    }

    @GetMapping("/")
    public ResponseEntity<Member> getMemberByEmail(@RequestParam String email) {
        return ResponseEntity.ok(memberService.getMemberByEmail(email));
    }

    @PostMapping("/register/email")
    public ResponseEntity<Member> initialMemberRegister(@RequestBody String email) {
        return ResponseEntity.ok(memberService.initialMemberRegister(email));
    }
}
