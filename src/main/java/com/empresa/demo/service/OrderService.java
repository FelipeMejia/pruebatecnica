package com.empresa.demo.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.empresa.demo.model.Order;

public class OrderService {
	BigDecimal aplicarTope(BigDecimal discount, BigDecimal total) {
		BigDecimal maxDiscount = total.multiply(new BigDecimal("0.20"));
        return (discount.compareTo(maxDiscount) > 0) ? maxDiscount : discount;
    }
	
	public BigDecimal calcularDescuento(Order order) {
		BigDecimal total = order.getTotal();

		// Caso base => Ningún descuento a aplicar
		BigDecimal discount = BigDecimal.ZERO;

		// Si total > 1000, aplicar 10% de descuento
		if (total.compareTo(new BigDecimal("1000")) > 0) {
			discount = total.multiply(new BigDecimal("0.10"));
		}

		// Si es VIP, aplicar 5% adicional
		if (order.isVip()) {
			BigDecimal subTotal = total.subtract(discount);
			discount = discount.add(subTotal.multiply(new BigDecimal("0.05")));
		}

		// Aplicar tope del 20% del total
		discount = aplicarTope(discount, total);

		return discount.setScale(2, RoundingMode.HALF_UP);
	}
}
