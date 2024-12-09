package com.hmdrinks.Repository;
import com.hmdrinks.Entity.Payment;
import com.hmdrinks.Entity.Shippment;
import com.hmdrinks.Enum.Payment_Method;
import com.hmdrinks.Enum.Status_Payment;
import com.hmdrinks.Enum.Status_Shipment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface ShipmentRepository extends JpaRepository<Shippment, Integer> {
   Shippment findByShipmentIdAndIsDeletedFalse(int shipmentId);
   List<Shippment> findByStatus(Status_Shipment status);
   Shippment findByPaymentPaymentIdAndIsDeletedFalse(int paymentId);
   @Query("SELECT s FROM Shippment s JOIN s.user u WHERE u.fullName LIKE %:keyword%")
   Page<Shippment> findByUserNameContaining(@Param("keyword") String keyword, Pageable pageable);
   Shippment findByPaymentPaymentId(int paymentId);
   Shippment findByUserUserIdAndShipmentId(int userId, int shipmentId);
   Page<Shippment> findAll(Pageable pageable);
   List<Shippment> findAll();
   Page<Shippment> findAllByStatus(Status_Shipment statusShipment, Pageable pageable);
   List<Shippment> findAllByStatus(Status_Shipment statusShipment);
   Page<Shippment> findAllByUserUserIdAndStatus(int userId, Status_Shipment statusShipment,Pageable pageable);
   Page<Shippment> findAllByUserUserId(int userId, Pageable pageable);
   List<Shippment> findAllByUserUserId(int shipmentId);
   List<Shippment> findAllByUserUserIdAndStatus(int userId, Status_Shipment statusShipment);
}
