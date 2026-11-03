<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %><%@ page import="java.util.List,ec.edu.monster.modelo.Celular,ec.edu.monster.modelo.CarritoVenta" %><%
String titulo = "Facturar venta";
String subtitulo = "Efectivo con 42% de descuento o crédito directo del Banco BanQuito.";
List<Celular> celulares = (List<Celular>) request.getAttribute("celulares");
CarritoVenta carrito = (CarritoVenta) request.getAttribute("carritoVenta");
List<CarritoVenta.Linea> lineas = carrito.lineas();
boolean credito = CarritoVenta.CREDITO.equals(request.getAttribute("formaPago"));
String cedula = (String) request.getAttribute("cedula");
String nombre = (String) request.getAttribute("nombre");
Integer cuotas = (Integer) request.getAttribute("cuotas");
if (cuotas == null) cuotas = 6;
%><%@ include file="encabezado.jspf" %>
<div class="columnas columnas--venta">
  <section class="panel">
    <h2>1. Celulares a vender</h2>
    <form class="fila-agregar" method="post" action="<%=ctx%>/facturar">
      <input type="hidden" name="csrf" value="<%=csrf%>">
      <input type="hidden" name="accion" value="agregar">
      <div class="campo-crece">
        <label for="codCelular">Celular</label>
        <select id="codCelular" name="codCelular" required>
          <% for (Celular c : celulares) { %>
            <option value="<%=c.getCodCelular()%>"><%=esc(c.nombre())%> · <%=dinero(c.getPrecio())%></option>
          <% } %>
        </select>
      </div>
      <div class="campo-cantidad">
        <label for="cantidad">Cantidad</label>
        <input id="cantidad" name="cantidad" type="number" min="1" max="<%=CarritoVenta.CANTIDAD_MAXIMA%>" value="1" required>
      </div>
      <button type="submit"<%=celulares.isEmpty() ? " disabled" : ""%>>Agregar</button>
    </form>

    <div class="tabla-contenedor">
      <table>
        <thead><tr><th>Celular</th><th class="num">Cantidad</th><th class="num">P. unitario</th><th class="num">Subtotal</th><th></th></tr></thead>
        <tbody>
        <% if (lineas.isEmpty()) { %>
          <tr><td colspan="5" class="vacio">El carrito está vacío. Agrega los celulares que se van a vender.</td></tr>
        <% } else for (int i = 0; i < lineas.size(); i++) { CarritoVenta.Linea l = lineas.get(i); %>
          <tr>
            <td><%=esc(l.celular().nombre())%></td>
            <td class="num"><%=l.cantidad()%></td>
            <td class="num"><%=dinero(l.celular().getPrecio())%></td>
            <td class="num"><%=dinero(l.subtotal())%></td>
            <td class="acciones">
              <form method="post" action="<%=ctx%>/facturar">
                <input type="hidden" name="csrf" value="<%=csrf%>">
                <input type="hidden" name="accion" value="quitar">
                <input type="hidden" name="indice" value="<%=i%>">
                <button class="link peligro" type="submit">Quitar</button>
              </form>
            </td>
          </tr>
        <% } %>
        </tbody>
      </table>
    </div>

    <div class="pie-carrito">
      <form method="post" action="<%=ctx%>/facturar">
        <input type="hidden" name="csrf" value="<%=csrf%>">
        <input type="hidden" name="accion" value="vaciar">
        <button class="secondary" type="submit"<%=lineas.isEmpty() ? " disabled" : ""%>>Vaciar carrito</button>
      </form>
      <dl class="resumen">
        <dt>Subtotal</dt><dd><%=dinero(carrito.subtotal())%></dd>
        <dt>Descuento</dt>
        <dd><span data-pago="EFECTIVO">- <%=dinero(carrito.descuento(CarritoVenta.EFECTIVO))%></span><span data-pago="CREDITO"><%=dinero(carrito.descuento(CarritoVenta.CREDITO))%></span></dd>
        <dt class="total">TOTAL</dt>
        <dd class="total"><span data-pago="EFECTIVO"><%=dinero(carrito.total(CarritoVenta.EFECTIVO))%></span><span data-pago="CREDITO"><%=dinero(carrito.total(CarritoVenta.CREDITO))%></span></dd>
      </dl>
    </div>
  </section>

  <section class="panel">
    <h2>2. Pago y comprador</h2>
    <form method="post" action="<%=ctx%>/facturar" id="formPago">
      <input type="hidden" name="csrf" value="<%=csrf%>">
      <input type="hidden" name="accion" value="facturar">
      <fieldset class="opciones">
        <legend>Forma de pago</legend>
        <label class="check"><input type="radio" name="formaPago" value="EFECTIVO"<%=credito ? "" : " checked"%>> Efectivo (42% de descuento)</label>
        <label class="check"><input type="radio" name="formaPago" value="CREDITO"<%=credito ? " checked" : ""%>> Crédito directo BanQuito</label>
      </fieldset>
      <label for="cedula">Cédula del comprador</label>
      <input id="cedula" name="cedula" required inputmode="numeric" pattern="\d{10}" maxlength="10" title="10 dígitos" value="<%=esc(cedula)%>">
      <div data-pago="EFECTIVO">
        <label for="nombre">Nombre del comprador</label>
        <input id="nombre" name="nombre" maxlength="100" value="<%=esc(nombre)%>">
      </div>
      <div data-pago="CREDITO">
        <label for="cuotas">Número de cuotas mensuales</label>
        <select id="cuotas" name="cuotas">
          <% for (int n = 3; n <= 18; n++) { %><option value="<%=n%>"<%=n == cuotas ? " selected" : ""%>><%=n%> meses</option><% } %>
        </select>
        <p class="muted nota">El banco verifica si el comprador es sujeto de crédito y su monto máximo. El nombre se toma del banco.</p>
      </div>
      <p class="muted nota" data-pago="EFECTIVO">Se aplica el 42% de descuento sobre el subtotal.</p>
      <button type="submit" class="ancho"<%=lineas.isEmpty() ? " disabled" : ""%>>Facturar</button>
    </form>
  </section>
</div>
<script>
  // Muestra solo los campos y totales de la forma de pago elegida; sin JavaScript se ven ambos.
  (function () {
    var radios = document.querySelectorAll('input[name="formaPago"]');
    function actualizar() {
      var forma = document.querySelector('input[name="formaPago"]:checked').value;
      document.querySelectorAll('[data-pago]').forEach(function (el) { el.hidden = el.dataset.pago !== forma; });
      document.getElementById('nombre').required = forma === 'EFECTIVO';
    }
    radios.forEach(function (r) { r.addEventListener('change', actualizar); });
    actualizar();
  })();
</script>
<%@ include file="pie.jspf" %>
