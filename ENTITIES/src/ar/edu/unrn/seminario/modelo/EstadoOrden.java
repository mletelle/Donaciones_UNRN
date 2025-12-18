package ar.edu.unrn.seminario.modelo;

public enum EstadoOrden {
	
	// constantes con logica de transicion
	PENDIENTE("Pendiente") {
		@Override
		public boolean esTransicionValida(EstadoOrden nuevoEstado) {
			// solo puede pasar a en ejecucion al iniciar visitas
			return nuevoEstado == EN_EJECUCION;
		}
	},
	EN_EJECUCION("En Ejecucion") {
		@Override
		public boolean esTransicionValida(EstadoOrden nuevoEstado) {
			// puede completarse pero no volver a pendiente
			return nuevoEstado == COMPLETADO;
		}
	},
	COMPLETADO("Completado") {
		@Override
		public boolean esTransicionValida(EstadoOrden nuevoEstado) {
			return false; // estado terminal
		}
	};

	private String descripcion;
	
	EstadoOrden(String descripcion) {
		this.descripcion = descripcion;
	}
	
	// metodo abstracto que obliga a cada constante a definir sus reglas
	public abstract boolean esTransicionValida(EstadoOrden nuevoEstado);
	
	@Override
	public String toString() {
		return descripcion;
	}
	
} 
