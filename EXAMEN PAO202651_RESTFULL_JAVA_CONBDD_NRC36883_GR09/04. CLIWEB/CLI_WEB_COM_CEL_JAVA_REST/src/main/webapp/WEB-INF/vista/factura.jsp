<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %><%@ page import="java.util.List,ec.edu.monster.modelo.Factura,ec.edu.monster.modelo.DetalleFactura,ec.edu.monster.modelo.CuotaAmortizacion" %><%
Factura f = (Factura) request.getAttribute("factura");
List<CuotaAmortizacion> tabla = (List<CuotaAmortizacion>) request.getAttribute("tabla");
java.math.BigDecimal montoCredito = f == null ? null : f.getTotal();
String titulo = f == null ? "Factura" : "Factura N.º " + f.getCodFactura();
String subtitulo = f == null ? "" : "Cliente: " + f.getNombre() + " · Cédula: " + f.getCedula()
    + " · " + VistaHtml.formaPago(f.getFormaPago());
%><%@ include file="encabezado.jspf" %>
<p class="solo-pantalla"><a class="link" href="<%=ctx%>/facturas">&larr; Volver a facturas</a></p>
<% if (f != null) { %>
<section class="panel">
  <div class="barra">
    <h2>Detalle</h2>
    <div class="factura-acciones">
      <span class="muted">Fecha: <%=VistaHtml.fecha(f.getFecha())%></span>
      <button type="button" class="secondary solo-pantalla" onclick="window.print()">Guardar como PDF</button>
    </div>
  </div>
  <div class="tabla-contenedor">
    <table>
      <thead><tr><th class="num">Cód.</th><th>Celular</th><th class="num">Cantidad</th><th class="num">P. unitario</th><th class="num">Subtotal</th></tr></thead>
      <tbody>
      <% for (DetalleFactura d : f.getDetalles()) { %>
        <tr>
          <td class="num"><%=d.getCodCelular()%></td>
          <td><%=esc(d.getMarca())%> <%=esc(d.getModelo())%></td>
          <td class="num"><%=d.getCantidad()%></td>
          <td class="num"><%=dinero(d.getPrecioUnitario())%></td>
          <td class="num"><%=dinero(d.getSubtotal())%></td>
        </tr>
      <% } %>
      </tbody>
    </table>
  </div>
  <dl class="resumen resumen--derecha">
    <dt>Subtotal</dt><dd><%=dinero(f.getSubtotal())%></dd>
    <dt><%="EFECTIVO".equals(f.getFormaPago()) ? "Descuento efectivo (42%)" : "Descuento"%></dt>
    <dd><%=f.getDescuento() != null && f.getDescuento().signum() != 0 ? "- " : ""%><%=dinero(f.getDescuento())%></dd>
    <dt class="total">TOTAL</dt><dd class="total"><%=dinero(f.getTotal())%></dd>
  </dl>
</section>
<% if (f.getCodCredito() != null) { %>
<section class="panel solo-pantalla">
  <h2>Tabla de amortización · Crédito BanQuito N.º <%=f.getCodCredito()%></h2>
  <p class="muted"><%=f.getNumeroCuotas()%> cuotas mensuales de <%=dinero(f.getValorCuota())%> · tasa 16.5% anual</p>
  <% if (tabla != null) { %><%@ include file="tabla-amortizacion.jspf" %><% } %>
</section>
<% } %>
<% } %>
<%@ include file="pie.jspf" %>
