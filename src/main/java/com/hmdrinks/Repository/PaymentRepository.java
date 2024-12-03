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
     Payment findByPaymentIdAndIsDeletedFalse(int paymentId);
     Payment findByOrderOrderId(int orderId);
     Payment findByOrderOrderIdAndIsDeletedFalse(int orderId);



     Page<Payment> findAll(Pageable pageable);
     List<Payment> findAllByIsDeletedFalse();
     Page<Payment> findAllByIsDeletedFalse(Pageable pageable);

     Page<Payment> findAllByStatusAndIsDeletedFalse(Status_Payment status, Pageable pageable);
     List<Payment> findAllByStatusAndIsDeletedFalse(Status_Payment status);
     Page<Payment> findAllByPaymentMethod(Payment_Method paymentMethod, Pageable pageable);
     Page<Payment> findAllByPaymentMethodAndIsDeletedFalse(Payment_Method paymentMethod, Pageable pageable);
     List<Payment> findAllByPaymentMethodAndIsDeletedFalse(Payment_Method paymentMethod);

     Payment findByOrderIdPayment(String orderIdPayment);

     @Query("SELECT DATE(p.dateCreated), SUM(p.amount) FROM Payment p WHERE p.dateCreated >= :startDate AND p.dateCreated <= :endDate AND p.status = 'COMPLETED' GROUP BY DATE(p.dateCreated)")
     List<Object[]> findDailyRevenueByDate(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);


     @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.dateCreated >= :startDate AND p.dateCreated <= :endDate AND p.status = 'COMPLETED'")
     Double findTotalRevenueByDate(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

}
