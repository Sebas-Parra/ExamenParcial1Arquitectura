(() => {
  const celularDialog = document.getElementById("celular-dialog");
  const retirarDialog = document.getElementById("retirar-dialog");
  if (!celularDialog || !retirarDialog) return;

  const nuevo = document.getElementById("nuevo-celular");
  nuevo?.addEventListener("click", () => celularDialog.showModal());

  if (celularDialog.dataset.open === "true") celularDialog.showModal();

  document.querySelectorAll("[data-cerrar-dialogo]").forEach((boton) => {
    boton.addEventListener("click", () => boton.closest("dialog")?.close());
  });

  const codigoRetirar = document.getElementById("retirar-codigo");
  const mensajeRetirar = document.getElementById("retirar-mensaje");
  document.querySelectorAll(".boton-retirar").forEach((boton) => {
    boton.addEventListener("click", () => {
      codigoRetirar.value = boton.dataset.codigo;
      mensajeRetirar.textContent = `¿Retirar ${boton.dataset.nombre} del catálogo? Sus facturas se conservan.`;
      retirarDialog.showModal();
    });
  });
})();
