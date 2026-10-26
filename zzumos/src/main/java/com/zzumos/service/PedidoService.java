package com.zzumos.service;

import com.zzumos.dto.Dtos.*;
import com.zzumos.model.*;
import com.zzumos.model.enums.EstadoPago;
import com.zzumos.model.enums.EstadoPedido;
import com.zzumos.model.enums.Rol;
import com.zzumos.security.UsuarioPrincipal;
import com.zzumos.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

import static com.zzumos.model.enums.EstadoPedido.*;

@Service
public class PedidoService {

    // Flujo permitido de estados (BPM "Gestionar pedido")
    private static final Map<EstadoPedido, Set<EstadoPedido>> FLUJO = Map.of(
            PENDIENTE, Set.of(EN_PREPARACION, CANCELADO),
            EN_PREPARACION, Set.of(LISTO, CANCELADO),
            LISTO, Set.of(ENTREGADO),
            ENTREGADO, Set.of(),
            CANCELADO, Set.of());

    private final PedidoRepository pedidoRepo;
    private final ClienteRepository clienteRepo;
    private final ProductoRepository productoRepo;
    private final OpcionProductoRepository opcionRepo;
    private final UsuarioRepository usuarioRepo;
    private final VentaRepository ventaRepo;
    private final PagoRepository pagoRepo;

    public PedidoService(PedidoRepository pedidoRepo, ClienteRepository clienteRepo,
                         ProductoRepository productoRepo, OpcionProductoRepository opcionRepo,
                         UsuarioRepository usuarioRepo, VentaRepository ventaRepo, PagoRepository pagoRepo) {
        this.pedidoRepo = pedidoRepo;
        this.clienteRepo = clienteRepo;
        this.productoRepo = productoRepo;
        this.opcionRepo = opcionRepo;
        this.usuarioRepo = usuarioRepo;
        this.ventaRepo = ventaRepo;
        this.pagoRepo = pagoRepo;
    }

    /** RF05-RF07: valida, calcula el total en el servidor y registra el pedido. */
    @Transactional
    public PedidoResponse crear(CrearPedidoRequest req, Integer idUsuario) {
        Cliente cliente = clienteRepo.findByUsuario_IdUsuario(idUsuario)
                .orElseThrow(() -> new NoSuchElementException("Tu cuenta no tiene un perfil de cliente"));

        Pedido pedido = new Pedido();
        // código temporal único; se reemplaza por ZZ-0001 cuando ya hay id
        pedido.setCodigoPedido("T" + UUID.randomUUID().toString().replace("-", "").substring(0, 11));
        pedido.setCliente(cliente);
        pedido.setMetodoPago(req.metodoPago());
        pedido.setDireccionEntrega(req.direccionEntrega());
        pedido.setReferencia(req.referencia());
        BigDecimal envio = req.costoEnvio() == null ? BigDecimal.ZERO : req.costoEnvio();
        pedido.setCostoEnvio(envio);

        BigDecimal total = BigDecimal.ZERO;
        for (ItemRequest item : req.items()) {
            Producto producto = productoRepo.findById(item.idProducto())
                    .filter(Producto::isDisponible)
                    .orElseThrow(() -> new NoSuchElementException(
                            "Producto no disponible: " + item.idProducto()));

            DetallePedido detalle = new DetallePedido();
            detalle.setPedido(pedido);
            detalle.setProducto(producto);
            detalle.setCantidad(item.cantidad());
            detalle.setPrecioUnitario(producto.getPrecioBase());
            detalle.setNotaCocina(item.notaCocina());

            BigDecimal unitario = producto.getPrecioBase();
            if (item.opciones() != null) {
                for (Integer idOpcion : item.opciones()) {
                    OpcionProducto opcion = opcionRepo.findById(idOpcion)
                            .filter(o -> o.isDisponible()
                                    && o.getProducto().getIdProducto().equals(producto.getIdProducto()))
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Opción inválida para el producto: " + idOpcion));
                    DetallePedidoOpcion dpo = new DetallePedidoOpcion();
                    dpo.setDetalle(detalle);
                    dpo.setOpcion(opcion);
                    dpo.setPrecioAdicional(opcion.getPrecioAdicional());
                    detalle.getOpciones().add(dpo);
                    unitario = unitario.add(opcion.getPrecioAdicional());
                }
            }
            total = total.add(unitario.multiply(BigDecimal.valueOf(item.cantidad())));
            pedido.getDetalles().add(detalle);
        }
        pedido.setTotal(total.add(envio));

        pedidoRepo.saveAndFlush(pedido);
        pedido.setCodigoPedido(String.format("ZZ-%04d", pedido.getIdPedido()));
        return toResponse(pedido);
    }

