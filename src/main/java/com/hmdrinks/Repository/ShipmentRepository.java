package com.hmdrinks.Repository;
import com.hmdrinks.Entity.Payment;
import com.hmdrinks.Entity.Shippment;
import com.hmdrinks.Enum.Payment_Method;
import com.hmdrinks.Enum.Status_Payment;
import com.hmdrinks.Enum.Status_Shipment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ShipmentRepository extends JpaRepository<Shippment, Integer> {
   Shippment findByShipmentIdAndIsDeletedFalse(int shipmentId);

   Shippment findByUserUserIdAndShipmentId(int userId, int shipmentId);
   Page<Shippment> findAll(Pageable pageable);
   Page<Shippment> findAllByStatus(Status_Shipment statusShipment, Pageable pageable);
   Page<Shippment> findAllByUserUserIdAndStatus(int userId, Status_Shipment statusShipment,Pageable pageable);
}
