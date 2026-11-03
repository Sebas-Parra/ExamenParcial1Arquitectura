<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %><%@ page import="java.util.List,ec.edu.monster.modelo.Credito,ec.edu.monster.modelo.CuotaAmortizacion,java.net.URLEncoder,java.nio.charset.StandardCharsets" %><%
String titulo = "Tabla de amortización";
String subtitulo = "Consulta en el Banco BanQuito los créditos de un cliente y sus cuotas.";
String cedula = (String) request.getAttribute("cedula");
List<Credito> creditos = (List<Credito>) request.getAttribute("creditos");
String cliente = (String) request.getAttribute("cliente");
Credito elegido = (Credito) request.getAttribute("creditoSeleccionado");
List<CuotaAmortizacion> tabla = (List<CuotaAmortizacion>) request.getAttribute("tabla");
java.math.BigDecimal montoCredito = elegido == null ? null : elegido.getMonto();
%><%@ include file="encabezado.jspf" %>
<section class="panel">
  <form class="fila-busqueda" method="get" action="<%=ctx%>/amortizacion">
    <div>
      <label for="cedula">Cédula del cliente</label>
      <input id="cedula" name="cedula" required inputmode="numeric" pattern="\d{10}" maxlength="10" title="10 dígitos" value="<%=esc(cedula)%>">
    </div>
    <button type="submit">Buscar créditos</button>
  </form>
</section>
<% if (creditos != null) { %>
<div class="columnas columnas--amortizacion">
  <section class="panel">
    <h2><%=cliente == null ? "Créditos del cliente" : "Créditos de " + esc(cliente)%></h2>
    <% if (creditos.isEmpty()) { %>
      <p class="vacio">El cliente no tiene créditos en el Banco BanQuito.</p>
    <% } else { %>
    <div class="tabla-contenedor">
      <table>
        <thead><tr><th class="num">N.º</th><th>Fecha</th><th class="num">Monto</th><th class="num">Cuotas</th><th>Estado</th></tr></thead>
        <tbody>
        <% for (Credito c : creditos) { boolean activoCredito = elegido != null && elegido.getCodCredito() == c.getCodCredito(); %>
          <tr class="seleccionable<%=activoCredito ? " seleccionada" : ""%>">
            <td class="num"><a class="link" href="<%=ctx%>/amortizacion?cedula=<%=URLEncoder.encode(cedula, StandardCharsets.UTF_8)%>&amp;credito=<%=c.getCodCredito()%>"><%=c.getCodCredito()%></a></td>
            <td class="nowrap"><%=esc(c.getFecha())%></td>
            <td class="num"><%=dinero(c.getMonto())%></td>
            <td class="num"><%=c.getNumeroCuotas()%></td>
            <td><span class="etiqueta<%="ACTIVO".equals(c.getEstado()) ? "" : " etiqueta--gris"%>"><%=esc(c.getEstado())%></span></td>
          </tr>
        <% } %>
        </tbody>
      </table>
    </div>
    <p class="muted nota">Elija el número de un crédito para ver su tabla.</p>
    <% } %>
  </section>
  <section class="panel">
    <h2>Tabla de amortización</h2>
    <% if (elegido != null && tabla != null) { %>
      <p class="muted">Crédito N.º <%=elegido.getCodCredito()%> · <%=dinero(elegido.getMonto())%> a <%=elegido.getNumeroCuotas()%> meses ·
        cuota <%=dinero(elegido.getValorCuota())%> · tasa <%=elegido.getTasaAnual() == null ? "16.5" : elegido.getTasaAnual().toPlainString()%>% anual</p>
      <%@ include file="tabla-amortizacion.jspf" %>
    <% } else { %>
      <p class="vacio">Seleccione un crédito para ver su tabla de amortización.</p>
    <% } %>
  </section>
</div>
<% } %>
<%@ include file="pie.jspf" %>
