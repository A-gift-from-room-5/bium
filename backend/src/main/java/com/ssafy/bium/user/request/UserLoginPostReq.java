package com.ssafy.bium.user.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserLoginPostReq {

    String userEmail;
    String userPw;
}
