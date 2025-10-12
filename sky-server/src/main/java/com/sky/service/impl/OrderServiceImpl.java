package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.webSocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WeChatPayUtil weChatPayUtil;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private WebSocketServer webSocketServer;

    /**
     * 订单提交
     * @param ordersSubmitDTO
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        // 我们需要先处理各种业务问题
        // 检验地址是否为空
        Long addressBookId = ordersSubmitDTO.getAddressBookId();
        AddressBook addressBook = addressBookMapper.getById(addressBookId);
        if (addressBook == null){
            throw new OrderBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        // 检验购物车数据是否为空
        Long userId = BaseContext.getCurrentId();
        List<ShoppingCart> list = shoppingCartMapper.list(userId);
        if (list == null || list.size() == 0){
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        // 当用户提交过来数据时
        // 我们需要先向订单表之中插入一条数据
        Orders orders = new Orders();
        // 属性拷贝
        BeanUtils.copyProperties(ordersSubmitDTO,orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setUserId(userId);
        orders.setConsignee(addressBook.getConsignee());
        orders.setAddress(addressBook.getCityName() + addressBook.getDistrictName() + addressBook.getDetail());
        orders.setPhone(addressBook.getPhone());
        // 插入
        orderMapper.insert(orders);
        // 然后将这个订单中的所有菜品以及套餐插入到订单明细表中
        Long orderId = orders.getId();
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (ShoppingCart cart : list) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart,orderDetail);
            orderDetail.setOrderId(orderId);
            // 插入列表
            orderDetails.add(orderDetail);
        }
        // 插入数据库
        orderDetailMapper.insert(orderDetails);
        // 最后我们就需要把这个用户购物车中的所有数据全部删除（清空购物车）
        shoppingCartMapper.clean(userId);
        // 封装VO对象传回结果
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orderId)
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .build();
        return orderSubmitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );

        JSONObject jsonObject = new JSONObject();

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);

        //通过websocket向客户端浏览器推送消息 type orderId content
        Map map = new HashMap();
        map.put("type", 1); // 1表示来单提醒 2表示客户催单
        map.put("orderId", ordersDB.getId());
        map.put("content", "订单号：" + outTradeNo);

        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);
    }

    /**
     * 查询具体订单
     * @param id
     * @return
     */
    @Override
    public OrderVO selectByOrderId(Long id) {
        // 先查询订单信息
        OrderVO orderVO = orderMapper.selectByOrderId(id);
        // 在查询订单菜品信息
        List<OrderDetail> list = orderDetailMapper.selectByOrderId(id);
        orderVO.setOrderDetailList(list);
        String detailToStr = getDetailToStr(list);
        orderVO.setOrderDishes(detailToStr);
        return orderVO;
    }

    /**
     * 取消订单
     * @param ordersCancelDTO
     */
    @Override
    public void cancel(OrdersCancelDTO ordersCancelDTO) {
        // 先查询订单信息
        Long id = ordersCancelDTO.getId();
        Orders orders = orderMapper.selectByOrderId(id);
        // 如果订单不存在直接抛出异常
        if (orders == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        // 根据订单状态做出不同的改变
        // 如果订单未付款，那么直接取消
        if (orders.getStatus() > 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders order = new Orders();
        order.setId(orders.getId());

        // 订单处于待接单状态下取消，需要进行退款
        if (orders.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            //调用微信支付退款接口
//            try {
//                weChatPayUtil.refund(
//                        orders.getNumber(), //商户订单号
//                        orders.getNumber(), //商户退款单号
//                        new BigDecimal(0.01),//退款金额，单位 元
//                        new BigDecimal(0.01));//原订单金额
//            } catch (Exception e) {
//                e.printStackTrace();
//            }

            //支付状态修改为 退款
            order.setPayStatus(Orders.REFUND);
        }

        // 更新订单状态、取消原因、取消时间
        order.setStatus(Orders.CANCELLED);
        if (ordersCancelDTO.getCancelReason() != null){
            order.setCancelReason(ordersCancelDTO.getCancelReason());
        }else {
            order.setCancelReason("用户取消");
        }
        order.setCancelTime(LocalDateTime.now());
        orderMapper.update(order);

    }

    /**
     * 再来一单
     * @param id
     */
    @Override
    public void moreOrder(Long id) {
        // 再来一单是插入购物车的操作
        // 获取到菜品
        List<OrderDetail> list = orderDetailMapper.selectByOrderId(id);
        List<ShoppingCart> shoppingCarts = new ArrayList<>();
        // 然后把菜品转换为购物车对象
        for (OrderDetail detail : list) {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(detail,shoppingCart);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCart.setUserId(BaseContext.getCurrentId());
            shoppingCarts.add(shoppingCart);
        }
        // 插入购物车
        shoppingCartMapper.insertBatch(shoppingCarts);
    }

    /**
     * 管理端订单查询
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        //  使用pageHelper动态分页
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        // 查询订单数据
        Page<OrderVO> page =  orderMapper.list(ordersPageQueryDTO);
        for (OrderVO orderVO : page) {
            // 查询到菜品数据
            List<OrderDetail> list = orderDetailMapper.selectByOrderId(orderVO.getId());
            String detail = getDetailToStr(list);
            orderVO.setOrderDishes(detail);
            orderVO.setOrderDetailList(list);
        }
        // 拿到结果
        long total = page.getTotal();
        List<OrderVO> result = page.getResult();

        // 创建PageResult对象
        PageResult pageResult = new PageResult();
        pageResult.setTotal(total);
        pageResult.setRecords(result);
        return pageResult;
    }

    /**
     * 管理端查询订单数量
     * @return
     */
    @Override
    public OrderStatisticsVO countStatus() {
        Integer toBeConfirmed = orderMapper.countByStatus(Orders.TO_BE_CONFIRMED);
        Integer confirmed = orderMapper.countByStatus(Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.countByStatus(Orders.DELIVERY_IN_PROGRESS);

        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
        return orderStatisticsVO;
    }

    /**
     * 接单
     * @param ordersConfirmDTO
     */
    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        OrderVO orderVO = orderMapper.selectByOrderId(ordersConfirmDTO.getId());
        // 只有待接单状态才能接单
        if (orderVO == null || orderVO.getStatus() != 2){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        ordersConfirmDTO.setStatus(3);
        orderMapper.updateStatus(ordersConfirmDTO);
    }

    /**
     * 派送订单
     * @param id
     */
    @Override
    public void delivery(Long id) {
        OrdersConfirmDTO ordersConfirmDTO = new OrdersConfirmDTO();
        ordersConfirmDTO.setStatus(4);
        ordersConfirmDTO.setId(id);
        OrderVO orderVO = orderMapper.selectByOrderId(ordersConfirmDTO.getId());
        // 只有待接单状态才能接单
        if (orderVO == null || orderVO.getStatus() != 3){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        orderMapper.updateStatus(ordersConfirmDTO);
    }

    /**
     * 完成订单
     * @param id
     */
    @Override
    public void complete(Long id) {
        OrderVO orderVO = orderMapper.selectByOrderId(id);
        // 只有待接单状态才能接单
        if (orderVO == null || orderVO.getStatus() != 4){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        orderVO.setStatus(5);
        orderVO.setDeliveryTime(LocalDateTime.now());
        orderMapper.update(orderVO);
    }

    /**
     * 拒单
     * @param ordersRejectionDTO
     */
    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        OrderVO orderVO = orderMapper.selectByOrderId(ordersRejectionDTO.getId());
        // 只有待接单状态才能接单
        if (orderVO == null || orderVO.getStatus() != 2){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = Orders.builder()
                .id(ordersRejectionDTO.getId())
                .cancelReason(ordersRejectionDTO.getRejectionReason())
                .rejectionReason(ordersRejectionDTO.getRejectionReason())
                .cancelTime(LocalDateTime.now())
                .status(Orders.CANCELLED)
                .build();
        orderMapper.update(orders);
    }

    /**
     * 用户催单
     * @param id
     */
    @Override
    public void reminder(Long id) {
        OrderVO orderVO = orderMapper.selectByOrderId(id);
        if (orderVO == null){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //通过websocket向客户端浏览器推送消息 type orderId content
        Map map = new HashMap();
        map.put("type", 2); // 1表示来单提醒 2表示客户催单
        map.put("orderId", orderVO.getId());
        map.put("content", "订单号：" + orderVO);

        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);
    }

    /**
     * 把订单菜品信息封装成一个字符串（鱼香肉丝*4）
     * @param list
     * @return
     */
    public String getDetailToStr(List<OrderDetail> list){
        List<String> orderDetailList = list.stream().map(x -> {
            String orderDish = x.getName() + "*" + x.getNumber() + ";";
            return orderDish;
        }).collect(Collectors.toList());

        return String.join("",orderDetailList);
    }
}
