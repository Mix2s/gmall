package com.hui.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hui.gmall.bean.PmsSkuAttrValue;
import com.hui.gmall.bean.PmsSkuImage;
import com.hui.gmall.bean.PmsSkuInfo;
import com.hui.gmall.bean.PmsSkuSaleAttrValue;
import com.hui.gmall.manage.mapper.PmsSkuAttrValueMapper;
import com.hui.gmall.manage.mapper.PmsSkuImageMapper;
import com.hui.gmall.manage.mapper.PmsSkuInfoMapper;
import com.hui.gmall.manage.mapper.PmsSkuSaleAttrValueMapper;
import com.hui.gmall.service.SkuService;
import com.hui.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    PmsSkuInfoMapper pmsSkuInfoMapper;

    @Autowired
    PmsSkuAttrValueMapper pmsSkuAttrValueMapper;

    @Autowired
    PmsSkuSaleAttrValueMapper pmsSkuSaleAttrValueMapper;

    @Autowired
    PmsSkuImageMapper pmsSkuImageMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public void saveSkuInfo(PmsSkuInfo pmsSkuInfo) {

        //插入skuInfo
        int insertSelective = pmsSkuInfoMapper.insertSelective(pmsSkuInfo);
        String skuId = pmsSkuInfo.getId();

        //插入平台属性关联
        List<PmsSkuAttrValue> skuAttrValueList = pmsSkuInfo.getSkuAttrValueList();
        for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
            pmsSkuAttrValue.setSkuId(skuId);
            pmsSkuAttrValueMapper.insertSelective(pmsSkuAttrValue);

        }
        //插入销售属性关联
        List<PmsSkuSaleAttrValue> skuSaleAttrValueList = pmsSkuInfo.getSkuSaleAttrValueList();
        for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
            pmsSkuSaleAttrValue.setSkuId(skuId);
            pmsSkuSaleAttrValueMapper.insertSelective(pmsSkuSaleAttrValue);
        }

        //插入图片信息
        List<PmsSkuImage> skuImageList = pmsSkuInfo.getSkuImageList();
        for (PmsSkuImage pmsSkuImage : skuImageList) {
            pmsSkuImage.setSkuId(skuId);
            pmsSkuImageMapper.insertSelective(pmsSkuImage);
        }

    }
    @Override
    public PmsSkuInfo getSkuById(String skuId,String ip) {

        System.out.println("ip:"+ip+"访问 当前线程："+Thread.currentThread().getName()+"查询商品详情");

        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        //连接缓存
        Jedis jedis = redisUtil.getJedis();

        //查询缓存
        String skuKey = "sku:" + skuId + ":info";
        String skuJson = jedis.get(skuKey);

        if (StringUtils.isNotBlank(skuJson)) { //if(skuJson!=null&&skuJson.equals=(""))

            System.out.println("ip:"+ip+"访问 当前线程："+Thread.currentThread().getName()+"从缓存获取商品");

            pmsSkuInfo = JSON.parseObject(skuJson, PmsSkuInfo.class);
        } else {
            System.out.println("ip:"+ip+"访问 当前线程："+Thread.currentThread().getName()+"缓存获取失败 DB查询 申请锁:"+"skuId:"+skuId+":lock");

            //缓存不存在 去DB查询 mysql
            String token = UUID.randomUUID().toString();  //产生一个临时随机值 用来标记当前线程锁
            //设置分布式锁
            String OK = jedis.set("sku:"+skuId+":lock",token,"nx","px",10*1000); //拿到锁 锁过期时间10s

            if(StringUtils.isNotBlank(OK)&&OK.equals("OK")){

                //获取锁成功
                System.out.println("ip:"+ip+"访问 当前线程："+Thread.currentThread().getName()+"缓存获取成功 10s DB查询 :"+"skuId:"+skuId+":lock");

                //设置成功有权利访问数据库 有权利在过期时间内访问数据库
                pmsSkuInfo = getSkuByIdFromDb(skuId);

                try {
                    Thread.sleep(1000*5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (pmsSkuInfo != null) {
                    //DB查询结果存入redis
                    jedis.set("sku:" + skuId + ":info", JSON.toJSONString(pmsSkuInfo));
                }else{
                    //数据库中不存在sku 放置缓存穿透
                    //直接设置 null 或者空字符串赋给redis  并且为其设置过期时间
                    jedis.setex("sku:" + skuId + ":info",60*3, JSON.toJSONString(""));
                }

                //释放锁资源
                System.out.println("ip:"+ip+"访问 当前线程："+Thread.currentThread().getName()+"查询完毕 释放锁资源:"+"skuId:"+skuId+":lock");

                //将获取的锁资源释放
                String lockToken = jedis.get("sku:"+skuId+":lock");
                if(StringUtils.isNotBlank(lockToken)&&lockToken.equals(token)){
                    //jedis.eval("")脚本语言
                    //保证当前删除的 是自己线程的锁
                    /*
                    //在查询到key的同时直接删除key 避免高并发带来的问题
                    String script = "if redis.call('get',KEYS[1])==ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
                    jedis.eval(script, Collections.singletonList("Lock"),Collections.singletonList(token));
                    */
                   jedis.del("sku:"+skuId+":lock");
                }

            }else{
                //获取锁失败
                System.out.println("ip:"+ip+"访问 当前线程："+Thread.currentThread().getName()+"缓存获取失败 自旋 申请锁:"+"skuId:"+skuId+":lock");

                    //设置失败 自旋(当前线程睡眠几秒 后重新访问)
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
               // getSkuById()  等于开辟一个新线程 无效自旋
                return getSkuById(skuId,ip);
            }

        }
        jedis.close();
        return pmsSkuInfo;
    }

    @Override
    public List<PmsSkuInfo> getAllSku() {
        List<PmsSkuInfo> pmsSkuInfos = pmsSkuInfoMapper.selectAll();
        for (PmsSkuInfo pmsSkuInfo : pmsSkuInfos) {
            String skuid = pmsSkuInfo.getId();

            PmsSkuAttrValue pmsSkuAttrValue = new PmsSkuAttrValue();
            pmsSkuAttrValue.setSkuId(skuid);
            List<PmsSkuAttrValue> select = pmsSkuAttrValueMapper.select(pmsSkuAttrValue);

            pmsSkuInfo.setSkuAttrValueList(select);
        }
        return pmsSkuInfos;
    }

    @Override
    public boolean checkPrice(String productSkuId, BigDecimal productPrice) {
        boolean b = false;

        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(productSkuId);
        PmsSkuInfo pmsSkuInfo1 = pmsSkuInfoMapper.selectOne(pmsSkuInfo);

        BigDecimal price = pmsSkuInfo1.getPrice();

        if(price.compareTo(productPrice)==0){
            b = true;
        }
        return b;
    }

    //从数据库查询
    public PmsSkuInfo getSkuByIdFromDb(String skuId) { //getSkuByIdFromDb
        //sku商品对象展示
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(skuId);

        PmsSkuInfo skuInfo = pmsSkuInfoMapper.selectOne(pmsSkuInfo);

        //页面展示图片 sku
        PmsSkuImage pmsSkuImage = new PmsSkuImage();
        pmsSkuImage.setSkuId(skuId);
        List<PmsSkuImage> pmsSkuImages = pmsSkuImageMapper.select(pmsSkuImage);

        skuInfo.setSkuImageList(pmsSkuImages);
        return skuInfo;
    }


    @Override
    public List<PmsSkuInfo> getSkuSaleAttrValueListBySpu(String productId) {
        List<PmsSkuInfo> pmsSkuInfos = pmsSkuInfoMapper.selectSkuSaleAttrValueListBySpu(productId);
        return pmsSkuInfos;
    }

}
