window.addEventListener("beforeunload", function () {
    // Substitui o estado atual no histórico com outra URL
    history.replaceState(null, null, "/gestao-produtos");
});