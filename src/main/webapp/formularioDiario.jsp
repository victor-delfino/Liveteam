<%@ page import="java.time.LocalDate" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Inserir Dados Diários</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/@fortawesome/fontawesome-free/css/all.min.css" rel="stylesheet">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/assets/css/pages/formulario-diario-page.css">
    <%@ include file="WEB-INF/jspf/html-head.jspf" %>
</head>
<body>
    <% 
    if (request.getSession(false) == null || request.getSession(false).getAttribute("usuarioLogado") == null) {
        response.sendRedirect("login.jsp");
        return;
    }
    %>
    <%@ include file="WEB-INF/jspf/header.jspf" %>
    
    <%
        LocalDate dataAtual = LocalDate.now();
        int diaAtual = dataAtual.getDayOfMonth();
        int mesAtual = dataAtual.getMonthValue();
        int anoAtual = dataAtual.getYear();
    %>
    
    <div class="container mt-4 form-container">
        <h2>Inserir Informações do dia <%= diaAtual %>/<%= mesAtual %></h2>
        
        <form action="salvar-dados" method="post" id="formDiario" novalidate enctype="multipart/form-data">
            <!-- Data (oculta) -->
            <div class="form-section" style="display:none;">
                <div class="row">
                    <div class="col-md-4 mb-3">
                        <label for="dia" class="form-label">Dia</label>
                        <input type="number" id="dia" name="dia" class="form-control" value="<%= diaAtual %>" readonly>
                    </div>
                    <div class="col-md-4 mb-3">
                        <label for="mes" class="form-label">Mês</label>
                        <input type="number" id="mes" name="mes" class="form-control" value="<%= mesAtual %>" readonly>
                    </div>
                    <div class="col-md-4 mb-3">
                        <label for="ano" class="form-label">Ano</label>
                        <input type="number" id="ano" name="ano" class="form-control" value="<%= anoAtual %>" readonly>
                    </div>
                </div>
            </div>

            <!-- Alimentação -->
            <div class="row">
                <div class="form-section col-md-3">
                    <h3>Alimentação</h3>
                    
                    <!-- Café da Manhã -->
                    <div class="mb-3">
                        <label for="cafe_da_manha" class="form-label">Café da Manhã</label>
                        <div class="row g-2 align-items-center">
                            <div class="col-8">
                                <textarea id="cafe_da_manha" name="cafe_da_manha" class="form-control" maxlength="500" required></textarea>
                                <div class="invalid-feedback">Por favor, descreva seu café da manhã</div>
                            </div>
                            <div class="col-4 d-flex justify-content-center">
                                <input type="file" id="imageUploadCafe" name="cafe_imagem" accept="image/*" onchange="uploadImage(this, 'cafe_da_manha')" hidden>
                                <label for="imageUploadCafe" class="btn btn-outline-secondary w-100" aria-label="Fazer upload de imagem para Café da Manhã">
                                    <i class="fas fa-camera"></i>
                                </label>
                            </div>
                        </div>
                    </div>

                    <!-- Almoço -->
                    <div class="mb-3">
                        <label for="almoco" class="form-label">Almoço</label>
                        <div class="row g-2 align-items-center">
                            <div class="col-8">
                                <textarea id="almoco" name="almoco" class="form-control" maxlength="500" required></textarea>
                                <div class="invalid-feedback">Por favor, descreva seu almoço</div>
                            </div>
                            <div class="col-4 d-flex justify-content-center">
                                <input type="file" id="imageUploadAlmoco" name="almoco_imagem" accept="image/*" onchange="uploadImage(this, 'almoco')" hidden>
                                <label for="imageUploadAlmoco" class="btn btn-outline-secondary w-100" aria-label="Fazer upload de imagem para Almoço">
                                    <i class="fas fa-camera"></i>
                                </label>
                            </div>
                        </div>
                    </div>

                    <!-- Jantar -->
                    <div class="mb-3">
                        <label for="jantar" class="form-label">Jantar</label>
                        <div class="row g-2 align-items-center">
                            <div class="col-8">
                                <textarea id="jantar" name="jantar" class="form-control" maxlength="500" required></textarea>
                                <div class="invalid-feedback">Por favor, descreva seu jantar</div>
                            </div>
                            <div class="col-4 d-flex justify-content-center">
                                <input type="file" id="imageUploadJantar" name="jantar_imagem" accept="image/*" onchange="uploadImage(this, 'jantar')" hidden>
                                <label for="imageUploadJantar" class="btn btn-outline-secondary w-100" aria-label="Fazer upload de imagem para Jantar">
                                    <i class="fas fa-camera"></i>
                                </label>
                            </div>
                        </div>
                    </div>

                    <!-- Lanches -->
                    <div class="mb-3">
                        <label for="lanches" class="form-label">Lanches</label>
                        <div class="row g-2 align-items-center">
                            <div class="col-8">
                                <textarea id="lanches" name="lanches" class="form-control" maxlength="500"></textarea>
                            </div>
                            <div class="col-4 d-flex justify-content-center">
                                <input type="file" id="imageUploadLanches" name="lanches_imagem" accept="image/*" onchange="uploadImage(this, 'lanches')" hidden>
                                <label for="imageUploadLanches" class="btn btn-outline-secondary w-100" aria-label="Fazer upload de imagem para Lanches">
                                    <i class="fas fa-camera"></i>
                                </label>
                            </div>
                        </div>
                    </div>

                    <div class="mb-3">
                        <label for="observacoes_alimentacao" class="form-label">Observações</label>
                        <textarea id="observacoes_alimentacao" name="observacoes_alimentacao" class="form-control" maxlength="500"></textarea>
                    </div>
                </div>

                <!-- Líquidos -->
                <div class="form-section col-md-3">
                    <h3>Ingestão de Líquidos</h3>
                    <div class="mb-3">
                        <label for="agua" class="form-label">Água (em litros)</label>
                        <input type="number" id="agua" name="agua" class="form-control" min="0" step="0.1" required>
                        <div class="invalid-feedback">Por favor, informe a quantidade de água consumida</div>
                    </div>
                    <div class="mb-3">
                        <label for="outros_liquidos" class="form-label">Outros Líquidos</label>
                        <input type="text" id="outros_liquidos" name="outros_liquidos" class="form-control" maxlength="100">
                    </div>
                    <div class="mb-3">
                        <label for="observacoes_liquidos" class="form-label">Observações</label>
                        <textarea id="observacoes_liquidos" name="observacoes_liquidos" class="form-control" maxlength="500"></textarea>
                    </div>
                </div>

                <!-- Exercícios -->
                <div class="form-section col-md-3">
                    <h3>Exercícios</h3>
                    <div class="mb-3">
                        <label for="tipo_treino" class="form-label">Tipo de Treino</label>
                        <input type="text" id="tipo_treino" name="tipo_treino" class="form-control" maxlength="100" required>
                        <div class="invalid-feedback">Por favor, informe o tipo de treino realizado</div>
                    </div>
                    <div class="mb-3">
                        <label for="duracao_treino" class="form-label">Duração (minutos)</label>
                        <input type="number" id="duracao_treino" name="duracao_treino" class="form-control" min="1" required>
                        <div class="invalid-feedback">Por favor, informe a duração do treino</div>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Intensidade</label>
                        <div class="btn-group w-100" role="group" aria-label="Intensidade do treino">
                            <input type="radio" class="btn-check" name="intensidade_treino" id="intensidade_treino1" value="BAIXO" autocomplete="off" required>
                            <label class="btn btn-dark" for="intensidade_treino1">BAIXA</label>
                            <input type="radio" class="btn-check" name="intensidade_treino" id="intensidade_treino2" value="MEDIO" autocomplete="off">
                            <label class="btn btn-warning" for="intensidade_treino2">MÉDIA</label>
                            <input type="radio" class="btn-check" name="intensidade_treino" id="intensidade_treino3" value="ALTO" autocomplete="off">
                            <label class="btn btn-danger" for="intensidade_treino3">ALTA</label>
                        </div>
                        <div class="invalid-feedback">Por favor, selecione a intensidade do treino</div>
                    </div>
                    <div class="mb-3">
                        <label for="detalhes_exercicio" class="form-label">Detalhes</label>
                        <textarea id="detalhes_exercicio" name="detalhes_exercicio" class="form-control" maxlength="500"></textarea>
                    </div>
                    <div class="mb-3">
                        <label for="observacoes_exercicio" class="form-label">Observações</label>
                        <textarea id="observacoes_exercicio" name="observacoes_exercicio" class="form-control" maxlength="500"></textarea>
                    </div>
                </div>

                <!-- Avaliação Pessoal -->
                <div class="form-section col-md-3">
                    <h3>Avaliação Pessoal</h3>
                    
                    <!-- Nível de Fome -->
                    <div class="mb-3">
                        <label class="form-label">Nível de Fome (1-5)</label>
                        <div class="btn-group w-100" role="group" aria-label="Nível de fome">
                            <input type="radio" class="btn-check" name="nivel_fome" id="nivel_fome_1" value="1" autocomplete="off" required>
                            <label class="btn btn-dark" for="nivel_fome_1">1</label>
                            <input type="radio" class="btn-check" name="nivel_fome" id="nivel_fome_2" value="2" autocomplete="off">
                            <label class="btn btn-danger" for="nivel_fome_2">2</label>
                            <input type="radio" class="btn-check" name="nivel_fome" id="nivel_fome_3" value="3" autocomplete="off">
                            <label class="btn btn-warning" for="nivel_fome_3">3</label>
                            <input type="radio" class="btn-check" name="nivel_fome" id="nivel_fome_4" value="4" autocomplete="off">
                            <label class="btn btn-info" for="nivel_fome_4">4</label>
                            <input type="radio" class="btn-check" name="nivel_fome" id="nivel_fome_5" value="5" autocomplete="off">
                            <label class="btn btn-success" for="nivel_fome_5">5</label>
                        </div>
                        <div class="invalid-feedback">Por favor, selecione seu nível de fome</div>
                    </div>

                    <!-- Nível de Energia -->
                    <div class="mb-3">
                        <label class="form-label">Nível de Energia (1-5)</label>
                        <div class="btn-group w-100" role="group" aria-label="Nível de energia">
                            <input type="radio" class="btn-check" name="nivel_energia" id="nivel_energia_1" value="1" autocomplete="off" required>
                            <label class="btn btn-dark" for="nivel_energia_1">1</label>
                            <input type="radio" class="btn-check" name="nivel_energia" id="nivel_energia_2" value="2" autocomplete="off">
                            <label class="btn btn-danger" for="nivel_energia_2">2</label>
                            <input type="radio" class="btn-check" name="nivel_energia" id="nivel_energia_3" value="3" autocomplete="off">
                            <label class="btn btn-warning" for="nivel_energia_3">3</label>
                            <input type="radio" class="btn-check" name="nivel_energia" id="nivel_energia_4" value="4" autocomplete="off">
                            <label class="btn btn-info" for="nivel_energia_4">4</label>
                            <input type="radio" class="btn-check" name="nivel_energia" id="nivel_energia_5" value="5" autocomplete="off">
                            <label class="btn btn-success" for="nivel_energia_5">5</label>
                        </div>
                        <div class="invalid-feedback">Por favor, selecione seu nível de energia</div>
                    </div>

                    <!-- Qualidade do Sono -->
                    <div class="mb-3">
                        <label class="form-label">Qualidade do Sono (1-5)</label>
                        <div class="btn-group w-100" role="group" aria-label="Qualidade do sono">
                            <input type="radio" class="btn-check" name="qualidade_sono" id="qualidade_sono_1" value="1" autocomplete="off" required>
                            <label class="btn btn-dark" for="qualidade_sono_1">1</label>
                            <input type="radio" class="btn-check" name="qualidade_sono" id="qualidade_sono_2" value="2" autocomplete="off">
                            <label class="btn btn-danger" for="qualidade_sono_2">2</label>
                            <input type="radio" class="btn-check" name="qualidade_sono" id="qualidade_sono_3" value="3" autocomplete="off">
                            <label class="btn btn-warning" for="qualidade_sono_3">3</label>
                            <input type="radio" class="btn-check" name="qualidade_sono" id="qualidade_sono_4" value="4" autocomplete="off">
                            <label class="btn btn-info" for="qualidade_sono_4">4</label>
                            <input type="radio" class="btn-check" name="qualidade_sono" id="qualidade_sono_5" value="5" autocomplete="off">
                            <label class="btn btn-success" for="qualidade_sono_5">5</label>
                        </div>
                        <div class="invalid-feedback">Por favor, selecione a qualidade do sono</div>
                    </div>

                    <!-- Outras Observações -->
                    <div class="mb-3">
                        <label for="observacoes_avaliacao" class="form-label">Outras Observações</label>
                        <textarea id="observacoes_avaliacao" name="observacoes_avaliacao" class="form-control" maxlength="500"></textarea>
                    </div>
                </div>
            </div>

            <div class="text-end mt-3">
                <button type="submit" class="btn btn-primary">Salvar</button>
            </div>
        </form>
    </div>

    <!-- Modal de Erro -->
    <div class="modal fade" id="modalMensagem" tabindex="-1" aria-labelledby="modalMensagemLabel" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="modalMensagemLabel">Erro</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Fechar"></button>
                </div>
                <div class="modal-body">
                    Por favor, corrija os campos destacados antes de enviar o formulário.
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Fechar</button>
                </div>
            </div>
        </div>
    </div>

    <%@ include file="WEB-INF/jspf/footer.jspf" %>

    <script>
        // Validação do formulário
        const formularioDiario = document.getElementById('formDiario');
        formularioDiario.addEventListener('submit', function(event) {
            let isValid = true;
            
            // Limpar mensagens de erro anteriores
            document.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));
            document.querySelectorAll('.invalid-feedback').forEach(el => el.style.display = 'none');

            // Verificar campos obrigatórios
            const requiredFields = formularioDiario.querySelectorAll('[required]');
            requiredFields.forEach(field => {
                if (!field.value) {
                    if (field.type === 'radio') {
                        // Verificar grupo de rádio
                        const radios = document.querySelectorAll(`input[name="${field.name}"]`);
                        const checked = Array.from(radios).some(radio => radio.checked);
                        
                        if (!checked) {
                            radios.forEach(radio => radio.closest('.btn-group')?.querySelector('.invalid-feedback')?.style.removeProperty('display'));
                            isValid = false;
                        }
                    } else {
                        field.classList.add('is-invalid');
                        field.closest('.mb-3')?.querySelector('.invalid-feedback')?.style.removeProperty('display');
                        isValid = false;
                    }
                }
            });

            if (!isValid) {
                event.preventDefault();
                new bootstrap.Modal(document.getElementById('modalMensagem')).show();
            }
        });

        // Upload de imagens
        function uploadImage(inputElement, textAreaId) {
            const file = inputElement.files[0];
            if (!file) return;

            const formData = new FormData();
            formData.append("image", file);
            
            fetch("ImageAnalysisServlet", {
                method: "POST",
                body: formData
            })
            .then(response => {
                if (!response.ok) throw new Error('Erro na resposta do servidor');
                return response.json();
            })
            .then(data => {
                document.getElementById(textAreaId).value = data.response || "Não foi possível analisar a imagem.";
            })
            .catch(error => {
                console.error('Erro:', error);
                document.getElementById(textAreaId).value = "Erro ao processar a imagem.";
            });
        }
    </script>
</body>
</html>