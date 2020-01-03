package com.hui.gmall.user.service;

import com.hui.gmall.user.bean.UmsMember;
import com.hui.gmall.user.bean.UmsMemberReceiveAddress;

import java.util.List;

public interface UserService {
    List<UmsMember> getAllUser();

    List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId);
}
