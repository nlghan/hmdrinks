package com.hmdrinks.Repository;
import com.hmdrinks.Entity.Payment;
import com.hmdrinks.Enum.Payment_Method;
import com.hmdrinks.Enum.Status_Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface PaymentRepository extends JpaRepository<Payment, Integer> {
     Payment findByPaymentId(int paymentId);
     Payment findByOrderOrderId(int orderId);

     Page<Payment> findAll(Pageable pageable);

     Page<Payment> findAllByStatus(Status_Payment status, Pageable pageable);
     Page<Payment> findAllByPaymentMethod(Payment_Method paymentMethod, Pageable pageable);

     Payment findByOrderOrderIdAndStatus(int orderId, Status_Payment status);

     Payment findByOrderIdPayment(String orderIdPayment);

}
