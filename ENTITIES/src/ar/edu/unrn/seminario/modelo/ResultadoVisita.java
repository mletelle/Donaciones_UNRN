package ar.edu.unrn.seminario.modelo;

public enum ResultadoVisita {
	
	// constantes con logica de impacto en el negocio
	RECOLECCION_EXITOSA("Recoleccion Exitosa") {
		@Override
		public void aplicarEfectos(PedidosDonacion pedido) {
			pedido.marcarCompletado();
			pedido.actualizarInventario(EstadoBien.EN_STOCK);
		}
	},
	RECOLECCION_PARCIAL("Recoleccion Parcial") {
		@Override
		public void aplicarEfectos(PedidosDonacion pedido) {
			pedido.marcarEnEjecucion();
			// inventario parcialmente actualizado - no se toca aca
		}
	},
	DONANTE_AUSENTE("Donante Ausente") {
		@Override
		public void aplicarEfectos(PedidosDonacion pedido) {
			pedido.marcarEnEjecucion();
			// no cambia inventario
		}
	},
	CANCELADO("Cancelado") {
		@Override
		public void aplicarEfectos(PedidosDonacion pedido) {
			pedido.marcarCompletado();
			// no se actualiza inventario
		}
	};

	private String descripcion;
	
	ResultadoVisita(String descripcion) {
		this.descripcion = descripcion;
	}
	
	// metodo abstracto que obliga a cada constante a definir su impacto
	public abstract void aplicarEfectos(PedidosDonacion pedido);
	
	@Override
	public String toString() {
		return descripcion;
	}
	
	public static ResultadoVisita fromString(String texto) {
		for (ResultadoVisita r : ResultadoVisita.values()) {
			if (r.descripcion.equalsIgnoreCase(texto) || r.name().equalsIgnoreCase(texto)) {
				return r;
			}
		}
		throw new IllegalArgumentException("Resultado de visita desconocido: " + texto);
	}
	
}
