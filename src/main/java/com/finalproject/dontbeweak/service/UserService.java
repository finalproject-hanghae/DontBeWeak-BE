package com.finalproject.dontbeweak.service;

import com.finalproject.dontbeweak.auth.jwt.JwtTokenProvider;
import com.finalproject.dontbeweak.jwtwithredis.*;
import com.finalproject.dontbeweak.dto.LoginIdCheckDto;
import com.finalproject.dontbeweak.dto.SignupRequestDto;
import com.finalproject.dontbeweak.exception.CustomException;
import com.finalproject.dontbeweak.exception.ErrorCode;
import com.finalproject.dontbeweak.model.Cat;
import com.finalproject.dontbeweak.model.User;
import com.finalproject.dontbeweak.repository.CatRepository;
import com.finalproject.dontbeweak.repository.UserRepository;
import com.finalproject.dontbeweak.auth.UserDetailsImpl;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final CatRepository catRepository;
    private final CatService catService;
    private final RedisTemplate redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;
    private final Response response;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final HttpServletResponse httpServletResponse;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_TYPE = "Bearer";


    //?????? ????????????
    public String registerUser(SignupRequestDto requestDto){
        String error = "";
        String username = requestDto.getUsername();
        String password = requestDto.getPassword();
        String passwordCheck = requestDto.getPasswordCheck();
        String nickname = requestDto.getNickname();
//        String pattern = "^[a-zA-Z0-9]*$";
        String pattern = "^[a-zA-Z]{1}[a-zA-Z0-9_]{4,11}$";

        //?????? username ?????? ??????
        Optional<User> found = userRepository.findByUsername(username);
        if(found.isPresent()){
            throw new CustomException(ErrorCode.USERNAME_DUPLICATION_CODE);
        }

        //???????????? ??????
        if(!Pattern.matches(pattern, username)){
            throw new CustomException(ErrorCode.USERNAME_FORM_CODE);
        }
        if (!password.equals(passwordCheck)){
            throw new CustomException(ErrorCode.PASSWORD_CHECK_CODE);
        } else if (password.length() < 4) {
            throw new CustomException(ErrorCode.PASSWORD_LENGTH_CODE);
        }

        //???????????? ?????????
        password = passwordEncoder.encode(password);
        requestDto.setPassword(password);

        //?????? ?????? ??????
        User user = User.builder()
                .username(username)
                .nickname(nickname)
                .password(password)
                .role("ROLE_USER")
                .build();

        userRepository.save(user);

        // ???????????? ??? ???????????? ??? ????????? ?????? ??????
        catService.createNewCat(user);

        return error;
    }

    //????????? ?????? ?????? ??????
    public LoginIdCheckDto userInfo(UserDetailsImpl userDetails) {

        String username = userDetails.getUser().getUsername();
        String nickname = userDetails.getUser().getNickname();
        int point = userDetails.getUser().getPoint();

        Optional<Cat> catTemp = catRepository.findByUser_Username(username);
        int level = catTemp.get().getLevel();
        int exp = catTemp.get().getExp();

        LoginIdCheckDto userInfo = new LoginIdCheckDto(username, nickname, point, level, exp);
        return userInfo;
    }


    // ????????????
    public ResponseEntity<?> logout(HttpServletRequest httpServletRequest) {

        // 1. Request Header?????? ?????? ?????? ??????
        String accessToken = resolveToken(httpServletRequest);

        // 2. Access Token ??????
        if (!jwtTokenProvider.validateToken(accessToken)) {
            return response.fail("????????? ???????????????.", HttpStatus.BAD_REQUEST);
        }

        // 3. Access Token ???????????? ????????? username?????? authentication ?????? ?????????
        Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);

        // 3. Redis ?????? ?????? Username?????? ????????? Refresh Token ??? ????????? ????????? ?????? ???, ?????? ?????? ??????.
        if (redisTemplate.opsForValue().get("RT:" + authentication.getName()) != null) {
            // Refresh Token ??????
            redisTemplate.delete("RT:" + authentication.getName());
            System.out.println("=== ???????????? ?????? ?????? ?????? ===");
        }

        // 4. ?????? Access Token ???????????? ????????? ?????? BlackList ??? ????????????
        Long expiration = jwtTokenProvider.getExpiration(accessToken);
        redisTemplate.opsForValue()
                .set(accessToken, "logout", expiration, TimeUnit.MILLISECONDS);

        return response.success("???????????? ???????????????.");
    }


    // Request Header?????? ?????? ?????? ??????
    private String resolveToken(HttpServletRequest httpServletRequest) {

        String bearerToken = httpServletRequest.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_TYPE)) {
            return bearerToken.substring(7);
        }
        return null;
    }

//    public ResponseEntity<?> authority() {
//        // SecurityContext??? ?????? ?????? authentication userEamil ??????
//        String user = ;
//
//        User user = userRepository.findByUsername(username)
//                .orElseThrow(() -> new UsernameNotFoundException("No authentication information."));
//
//        // add ROLE_ADMIN
//        user.getRole().add(Authority.ROLE_ADMIN.name());
//        userRepository.save(user);
//
//        return response.success();
//    }
}