    /** RF09: cambia el estado; al ENTREGAR registra la venta y el pago (RF10). */
    @Transactional
    public PedidoResponse cambiarEstado(Integer idPedido, CambiarEstadoRequest req, Integer idUsuarioAtencion) {
        Pedido pedido = pedidoRepo.findById(idPedido)
                .orElseThrow(() -> new NoSuchElementException("El pedido no existe"));

        if (!FLUJO.get(pedido.getEstado()).contains(req.estado())) {
            throw new IllegalStateException(
                    "No se puede pasar de " + pedido.getEstado() + " a " + req.estado());
        }
        pedido.setEstado(req.estado());

        pedido.setUsuarioAtencion(usuarioRepo.getReferenceById(idUsuarioAtencion));

        if (req.estado() == ENTREGADO && !ventaRepo.existsByPedido(pedido)) {
            Venta venta = new Venta();
            venta.setPedido(pedido);
            ventaRepo.save(venta);

            Pago pago = new Pago();
            pago.setVenta(venta);
            pago.setMetodo(pedido.getMetodoPago());
            pago.setEstadoPago(EstadoPago.CONFIRMADO);
            pago.setFechaPago(LocalDateTime.now());
            pagoRepo.save(pago);
        }
        return toResponse(pedido);
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listar(EstadoPedido estado) {
        List<Pedido> pedidos = (estado == null)
                ? pedidoRepo.findAllByOrderByFechaHoraDesc()
                : pedidoRepo.findByEstadoOrderByFechaHoraDesc(estado);
        return pedidos.stream().map(this::toResponse).toList();
    }

    /** Seguimiento por código. Un CLIENTE solo puede ver sus propios pedidos (404 si no es suyo). */
    @Transactional(readOnly = true)
    public PedidoResponse buscarPorCodigo(String codigo, UsuarioPrincipal usuario) {
        Pedido pedido = pedidoRepo.findByCodigoPedido(codigo)
                .orElseThrow(() -> new NoSuchElementException("Pedido no encontrado: " + codigo));
        if (usuario.getRol() == Rol.CLIENTE) {
            Usuario dueno = pedido.getCliente().getUsuario();
            if (dueno == null || !dueno.getIdUsuario().equals(usuario.getIdUsuario())) {
                throw new NoSuchElementException("Pedido no encontrado: " + codigo);
            }
        }
        return toResponse(pedido);
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarMisPedidos(Integer idUsuario) {
        return pedidoRepo.findByCliente_Usuario_IdUsuarioOrderByFechaHoraDesc(idUsuario)
                .stream().map(this::toResponse).toList();
    }

    private PedidoResponse toResponse(Pedido p) {
        List<ItemResponse> items = p.getDetalles().stream()
                .map(d -> new ItemResponse(
                        d.getProducto().getNombre(), d.getCantidad(), d.getPrecioUnitario(),
                        d.getOpciones().stream().map(o -> o.getOpcion().getNombre()).toList(),
                        d.getNotaCocina()))
                .toList();
        return new PedidoResponse(p.getIdPedido(), p.getCodigoPedido(), p.getEstado(), p.getMetodoPago(),
                p.getFechaHora(), p.getCliente().nombreCompleto(), items, p.getCostoEnvio(), p.getTotal());
    }
}
