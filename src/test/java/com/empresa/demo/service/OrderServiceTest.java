package com.empresa.demo.service;

import com.empresa.demo.model.Order;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class OrderServiceTest {

	private final OrderService service = new OrderService();

	static class TestOrder extends Order {
		private final BigDecimal total;
		private final boolean vip;

		TestOrder(BigDecimal total, boolean vip) {
			this.total = total;
			this.vip = vip;
		}

		@Override
		public BigDecimal getTotal() {
			return total;
		}

		@Override
		public boolean isVip() {
			return vip;
		}
	}

	private Order order(String total, boolean vip) {
		return new TestOrder(new BigDecimal(total), vip);
	}

	@Test
	void calcularDescuento_total500_devuelve0() {
		var result = service.calcularDescuento(order("500.00", false));
		assertEquals(new BigDecimal("0.00"), result);
	}

	@Test
	void calcularDescuento_total1500NoVip_devuelve150() {
		var result = service.calcularDescuento(order("1500.00", false));
		assertEquals(new BigDecimal("150.00"), result);
	}

	@Test
	void ccalcularDescuento_total900Vip_devuelve45() {
		var result = service.calcularDescuento(order("900", true));
		assertEquals(new BigDecimal("45.00"), result);
	}

	@Test
	void calcularDescuento_total3000Vip_devuelve435() {
		var result = service.calcularDescuento(order("3000", true));
		assertEquals(new BigDecimal("435.00"), result);
	}

	// Explicar en el informe que el tope nunca se va a alcanzar y este test lo
	// demuestra.
	@Test
	void calcularDescuento_total10000Vip_devuelve1450() {
		var result = service.calcularDescuento(order("10000", true));
		assertEquals(new BigDecimal("1450.00"), result);
	}

	@Test
	void aplicarTope_total1000Vip_devuelve200() {
		BigDecimal total = new BigDecimal("1000");
		BigDecimal descuentoCalculado = new BigDecimal("500"); // 50% (> 20%) para forzar el tope

		var result = service.aplicarTope(descuentoCalculado, total);
		assertEquals(new BigDecimal("200.00"), result);
	}

	@Test
	void aplicarTope_total3000Vip_devuelve600() {
		BigDecimal total = new BigDecimal("3000");
		BigDecimal descuentoCalculado = new BigDecimal("2500");

		var result = service.aplicarTope(descuentoCalculado, total);
		assertEquals(new BigDecimal("600.00"), result);
	}
}
