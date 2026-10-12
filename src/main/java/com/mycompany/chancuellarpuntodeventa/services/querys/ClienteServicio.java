package com.mycompany.chancuellarpuntodeventa.services.querys;

import com.mycompany.chancuellarpuntodeventa.services.dtos.ClienteDTO;
import com.mycompany.chancuellarpuntodeventa.services.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;

@Service
public class ClienteServicio {

    @Autowired
    private ClienteRepository clienteRepository;

    public List<ClienteDTO> obtenerTodosLosClientes() {
        return clienteRepository.findAll();
    }

    // NUEVO MÉTODO AGREGADO PARA BUSCAR POR ID
    public ClienteDTO obtenerClientePorId(Long id) {
        return clienteRepository.findById(id).orElse(null);
    }

    public List<ClienteDTO> buscarClientes(String filtro) {
        if (filtro == null || filtro.trim().isEmpty()) {
            return clienteRepository.findAll();
        }
        return clienteRepository.buscarPorNombreOTelefono(filtro.trim());
    }

    public void guardarCliente(ClienteDTO cliente) {
        clienteRepository.save(cliente);
    }

    // Actualizar datos del cliente
    public void actualizarCliente(Long id, String nombre, String telefono, String direccion, BigDecimal limiteCredito) throws Exception {
        ClienteDTO cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new Exception("El cliente no existe en la base de datos."));

        cliente.setNombre(nombre);
        cliente.setTelefono(telefono);
        cliente.setDireccion(direccion);
        cliente.setLimiteCredito(limiteCredito);

        clienteRepository.save(cliente);
    }

    // Eliminar cliente con validación de deuda pendiente
    public void eliminarCliente(Long id) throws Exception {
        ClienteDTO cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new Exception("El cliente no existe."));

        BigDecimal deuda = cliente.getDeudaActual() != null ? cliente.getDeudaActual() : BigDecimal.ZERO;
        if (deuda.compareTo(BigDecimal.ZERO) > 0) {
            throw new Exception("No se puede eliminar al cliente porque tiene una deuda pendiente de $" + deuda + ".");
        }

        clienteRepository.deleteById(id);
    }

    public void registrarAbono(Long clienteId, BigDecimal montoAbono) throws Exception {
        ClienteDTO cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new Exception("El cliente no existe en la base de datos."));

        if (montoAbono == null || montoAbono.compareTo(BigDecimal.ZERO) <= 0) {
            throw new Exception("El monto del abono debe ser mayor a cero.");
        }

        BigDecimal deudaActual = cliente.getDeudaActual() != null ? cliente.getDeudaActual() : BigDecimal.ZERO;

        if (deudaActual.compareTo(BigDecimal.ZERO) == 0) {
            throw new Exception("El cliente no cuenta con deudas pendientes por saldar.");
        }

        if (montoAbono.compareTo(deudaActual) > 0) {
            throw new Exception("El abono no puede ser mayor a la deuda actual (" + deudaActual + ").");
        }

        BigDecimal nuevaDeuda = deudaActual.subtract(montoAbono);
        clienteRepository.actualizarDeudaPorId(clienteId, nuevaDeuda);
    }

    public BigDecimal calcularDeudaTotal() {
        List<ClienteDTO> clientes = clienteRepository.findAll();
        BigDecimal total = BigDecimal.ZERO;
        for (ClienteDTO c : clientes) {
            if (c.getDeudaActual() != null) {
                total = total.add(c.getDeudaActual());
            }
        }
        return total;
    }
}