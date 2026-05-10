package com.ecommerce.payment.service;

import com.ecommerce.payment.dto.PaymentRequest;
import com.ecommerce.payment.dto.PaymentResponse;
import com.ecommerce.payment.model.Payment;
import com.ecommerce.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    private Payment payment;
    private PaymentRequest paymentRequest;

    @BeforeEach
    void setUp() {
        payment = new Payment();
        payment.setId(1L);
        payment.setTransactionId("TXN-TEST123");
        payment.setOrderNumber("ORD-12345678");
        payment.setAmount(new BigDecimal("99.99"));
        payment.setPaymentMethod(Payment.PaymentMethod.CREDIT_CARD);
        payment.setStatus(Payment.PaymentStatus.COMPLETED);
        payment.setCardLastFourDigits("4242");

        paymentRequest = new PaymentRequest();
        paymentRequest.setOrderNumber("ORD-12345678");
        paymentRequest.setAmount(new BigDecimal("99.99"));
        paymentRequest.setPaymentMethod(Payment.PaymentMethod.CREDIT_CARD);
        paymentRequest.setCardNumber("4242424242424242");
    }

    @Test
    void processPayment_ShouldCreatePayment() {
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        PaymentResponse response = paymentService.processPayment(paymentRequest);

        assertThat(response).isNotNull();
        assertThat(response.getOrderNumber()).isEqualTo("ORD-12345678");
        assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("99.99"));
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void getPaymentById_ShouldReturnPayment() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.getPaymentById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTransactionId()).isEqualTo("TXN-TEST123");
    }

    @Test
    void getPaymentById_ShouldThrowException_WhenNotFound() {
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> paymentService.getPaymentById(999L));
    }

    @Test
    void getPaymentByTransactionId_ShouldReturnPayment() {
        when(paymentRepository.findByTransactionId("TXN-TEST123")).thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.getPaymentByTransactionId("TXN-TEST123");

        assertThat(response).isNotNull();
        assertThat(response.getTransactionId()).isEqualTo("TXN-TEST123");
    }

    @Test
    void getPaymentsByOrderNumber_ShouldReturnPayments() {
        when(paymentRepository.findByOrderNumber("ORD-12345678")).thenReturn(Arrays.asList(payment));

        List<PaymentResponse> responses = paymentService.getPaymentsByOrderNumber("ORD-12345678");

        assertThat(responses).isNotEmpty();
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getOrderNumber()).isEqualTo("ORD-12345678");
    }

    @Test
    void getAllPayments_ShouldReturnAllPayments() {
        when(paymentRepository.findAll()).thenReturn(Arrays.asList(payment));

        List<PaymentResponse> responses = paymentService.getAllPayments();

        assertThat(responses).isNotEmpty();
        assertThat(responses).hasSize(1);
    }

    @Test
    void refundPayment_ShouldRefundCompletedPayment() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        payment.setStatus(Payment.PaymentStatus.COMPLETED);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        PaymentResponse response = paymentService.refundPayment(1L);

        assertThat(response).isNotNull();
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void refundPayment_ShouldThrowException_WhenNotCompleted() {
        payment.setStatus(Payment.PaymentStatus.PENDING);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        assertThrows(RuntimeException.class, () -> paymentService.refundPayment(1L));
    }
}