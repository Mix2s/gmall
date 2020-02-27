package com.hui.gmall.user.service.impl;


import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.hui.gmall.bean.UmsMember;
import com.hui.gmall.bean.UmsMemberReceiveAddress;
import com.hui.gmall.service.UserService;
import com.hui.gmall.user.mapper.UmsMemberReceiveAddressMapper;
import com.hui.gmall.user.mapper.UserMapper;
import com.hui.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;


import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    UmsMemberReceiveAddressMapper umsMemberReceiveAddressMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public List<UmsMember> getAllUser() {

        List<UmsMember> umsMemberList = userMapper.selectAll();//userMapper.selectAllUser();

        return umsMemberList;
    }

    @Override
    public List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId) {

        // 封装的参数对象
        UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();
        umsMemberReceiveAddress.setMemberId(memberId);
        List<UmsMemberReceiveAddress> umsMemberReceiveAddresses = umsMemberReceiveAddressMapper.select(umsMemberReceiveAddress);


//        Example example = new Example(UmsMemberReceiveAddress.class);
//        example.createCriteria().andEqualTo("memberId",memberId);
//        List<UmsMemberReceiveAddress> umsMemberReceiveAddresses = umsMemberReceiveAddressMapper.selectByExample(example);

        return umsMemberReceiveAddresses;
    }

    @Override
    public UmsMember login(UmsMember umsMember) {
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            if (jedis != null) {
                //做缓存存放umsMember
                String umsMemberStr = jedis.get("user:" + umsMember.getPassword() + ":password");  //缺点保证 一个password对应一个UsermemeberId
                if (StringUtils.isNotBlank(umsMemberStr)) {
                    //密码正确
                    UmsMember umsMemberFromCache = JSON.parseObject(umsMemberStr, UmsMember.class);
                    return umsMemberFromCache;
                }
                /*else {
                    //密码错误
                    //缓存中吗没有
                    //开数据库
                    UmsMember umsMemberFromDb = loginFromDb(umsMember);
                    if(umsMemberFromDb!=null){
                        jedis.setex("user:"+umsMember.getPassword()+":info",60*60*24,JSON.toJSONString(umsMemberFromDb));
                    }
                    return umsMemberFromDb;  //有则返回用户 无则返回null
                }*/
            }
            //密码错误
            //缓存中吗没有
            //开数据库
            //连接redis失败 开启数据库
            //开启数据库
            UmsMember umsMemberFromDb = loginFromDb(umsMember);
            if (umsMemberFromDb != null) {
                jedis.setex("user:" + umsMember.getPassword() + ":info", 60 * 60 * 24, JSON.toJSONString(umsMemberFromDb));
            }
            return umsMemberFromDb;  //有则返回用户 无则返回null
        }finally {
            jedis.close();
        }
    }

    @Override
    public void addUserToken(String token, String memberId) {
        Jedis jedis = redisUtil.getJedis();

        jedis.setex("user:"+memberId+":token",60*60*2,token);

        jedis.close();
    }

    private UmsMember loginFromDb(UmsMember umsMember) {

        List<UmsMember> umsMembers = userMapper.select(umsMember);
        if(umsMembers!=null){
            return umsMembers.get(0);
        }
        return null;
    }

    @Override
    public void addOauthUser(UmsMember umsMember) {
        userMapper.insertSelective(umsMember);
    }

    @Override
    public UmsMember checkOauthUser(UmsMember umsCheck) {
        UmsMember umsMember = userMapper.selectOne(umsCheck);
        return umsMember;
    }

}
