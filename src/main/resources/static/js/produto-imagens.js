document.addEventListener("DOMContentLoaded", function () {
    const fotosInput = document.getElementById("fotos");
    const previewContainer = document.getElementById("preview-container");
    const imagensExcluidas = document.getElementById("imagensExcluidas");

    // Objeto para acumular os arquivos
    const dataTransfer = new DataTransfer();

    // Adiciona imagens novas à visualização e prepara para envio
    fotosInput.addEventListener("change", function () {
        const files = Array.from(fotosInput.files);

        files.forEach((file) => {
            // Adicionar o arquivo ao objeto DataTransfer
            dataTransfer.items.add(file);

            const reader = new FileReader();
            reader.onload = function (e) {
                const colDiv = document.createElement("div");
                colDiv.className = "col-md-3 position-relative";

                const img = document.createElement("img");
                img.src = e.target.result;
                img.alt = "Imagem do Produto";
                img.className = "img-fluid";

                const btnRemove = document.createElement("button");
                btnRemove.type = "button";
                btnRemove.className = "btn btn-danger btn-sm position-absolute top-0 end-0";
                btnRemove.innerText = "x";
                btnRemove.addEventListener("click", function () {
                    colDiv.remove();
                    removeFileFromDataTransfer(file);
                });

                colDiv.appendChild(img);
                colDiv.appendChild(btnRemove);
                previewContainer.appendChild(colDiv);
            };

            reader.readAsDataURL(file);
        });

        // Atualizar o campo de arquivos com o novo objeto DataTransfer
        fotosInput.files = dataTransfer.files;
    });

    // Remove um arquivo do DataTransfer
    function removeFileFromDataTransfer(fileToRemove) {
        const newDataTransfer = new DataTransfer();

        Array.from(dataTransfer.files).forEach((file) => {
            if (file !== fileToRemove) {
                newDataTransfer.items.add(file);
            }
        });

        // Atualizar o DataTransfer e o input de arquivos
        dataTransfer.items.clear();
        Array.from(newDataTransfer.files).forEach((file) => {
            dataTransfer.items.add(file);
        });

        fotosInput.files = dataTransfer.files;
    }

    // Remove uma imagem existente e marca para exclusão
    window.removerImagem = function (button) {
        const imagemId = button.getAttribute("data-imagem-id");

        if (imagemId) {
            // Adiciona o ID ao campo oculto para exclusão no servidor
            imagensExcluidas.value += imagemId + ",";
        }

        // Remove a imagem visualmente
        button.parentElement.remove();
    };
});
