package com.nexuspos.backend.service;

import com.nexuspos.backend.model.Caja;
import com.nexuspos.backend.model.Mesa;
import com.nexuspos.backend.model.Venta;
import com.nexuspos.backend.repository.CajaRepository;
import com.nexuspos.backend.repository.MesaRepository;
import com.nexuspos.backend.repository.VentaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VentaService {

    private final VentaRepository ventaRepository;
    private final MesaRepository mesaRepository;
    private final CajaRepository cajaRepository;
    private final MesaService mesaService;
    private final ObjectMapper objectMapper;

    /**
     * Cerrar mesa y crear venta
     *
     * @Transactional: Garantiza que todas las operaciones se completen o se revierta todo
     * @param mesaId ID de la mesa a cerrar
     * @param efectivo Monto en efectivo
     * @param transferencias Monto en transferencias
     * @param cajaId ID de la caja donde se registra la venta (opcional, usa la primera caja abierta si es null)
     */
    @Transactional
    public Venta cerrarMesaConVenta(Long mesaId, Double efectivo, Double transferencias, Long cajaId) {
        log.info("Cerrando mesa {} y creando venta en caja {}", mesaId, cajaId);

        // 1. Buscar mesa
        Mesa mesa = mesaRepository.findById(mesaId)
            .orElseThrow(() -> new IllegalArgumentException("Mesa no encontrada"));

        if (mesa.getPedidos().isEmpty()) {
            throw new IllegalArgumentException("La mesa no tiene pedidos");
        }

        // 2. Validar pago
        Double totalPagado = efectivo + transferencias;
        if (totalPagado < mesa.getTotal()) {
            throw new IllegalArgumentException(
                String.format("Pago insuficiente. Total: %.2f, Pagado: %.2f", mesa.getTotal(), totalPagado)
            );
        }

        // 3. Obtener caja (usar cajaId si se proporciona, o buscar la primera caja abierta)
        Caja caja;
        if (cajaId != null) {
            caja = cajaRepository.findById(cajaId)
                .orElseThrow(() -> new IllegalStateException("Caja no encontrada con ID: " + cajaId));
        } else {
            // Si no se especifica caja, usar la primera caja abierta
            List<Caja> cajasAbiertas = cajaRepository.findByAbierta(true);
            if (cajasAbiertas.isEmpty()) {
                throw new IllegalStateException("No hay cajas abiertas");
            }
            caja = cajasAbiertas.get(0);
        }

        if (!caja.getAbierta()) {
            throw new IllegalStateException("La caja está cerrada");
        }

        // 4. Crear venta (snapshot de los datos)
        Venta venta = new Venta();
        venta.setMesa(mesa.getNumero() + " - " + mesa.getNombre());
        venta.setTotal(mesa.getTotal());
        venta.setEfectivo(efectivo);
        venta.setTransferencias(transferencias);

        // Convertir pedidos a JSON para guardar snapshot
        try {
            String productosJson = objectMapper.writeValueAsString(mesa.getPedidos());
            venta.setProductosJson(productosJson);
        } catch (Exception e) {
            log.error("Error al serializar pedidos", e);
            venta.setProductosJson("[]");
        }

        // 5. Sumar dinero a la caja
        caja.setEfectivo(caja.getEfectivo() + efectivo);
        caja.setTransferencias(caja.getTransferencias() + transferencias);
        cajaRepository.save(caja);

        // 6. Liberar mesa (esto también actualiza stock)
        mesaService.liberar(mesaId);

        // 7. Guardar venta
        Venta ventaGuardada = ventaRepository.save(venta);

        log.info("Venta creada con ID: {}", ventaGuardada.getId());
        return ventaGuardada;
    }

    // Obtener todas las ventas
    public List<Venta> findAll() {
        return ventaRepository.findAll();
    }

    // Obtener venta por ID
    public Optional<Venta> findById(Long id) {
        return ventaRepository.findById(id);
    }

    // Contar ventas
    public long count() {
        return ventaRepository.count();
    }

    // Calcular totales
    public Double calcularTotalVentas() {
        Double total = ventaRepository.calcularTotalVentas();
        return total != null ? total : 0.0;
    }

    public Double calcularTotalEfectivo() {
        Double total = ventaRepository.calcularTotalEfectivo();
        return total != null ? total : 0.0;
    }

    public Double calcularTotalTransferencias() {
        Double total = ventaRepository.calcularTotalTransferencias();
        return total != null ? total : 0.0;
    }

    // ========= ESTADÍSTICAS DIARIAS =========

    /**
     * Obtener estadísticas de ventas de los últimos N días
     */
    public Map<String, Object> obtenerEstadisticasDiarias(int dias) {
        LocalDateTime fechaInicio = LocalDateTime.now().minusDays(dias);
        List<Venta> ventas = ventaRepository.findVentasUltimosDias(fechaInicio);

        // Agrupar ventas por día
        Map<String, List<Venta>> ventasPorDia = ventas.stream()
            .collect(Collectors.groupingBy(venta ->
                venta.getCreatedAt().toLocalDate().toString()
            ));

        // Calcular totales por día
        List<Map<String, Object>> ventasDiarias = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (Map.Entry<String, List<Venta>> entry : ventasPorDia.entrySet()) {
            String fecha = entry.getKey();
            List<Venta> ventasDelDia = entry.getValue();

            Double totalDia = ventasDelDia.stream()
                .mapToDouble(Venta::getTotal)
                .sum();

            Double efectivoDia = ventasDelDia.stream()
                .mapToDouble(Venta::getEfectivo)
                .sum();

            Double transferenciasDia = ventasDelDia.stream()
                .mapToDouble(Venta::getTransferencias)
                .sum();

            Map<String, Object> dia = new HashMap<>();
            dia.put("fecha", fecha);
            dia.put("total", totalDia);
            dia.put("efectivo", efectivoDia);
            dia.put("transferencias", transferenciasDia);
            dia.put("cantidad", ventasDelDia.size());

            ventasDiarias.add(dia);
        }

        // Ordenar por fecha
        ventasDiarias.sort((a, b) ->
            ((String) a.get("fecha")).compareTo((String) b.get("fecha"))
        );

        // Encontrar mejor y peor día
        Map<String, Object> mejorDia = ventasDiarias.stream()
            .max(Comparator.comparing(d -> (Double) d.get("total")))
            .orElse(null);

        Map<String, Object> peorDia = ventasDiarias.stream()
            .min(Comparator.comparing(d -> (Double) d.get("total")))
            .orElse(null);

        // Calcular totales generales
        Double totalGeneral = ventasDiarias.stream()
            .mapToDouble(d -> (Double) d.get("total"))
            .sum();

        Double promediosDiario = ventasDiarias.isEmpty() ? 0.0 :
            totalGeneral / ventasDiarias.size();

        // Construir respuesta
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("ventasPorDia", ventasDiarias);
        resultado.put("mejorDia", mejorDia);
        resultado.put("peorDia", peorDia);
        resultado.put("totalGeneral", totalGeneral);
        resultado.put("promedioDiario", promediosDiario);
        resultado.put("totalDias", ventasDiarias.size());

        return resultado;
    }

    /**
     * Obtener estadísticas de un rango de fechas específico
     */
    public Map<String, Object> obtenerEstadisticasRango(LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(LocalTime.MAX);

        Double total = ventaRepository.calcularTotalVentasEnRango(inicio, fin);
        Long cantidad = ventaRepository.contarVentasEnRango(inicio, fin);

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("totalVentas", total != null ? total : 0.0);
        resultado.put("cantidadVentas", cantidad != null ? cantidad : 0);
        resultado.put("fechaInicio", fechaInicio.toString());
        resultado.put("fechaFin", fechaFin.toString());

        return resultado;
    }

    /**
     * Obtener productos más vendidos
     * Analiza el JSON de productos en las ventas
     */
    public List<Map<String, Object>> obtenerProductosMasVendidos(int limite) {
        List<Venta> todasVentas = ventaRepository.findAll();
        Map<String, Map<String, Object>> productoStats = new HashMap<>();

        for (Venta venta : todasVentas) {
            try {
                // Parsear JSON de productos
                List<Map<String, Object>> productos = objectMapper.readValue(
                    venta.getProductosJson(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class)
                );

                for (Map<String, Object> producto : productos) {
                    String nombre = (String) producto.get("nombre");
                    Integer cantidad = (Integer) producto.get("cantidad");
                    Double precio = ((Number) producto.get("precio")).doubleValue();
                    String categoria = (String) producto.getOrDefault("categoria", "Sin categoría");

                    if (nombre != null && cantidad != null) {
                        productoStats.putIfAbsent(nombre, new HashMap<>());
                        Map<String, Object> stats = productoStats.get(nombre);

                        int cantidadActual = (int) stats.getOrDefault("cantidadVendida", 0);
                        double totalActual = (double) stats.getOrDefault("totalVentas", 0.0);

                        stats.put("nombre", nombre);
                        stats.put("categoria", categoria);
                        stats.put("cantidadVendida", cantidadActual + cantidad);
                        stats.put("totalVentas", totalActual + (precio * cantidad));
                        stats.put("precioPromedio", precio);
                    }
                }
            } catch (Exception e) {
                log.error("Error al procesar productos de venta {}: {}", venta.getId(), e.getMessage());
            }
        }

        // Convertir a lista y ordenar por cantidad vendida
        return productoStats.values().stream()
            .sorted((a, b) -> Integer.compare(
                (int) b.get("cantidadVendida"),
                (int) a.get("cantidadVendida")
            ))
            .limit(limite)
            .collect(Collectors.toList());
    }

    /**
     * Obtener ventas por categoría
     */
    public Map<String, Map<String, Object>> obtenerVentasPorCategoria() {
        List<Venta> todasVentas = ventaRepository.findAll();
        Map<String, Map<String, Object>> categoriaStats = new HashMap<>();

        for (Venta venta : todasVentas) {
            try {
                List<Map<String, Object>> productos = objectMapper.readValue(
                    venta.getProductosJson(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class)
                );

                for (Map<String, Object> producto : productos) {
                    String categoria = (String) producto.getOrDefault("categoria", "Sin categoría");
                    Integer cantidad = (Integer) producto.get("cantidad");
                    Double precio = ((Number) producto.get("precio")).doubleValue();

                    categoriaStats.putIfAbsent(categoria, new HashMap<>());
                    Map<String, Object> stats = categoriaStats.get(categoria);

                    int cantidadActual = (int) stats.getOrDefault("cantidad", 0);
                    double totalActual = (double) stats.getOrDefault("total", 0.0);

                    stats.put("categoria", categoria);
                    stats.put("cantidad", cantidadActual + cantidad);
                    stats.put("total", totalActual + (precio * cantidad));
                }
            } catch (Exception e) {
                log.error("Error al procesar productos de venta {}: {}", venta.getId(), e.getMessage());
            }
        }

        return categoriaStats;
    }

    /**
     * Obtener resumen completo del dashboard
     */
    public Map<String, Object> obtenerResumenDashboard() {
        Map<String, Object> resumen = new HashMap<>();

        // Totales generales
        resumen.put("totalVentas", calcularTotalVentas());
        resumen.put("totalEfectivo", calcularTotalEfectivo());
        resumen.put("totalTransferencias", calcularTotalTransferencias());
        resumen.put("cantidadVentas", count());

        // Productos más vendidos (top 10)
        resumen.put("productosMasVendidos", obtenerProductosMasVendidos(10));

        // Ventas por categoría
        resumen.put("ventasPorCategoria", obtenerVentasPorCategoria());

        // Estadísticas de últimos 7 días
        resumen.put("ventasUltimos7Dias", obtenerEstadisticasDiarias(7));

        return resumen;
    }
}
