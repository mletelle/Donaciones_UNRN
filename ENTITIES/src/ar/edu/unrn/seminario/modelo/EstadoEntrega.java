package ar.edu.unrn.seminario.modelo;

public enum EstadoEntrega {
	
	// constantes con logica de transicion
	PENDIENTE {
		@Override
		public boolean esTransicionValida(EstadoEntrega nuevoEstado) {
			// puede pasar a completado o cancelado
			return nuevoEstado == COMPLETADO || nuevoEstado == CANCELADO;
		}
	},
	COMPLETADO {
		@Override
		public boolean esTransicionValida(EstadoEntrega nuevoEstado) {
			return false; // estado terminal
		}
	},
	CANCELADO {
		@Override
		public boolean esTransicionValida(EstadoEntrega nuevoEstado) {
			return false; // estado terminal
		}
	};

	// metodo abstracto que obliga a cada constante a definir sus reglas
	public abstract boolean esTransicionValida(EstadoEntrega nuevoEstado);
	
}
