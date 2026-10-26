package com.zzumos.dto;

import com.zzumos.model.enums.EstadoPedido;
import com.zzumos.model.enums.Grupo;
import com.zzumos.model.enums.MetodoPago;
import com.zzumos.model.enums.Rol;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class Dtos {
    private Dtos() {}

    // ---- Autenticación (RF01, RF02)
    public record RegistroRequest(
            @NotBlank @Size(max = 100) String nombres,
            @Size(max = 100) String apellidos,
            @NotBlank @Pattern(regexp = "^\\+?\\d{9,14}$", message = "Teléfono inválido (solo dígitos, 9 a 14)") String telefono,
            @NotBlank @Email @Size(max = 120) String correo,
            @NotBlank @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres") String contrasena) {}

    public record LoginRequest(@NotBlank String correo, @NotBlank String contrasena) {}

    public record UsuarioResponse(Integer id, String nombres, String correo, Rol rol) {}

    // ---- Catálogo (RF03, RF04)
    public record OpcionDto(Integer id, Grupo grupo, String nombre, BigDecimal precioAdicional) {}
    public record ProductoDto(Integer id, String nombre, String descripcion,
                              BigDecimal precioBase, List<OpcionDto> opciones) {}

    // ---- Crear pedido (RF05, RF06, RF07). El cliente sale de la sesión, no del JSON.
    public record ItemRequest(
            @NotNull Integer idProducto,
            @NotNull @Min(1) Integer cantidad,
            List<Integer> opciones,
            @Size(max = 200) String notaCocina) {}

    public record CrearPedidoRequest(
            @NotEmpty @Valid List<ItemRequest> items,
            @NotNull MetodoPago metodoPago,
            @Size(max = 200) String direccionEntrega,
            @Size(max = 120) String referencia,
            BigDecimal costoEnvio) {}

    // ---- Cambiar estado (RF09). El personal sale de la sesión.
    public record CambiarEstadoRequest(@NotNull EstadoPedido estado) {}

    // ---- Respuestas (RF08)
    public record ItemResponse(String producto, Integer cantidad, BigDecimal precioUnitario,
                               List<String> opciones, String notaCocina) {}
    public record PedidoResponse(Integer id, String codigo, EstadoPedido estado, MetodoPago metodoPago,
                                 LocalDateTime fechaHora, String cliente, List<ItemResponse> items,
                                 BigDecimal costoEnvio, BigDecimal total) {}
}
