package student.yuhan.gtalent_spring_boot_260801.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import student.yuhan.gtalent_spring_boot_260801.entity.PaymentNotification;

public interface PaymentNotificationRepository extends JpaRepository<PaymentNotification, Long> {
    
}