<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %><%@ page import="java.util.List,ec.edu.monster.modelo.Celular" %><%
String titulo = "Catálogo de celulares";
String subtitulo = "Registra, actualiza y retira los teléfonos disponibles para la venta.";
List<Celular> celulares = (List<Celular>) request.getAttribute("celulares");
Celular formulario = (Celular) request.getAttribute("formulario");
boolean todos = Boolean.TRUE.equals(request.getAttribute("todos"));
boolean edicion = formulario != null && formulario.getCodCelular() != null;
String precioIngresado = (String) request.getAttribute("precioIngresado");
if (precioIngresado == null && formulario != null && formulario.getPrecio() != null) precioIngresado = formulario.getPrecio().toPlainString();
String parametroTodos = todos ? "&todos=true" : "";
%><%@ include file="encabezado.jspf" %>
<div class="columnas columnas--catalogo">
  <section class="panel">
    <div class="barra">
      <h2>Celulares</h2>
      <a class="link" href="<%=ctx%>/catalogo<%=todos ? "" : "?todos=true"%>"><%=todos ? "Ocultar retirados" : "Mostrar retirados"%></a>
    </div>
    <div class="tabla-contenedor">
      <table>
        <thead><tr><th class="num">Cód.</th><th>Celular</th><th>Descripción</th><th class="num">Precio</th><th>Estado</th><th></th></tr></thead>
        <tbody>
        <% if (celulares == null || celulares.isEmpty()) { %>
          <tr><td colspan="6" class="vacio">No hay celulares en el catálogo.</td></tr>
        <% } else for (Celular c : celulares) { boolean retirado = Boolean.FALSE.equals(c.getDisponible()); %>
          <tr<%=retirado ? " class=\"retirado\"" : ""%>>
            <td class="num"><%=c.getCodCelular()%></td>
            <td><strong><%=esc(c.getMarca())%></strong> <%=esc(c.getModelo())%></td>
            <td><%=esc(c.getDescripcion())%></td>
            <td class="num"><%=dinero(c.getPrecio())%></td>
            <td><span class="etiqueta<%=retirado ? " etiqueta--gris" : ""%>"><%=retirado ? "Retirado" : "Disponible"%></span></td>
            <td class="acciones">
              <a class="link" href="<%=ctx%>/catalogo?editar=<%=c.getCodCelular()%><%=parametroTodos%>">Editar</a>
              <% if (!retirado) { %>
              <form method="post" action="<%=ctx%>/catalogo" onsubmit="return confirm(this.dataset.pregunta)"
                    data-pregunta="¿Retirar <%=esc(c.nombre())%> del catálogo? Sus facturas se conservan.">
                <input type="hidden" name="csrf" value="<%=csrf%>">
                <input type="hidden" name="accion" value="retirar">
                <input type="hidden" name="codCelular" value="<%=c.getCodCelular()%>">
                <input type="hidden" name="todos" value="<%=todos%>">
                <button class="link peligro" type="submit">Retirar</button>
              </form>
              <% } %>
            </td>
          </tr>
        <% } %>
        </tbody>
      </table>
    </div>
  </section>

  <section class="panel" id="formulario">
    <h2><%=edicion ? "Editar celular N.º " + formulario.getCodCelular() : "Nuevo celular"%></h2>
    <form method="post" action="<%=ctx%>/catalogo">
      <input type="hidden" name="csrf" value="<%=csrf%>">
      <input type="hidden" name="accion" value="guardar">
      <input type="hidden" name="todos" value="<%=todos%>">
      <% if (edicion) { %><input type="hidden" name="codCelular" value="<%=formulario.getCodCelular()%>"><% } %>
      <label for="marca">Marca</label>
      <input id="marca" name="marca" required maxlength="50" value="<%=formulario == null ? "" : esc(formulario.getMarca())%>">
      <label for="modelo">Modelo</label>
      <input id="modelo" name="modelo" required maxlength="100" value="<%=formulario == null ? "" : esc(formulario.getModelo())%>">
      <label for="descripcion">Descripción <span class="muted">(opcional)</span></label>
      <input id="descripcion" name="descripcion" maxlength="255" value="<%=formulario == null ? "" : esc(formulario.getDescripcion())%>">
      <label for="precio">Precio (USD)</label>
      <input id="precio" name="precio" required inputmode="decimal" maxlength="12" value="<%=esc(precioIngresado)%>">
      <% if (edicion) { %>
        <label class="check"><input type="checkbox" name="disponible"<%=Boolean.FALSE.equals(formulario.getDisponible()) ? "" : " checked"%>> Disponible para la venta</label>
      <% } %>
      <div class="form-actions">
        <button type="submit"><%=edicion ? "Guardar cambios" : "Registrar celular"%></button>
        <% if (edicion) { %><a class="boton secondary" href="<%=ctx%>/catalogo<%=todos ? "?todos=true" : ""%>">Cancelar</a><% } %>
      </div>
    </form>
  </section>
</div>
<%@ include file="pie.jspf" %>
