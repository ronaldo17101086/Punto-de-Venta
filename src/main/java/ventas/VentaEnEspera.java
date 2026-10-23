package ventas;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class VentaEnEspera {

    private String folio;
    private String clienteNombre;
    private Object clienteSeleccionado;
    private Long clienteId; // <--- NUEVO: Guardamos el ID exacto del cliente
    private List<?> itemsCarrito;
    private String comentario;
    private BigDecimal total;
    private LocalDateTime fechaCreacion;

    public VentaEnEspera(String clienteNombre, Object clienteSeleccionado, Long clienteId, List<?> itemsCarrito, String comentario, BigDecimal total) {
        this.folio = "TK-" + System.currentTimeMillis() % 10000;
        this.clienteNombre = (clienteNombre != null && !clienteNombre.isEmpty()) ? clienteNombre : "Público General";
        this.clienteSeleccionado = clienteSeleccionado;
        this.clienteId = clienteId; // <--- NUEVO
        this.itemsCarrito = itemsCarrito;
        this.comentario = comentario;
        this.total = total;
        this.fechaCreacion = LocalDateTime.now();
    }

    @Override
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm:ss");
        return String.format("[%s] Cliente: %s | Total: $%s | Hora: %s",
                folio, clienteNombre, total.toString(), fechaCreacion.format(fmt));
    }

    public String getFolio() {
        return folio;
    }

    public Object getClienteSeleccionado() {
        return clienteSeleccionado;
    }

 public Long getClienteId() { 
    return clienteId; 
}

    public List<?> getItemsCarrito() {
        return itemsCarrito;
    }

    public String getComentario() {
        return comentario;
    }

    public BigDecimal getTotal() {
        return total;
    }
}
