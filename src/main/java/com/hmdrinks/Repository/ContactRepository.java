package com.hmdrinks.Repository;

import com.hmdrinks.Entity.Contact;
import com.hmdrinks.Entity.Voucher;
import com.hmdrinks.Enum.Status_Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactRepository extends JpaRepository<Contact, Integer> {
    Contact findByContactId(int voucherId);
    Contact findByContactIdAndUserUserId(int contactId, int userId);
    Contact findByContactIdAndUserUserIdAndIsDeletedFalse(int contactId, int userId);
    List<Contact> findByIsDeletedFalse();
    Page<Contact> findAllByStatus(Status_Contact status,Pageable pageable);
    Page<Contact> findAll(Pageable pageable);
}
