package com.hui.gmall.service;



import com.hui.gmall.bean.UmsMember;
import com.hui.gmall.bean.UmsMemberReceiveAddress;

import java.util.List;

public interface UserService {
    List<UmsMember> getAllUser();

    List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId);
}
