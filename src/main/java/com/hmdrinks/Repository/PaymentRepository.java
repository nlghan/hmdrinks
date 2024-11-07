package com.hmdrinks.Repository;
import com.hmdrinks.Entity.Payment;
import com.hmdrinks.Enum.Payment_Method;
import com.hmdrinks.Enum.Status_Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;


public interface PaymentRepository extends JpaRepository<Payment, Long> {
     Payment findByPaymentId(int paymentId);
     Payment findByOrderOrderId(int orderId);

     Page<Payment> findAll(Pageable pageable);

     Page<Payment> findAllByStatus(Status_Payment status, Pageable pageable);
     Page<Payment> findAllByPaymentMethod(Payment_Method paymentMethod, Pageable pageable);

     Payment findByOrderOrderIdAndStatus(int orderId, Status_Payment status);

     Payment findByOrderIdPayment(String orderIdPayment);

     @Query("SELECT DATE(p.dateCreated), SUM(p.amount) FROM Payment p WHERE p.dateCreated >= :startDate AND p.dateCreated <= :endDate AND p.status = 'COMPLETED' GROUP BY DATE(p.dateCreated)")
     List<Object[]> findDailyRevenueByDate(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);


     @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.dateCreated >= :startDate AND p.dateCreated <= :endDate AND p.status = 'COMPLETED'")
     Double findTotalRevenueByDate(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

}
