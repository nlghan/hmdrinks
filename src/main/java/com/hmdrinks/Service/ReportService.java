package com.hmdrinks.Service;

import com.hmdrinks.Repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final PaymentRepository paymentRepository;

    public ResponseEntity<Double> reportRevenueByDay(LocalDate date) {
        Double revenue = paymentRepository.findTotalRevenueByDate(date.atStartOfDay(), date.atTime(23, 59, 59));
        return ResponseEntity.ok(revenue != null ? revenue : 0.0);
    }

    public ResponseEntity<Map<String, Object>> reportRevenueByMonth(int year, Month month) {
        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(year, month, start.toLocalDate().lengthOfMonth(), 23, 59, 59);

        Map<LocalDate, Double> dailyRevenueMap = getDailyRevenueMap(start, end);
        double totalRevenue = dailyRevenueMap.values().stream().mapToDouble(Double::doubleValue).sum(); // Tính tổng doanh thu

        Map<String, Object> response = new HashMap<>();
        response.put("dailyRevenue", dailyRevenueMap);
        response.put("totalRevenue", totalRevenue);

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Map<String, Object>> reportRevenueByQuarter(int year, int quarter) {
        int startMonth = (quarter - 1) * 3 + 1;
        Map<Month, Double> monthlyRevenueMap = new HashMap<>();

        for (int month = startMonth; month < startMonth + 3; month++) {
            Month currentMonth = Month.of(month);
            double revenue = (Double) reportRevenueByMonth(year, currentMonth).getBody().get("totalRevenue");
            monthlyRevenueMap.put(currentMonth, revenue);
        }

        double totalQuarterRevenue = monthlyRevenueMap.values().stream().mapToDouble(Double::doubleValue).sum();


        Map<String, Object> response = new HashMap<>();
        response.put("monthlyRevenue", monthlyRevenueMap);
        response.put("totalQuarterRevenue", totalQuarterRevenue);

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Map<String, Object>> reportRevenueByYear(int year) {
        Map<Month, Double> monthlyRevenueMap = new HashMap<>();
        double totalYearRevenue = 0.0;

        for (Month month : Month.values()) {
            double revenue = (Double) reportRevenueByMonth(year, month).getBody().get("totalRevenue");
            monthlyRevenueMap.put(month, revenue);
            totalYearRevenue += revenue;
        }

        // Tạo bản đồ trả về
        Map<String, Object> response = new HashMap<>();
        response.put("monthlyRevenue", monthlyRevenueMap);
        response.put("totalYearRevenue", totalYearRevenue);

        return ResponseEntity.ok(response);
    }

    private Map<LocalDate, Double> getDailyRevenueMap(LocalDateTime start, LocalDateTime end) {
        List<Object[]> dailyRevenueData = paymentRepository.findDailyRevenueByDate(start, end);
        Map<LocalDate, Double> dailyRevenueMap = new HashMap<>();

        for (Object[] result : dailyRevenueData) {
            LocalDate date = ((java.sql.Date) result[0]).toLocalDate();
            Double revenue = result.length > 1 ? (Double) result[1] : 0.0;
            dailyRevenueMap.put(date, revenue != null ? revenue : 0.0);
        }
        return dailyRevenueMap;
    }

}
