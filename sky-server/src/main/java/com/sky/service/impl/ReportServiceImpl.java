package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkSpaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WorkSpaceService workSpaceService;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 营业额统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO turnOverStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        List<Double> priceCountList = new ArrayList<>();
        // 获得日期
        while(begin.isBefore(end) || begin.isEqual(end)){
            // 先查营业额
            LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(begin, LocalTime.MAX);
            Map map = new HashMap();
            map.put("beginTime",beginTime);
            map.put("endTime",endTime);
            map.put("status",Orders.COMPLETED);
            Double priceCount = orderMapper.selectAmountByMap(map);
            priceCount = priceCount == null ? 0.0 : priceCount;
            dateList.add(begin);
            begin = begin.plusDays(1);
            priceCountList.add(priceCount);
        }
        return TurnoverReportVO
                .builder()
                .dateList(StringUtils.join(dateList,","))
                .turnoverList(StringUtils.join(priceCountList,","))
                .build();
    }

    /**
     * 统计用户
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO getUserReport(LocalDate begin, LocalDate end) {
        // 创建集合保存数据
        List<LocalDate> dateList = new ArrayList<>();
        List<Integer> totalList = new ArrayList<>();
        List<Integer> newList = new ArrayList<>();
        // 查询数据
        while (begin.isBefore(end) || begin.isEqual(end)){
            // 转换时间
            LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(begin, LocalTime.MAX);
            LocalDate nextDate = begin.plusDays(1);
            // 查询用户总量
            Integer userTotal = userMapper.selectByDate(endTime);
            totalList.add(userTotal);
            // 查询当日用户增加量
            Integer newUser = userMapper.selectNewByDate(beginTime,endTime);
            newList.add(newUser);
            // 放入日期
            dateList.add(begin);
            begin = begin.plusDays(1);
        }
        return UserReportVO
                .builder()
                .dateList(StringUtils.join(dateList,","))
                .newUserList(StringUtils.join(newList,","))
                .totalUserList(StringUtils.join(totalList,","))
                .build();
    }

    /**
     * 统计订单数据
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO getOrderReport(LocalDate begin, LocalDate end) {
        // 创建集合，保存数据
        List<LocalDate> dateList = new ArrayList<>();
        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();
        Integer totalOrderCount = 0;
        Integer validOrderCount = 0;
        Double orderCompletionRate = 0.0;
        // 开始查询
        while (begin.isBefore(end) || begin.isEqual(end)){
            LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(begin, LocalTime.MAX);
            // 查询数据
            Map map = new HashMap();
            map.put("beginTime",beginTime);
            map.put("endTime",endTime);
            // 每日订单数
            Integer orderCount = orderMapper.selectCountByMap(map);
            map.put("status",Orders.COMPLETED);
            // 有效订单数
            Integer validOrder = orderMapper.selectCountByMap(map);
            orderCountList.add(orderCount);
            validOrderCountList.add(validOrder);
            dateList.add(begin);
            begin = begin.plusDays(1);
        }
        for (Integer integer : orderCountList) {
            totalOrderCount += integer;
        }
        for (Integer integer : validOrderCountList) {
            validOrderCount += integer;
        }
        orderCompletionRate =validOrderCount.doubleValue() / totalOrderCount;
        return OrderReportVO
                .builder()
                .totalOrderCount(totalOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .orderCountList(StringUtils.join(orderCountList,","))
                .validOrderCount(validOrderCount)
                .validOrderCountList(StringUtils.join(validOrderCountList,","))
                .dateList(StringUtils.join(dateList,",")).build();
    }

    /**
     * 销量统计top10
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO getTop10(LocalDate begin, LocalDate end) {
        // 构造localDateTime时间
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        // 查询数据
        List<GoodsSalesDTO> list = orderDetailMapper.selectTop10(beginTime,endTime);
        List<String> nameList = new ArrayList<>();
        List<Integer> saleList = new ArrayList<>();
        for (GoodsSalesDTO dto : list) {
            nameList.add(dto.getName());
            saleList.add(dto.getNumber());
        }
        return SalesTop10ReportVO
                .builder()
                .nameList(StringUtils.join(nameList,","))
                .numberList(StringUtils.join(saleList,","))
                .build();
    }

    /**
     * 获取业务报表
     * @param httpServletResponse
     */
    @Override
    public void report(HttpServletResponse httpServletResponse) {
        // 获取营业数据
        LocalDate begin = LocalDate.now().minusDays(1);
        LocalDate end = begin.minusDays(30);
        LocalDateTime beginTime = LocalDateTime.of(end, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(begin, LocalTime.MAX);
        BusinessDataVO businessData = workSpaceService.getBusinessData(beginTime,endTime);
        // 获得输入流对象
        InputStream template = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        // 创建一个新的excel文件
        try {
            XSSFWorkbook excel = new XSSFWorkbook(template);
            // 获取标签页
            XSSFSheet sheet = excel.getSheetAt(0);
            XSSFRow timeRow = sheet.getRow(1);
            // 获取单元格
            XSSFCell cell = timeRow.getCell(1);
            // 设置时间
            cell.setCellValue("表表日期：" + end + "至" + begin);
            // 设置营业额等订单数据
            sheet.getRow(3).getCell(2).setCellValue(businessData.getTurnover());
            sheet.getRow(3).getCell(4).setCellValue(businessData.getOrderCompletionRate());
            sheet.getRow(3).getCell(6).setCellValue(businessData.getNewUsers());
            sheet.getRow(4).getCell(2).setCellValue(businessData.getValidOrderCount());
            sheet.getRow(4).getCell(4).setCellValue(businessData.getUnitPrice());

            //获取详细订单数据
            for (int i = 0; i < 30; i++) {
                LocalDate date = end.plusDays(i);
                BusinessDataVO business = workSpaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));
                // 插入数据
                XSSFRow row = sheet.getRow(i + 7);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(business.getTurnover());
                row.getCell(3).setCellValue(business.getValidOrderCount());
                row.getCell(4).setCellValue(business.getOrderCompletionRate());
                row.getCell(5).setCellValue(business.getUnitPrice());
                row.getCell(6).setCellValue(business.getNewUsers());
            }

            // 返回excel数据给到浏览器
            ServletOutputStream outputStream = httpServletResponse.getOutputStream();
            excel.write(outputStream);
            // 释放内存
            outputStream.close();
            template.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
