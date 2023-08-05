package com.ssafy.bium.user.controller;

import com.ssafy.bium.user.User;
import com.ssafy.bium.user.repository.UserRepository;
import com.ssafy.bium.user.request.FilePostReq;
import com.ssafy.bium.user.request.UserLoginPostReq;
import com.ssafy.bium.user.request.UserModifyPostReq;
import com.ssafy.bium.user.request.UserRegisterPostReq;
import com.ssafy.bium.user.response.UserModifyGetRes;
import com.ssafy.bium.user.response.UserRankingGetRes;
import com.ssafy.bium.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    public static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final UserRepository userRepository;

    @Value("${file.imgPath}")
    private String uploadImgPath;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserLoginPostReq userLoginPostReq) {
        Map<String, Object> resultMap = new HashMap<>();

        userService.login(userLoginPostReq);
        resultMap.put("httpHeaders", "eyJhbGciOiJIUzI1NiJ9.eyJleHAiOjI5ODcwNzQ2NjJ9.lXRvR1Xv_W_WdAz15uw5VG4G6myl-fuj75tULle6vLs");
        resultMap.put("message", "success");

        return new ResponseEntity<Map<String, Object>>(resultMap, HttpStatus.OK);
    }

    // 로그아웃
    @GetMapping("/logout/{userEmail}")
    public ResponseEntity<?> logout(@PathVariable("userEmail") String userId) {
        Map<String, Object> resultMap = new HashMap<>();
        HttpStatus status = HttpStatus.ACCEPTED;
        try {
            resultMap.put("message", "success");
            status = HttpStatus.ACCEPTED;
        } catch (Exception e) {
            resultMap.put("message", e.getMessage());
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return new ResponseEntity<Map<String, Object>>(resultMap, status);
    }

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody UserRegisterPostReq registerInfo) {

        System.out.println("userController" + registerInfo.getUserEmail());
        User user = userService.setUser(registerInfo);
        System.out.println(user);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    // 아이디 중복 체크
    @GetMapping("/signup/check")
    public ResponseEntity<?> emailCheck(@RequestParam String userEmail) {

        int cnt = 1;
        if (userService.getUserByUserEmail(userEmail) == null) {
            cnt = 0;
        }

        return new ResponseEntity<>(cnt, HttpStatus.OK);
    }

    // 회원탈퇴
    @PostMapping("profile/delete")
    public ResponseEntity<?> deleteUser(@RequestParam String userEmail) {

        int check = userService.deleteUserByUserEmail(userEmail);
        return new ResponseEntity<>(check, HttpStatus.OK);
    }

    // 토큰과 유저 정보 반환
    @GetMapping("/info/{userEmail}")
    public ResponseEntity<Map<String, Object>> getInfo(
            @PathVariable("userEmail") String userEmail) {
        System.out.println("hello");

        Map<String, Object> resultMap = new HashMap<>();
        HttpStatus status = HttpStatus.UNAUTHORIZED;

        try {
//				로그인 사용자 정보.
            User user = userService.getUserByUserEmail(userEmail);
            resultMap.put("userInfo", user);
            resultMap.put("message", "success");
            status = HttpStatus.ACCEPTED;
        } catch (Exception e) {
            resultMap.put("message", e.getMessage());
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return new ResponseEntity<Map<String, Object>>(resultMap, status);

    }

    @GetMapping("profile/modify/{userEmail}")
    public ResponseEntity<?> getModifyData(@PathVariable("userEmail") String userEmail) {

        UserModifyGetRes userModifyGetRes = userService.getModifyData(userEmail);
        return new ResponseEntity<>(userModifyGetRes, HttpStatus.OK);
    }

    @PostMapping("profile/modify")
    public ResponseEntity<?> modifyProfile(UserModifyPostReq userModifyPostReq) {

        int result = userService.modifyProfile(userModifyPostReq);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("profile/img/{userEmail}")
    public ResponseEntity<?> setProfileImg(@PathVariable(value = "userEmail") String userEmail,
                                           @RequestParam(value = "upfile", required = false) MultipartFile file,
                                           @RequestParam(value = "imgType") int imgType) throws Exception {
        logger.debug("MultipartFile.isEmpty : {}", file.isEmpty());

        if (!file.isEmpty()) {
            String saveFolder = uploadImgPath + File.separator + userEmail + File.separator + imgType;
            logger.debug("저장 폴더: {}", saveFolder);
            File folder = new File(saveFolder);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            FilePostReq filePostReq = new FilePostReq();
            String originalFileName = file.getOriginalFilename();
            if (!originalFileName.isEmpty()) {
                String saveFileName = UUID.randomUUID()
                        + originalFileName.substring(originalFileName.lastIndexOf('.'));

                filePostReq.setUserId(userRepository.findByUserEmail(userEmail).get().getId());
                filePostReq.setImageType(imgType);
                filePostReq.setSaveFolder(userEmail);
                filePostReq.setOriginalFile(originalFileName);
                filePostReq.setSaveFile(saveFileName);

                logger.debug("원본 파일 이름 : {}, 실제 저장 파일 이름 : {}", file.getOriginalFilename(), saveFileName);
                file.transferTo(new File(folder, saveFileName));
            }
            userService.setImage(filePostReq);
        }
        return new ResponseEntity<>(HttpStatus.OK);

    }

    @GetMapping("profile/ranking/{userEmail}")
    public ResponseEntity<?> ranking(@PathVariable("userEmail") String userEmail) {

        Map<String, Object> resultMap = new HashMap<>();
        List<UserRankingGetRes> list = userService.getUserListTop5ByTotalBium();

        UserRankingGetRes userRankingGetRes = userService.getUserByTotalBium(userEmail);
        resultMap.put("ranking", list);
        resultMap.put("myRanking", userRankingGetRes);
        return new ResponseEntity<>(resultMap, HttpStatus.OK);

    }

}
