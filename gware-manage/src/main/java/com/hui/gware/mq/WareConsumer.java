package com.hui.gware.mq;

import com.alibaba.fastjson.JSON;

import com.hui.gware.bean.OmsOrder;

import com.hui.gware.bean.OmsOrderItem;
import com.hui.gware.bean.WareOrderTaskDetail;
import com.hui.gware.util.ActiveMQUtil;
import com.hui.gware.bean.WareOrderTask;
import com.hui.gware.enums.TaskStatus;
import com.hui.gware.mapper.WareOrderTaskDetailMapper;
import com.hui.gware.mapper.WareOrderTaskMapper;
import com.hui.gware.mapper.WareSkuMapper;
import com.hui.gware.service.GwareService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.util.*;

/**
 * @param
 * @return
 */
@Component
public class WareConsumer {

    @Autowired
    WareOrderTaskMapper wareOrderTaskMapper;

    @Autowired
    WareOrderTaskDetailMapper wareOrderTaskDetailMapper;

    @Autowired
    WareSkuMapper wareSkuMapper;

    @Autowired
    ActiveMQUtil activeMQUtil;

    @Autowired
    JmsTemplate jmsTemplate;

    @Autowired
    GwareService gwareService;

    @JmsListener(destination = "ORDER_PAY_QUEUE", containerFactory = "jmsQueueListener")
    public void receiveOrder(TextMessage textMessage) throws JMSException {
        String orderTaskJson = textMessage.getText();

        /***
         * 转化并保存订单对象
         */
        OmsOrder orderInfo = JSON.parseObject(orderTaskJson, OmsOrder.class);

        // 将order订单对象转为订单任务对象
        WareOrderTask wareOrderTask = new WareOrderTask();
        wareOrderTask.setConsignee(orderInfo.getReceiverName());
        wareOrderTask.setConsigneeTel(orderInfo.getReceiverPhone());
        wareOrderTask.setCreateTime(new Date());
        wareOrderTask.setDeliveryAddress(orderInfo.getReceiverDetailAddress());
        wareOrderTask.setOrderId(orderInfo.getId());
        ArrayList<WareOrderTaskDetail> wareOrderTaskDetails = new ArrayList<>();

        // 打开订单的商品集合
        List<OmsOrderItem> orderDetailList = orderInfo.getOmsOrderItems();
        for (OmsOrderItem orderDetail : orderDetailList) {
            WareOrderTaskDetail wareOrderTaskDetail = new WareOrderTaskDetail();

            wareOrderTaskDetail.setSkuId(orderDetail.getProductSkuId());
            wareOrderTaskDetail.setSkuName(orderDetail.getProductName());
            wareOrderTaskDetail.setSkuNum(orderDetail.getProductQuantity());
            wareOrderTaskDetails.add(wareOrderTaskDetail);

        }
        wareOrderTask.setDetails(wareOrderTaskDetails);
        wareOrderTask.setTaskStatus(TaskStatus.PAID);
        gwareService.saveWareOrderTask(wareOrderTask);

        textMessage.acknowledge();

        // 检查该交易的商品是否有拆单需求
        List<WareOrderTask> wareSubOrderTaskList = gwareService.checkOrderSplit(wareOrderTask);// 检查拆单

        // 库存削减
        if (wareSubOrderTaskList != null && wareSubOrderTaskList.size() >= 2) {
            for (WareOrderTask orderTask : wareSubOrderTaskList) {
                gwareService.lockStock(orderTask);
            }
        } else {
            gwareService.lockStock(wareOrderTask);
        }


    }

}
