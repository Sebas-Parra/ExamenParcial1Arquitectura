<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %><%@ page import="java.util.List,ec.edu.monster.modelo.Factura" %><%
String titulo = "Facturas";
String subtitulo = "Consulta las ventas registradas y su detalle.";
List<Factura> facturas = (List<Factura>) request.getAttribute("facturas");
String cedula = (String) request.getAttribute("cedula");
%><%@ include file="encabezado.jspf" %>
<section class="panel">
  <form class="fila-busqueda" method="get" action="<%=ctx%>/facturas">
    <div>
      <label for="cedula">Cédula del comprador</label>
      <input id="cedula" name="cedula" inputmode="numeric" pattern="\d{10}" maxlength="10" title="10 dígitos" placeholder="Vacío para ver todas" value="<%=esc(cedula)%>">
    </div>
    <button type="submit">Buscar</button>
  </form>
  <div class="tabla-contenedor">
    <table>
      <thead><tr><th class="num">N.º</th><th>Fecha</th><th>Cédula</th><th>Cliente</th><th>Forma de pago</th><th class="num">Total</th><th>Crédito</th><th></th></tr></thead>
      <tbody>
      <% if (facturas == null || facturas.isEmpty()) { %>
        <tr><td colspan="8" class="vacio">No hay facturas registradas<%=cedula == null || cedula.isEmpty() ? "" : " para esa cédula"%>.</td></tr>
      <% } else for (Factura f : facturas) { %>
        <tr>
          <td class="num"><%=f.getCodFactura()%></td>
          <td class="nowrap"><%=VistaHtml.fecha(f.getFecha())%></td>
          <td><%=esc(f.getCedula())%></td>
          <td><%=esc(f.getNombre())%></td>
          <td><span class="etiqueta<%=f.getCodCredito() == null ? " etiqueta--gris" : ""%>"><%=VistaHtml.formaPago(f.getFormaPago())%></span></td>
          <td class="num"><%=dinero(f.getTotal())%></td>
          <td><%=f.getCodCredito() == null ? "-" : "N.º " + f.getCodCredito() + " · " + f.getNumeroCuotas() + " cuotas"%></td>
          <td class="acciones"><a class="link" href="<%=ctx%>/facturas?id=<%=f.getCodFactura()%>">Ver detalle</a></td>
        </tr>
      <% } %>
      </tbody>
    </table>
  </div>
</section>
<%@ include file="pie.jspf" %>
