<%@ page import="java.time.LocalDate" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Inserir Dados Diários</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/@fortawesome/fontawesome-free/css/all.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://unpkg.com/@phosphor-icons/web@2.0.3/src/regular/style.css" />
    <%@ include file="WEB-INF/jspf/html-head.jspf" %>
    <style>
        body {
            background: #181c1f;
            color: #f7f7f7;
            font-family: 'Inter', 'Segoe UI', Arial, sans-serif;
        }
        .container.form-container {
            background: #181c1f;
            border-radius: 18px;
            box-shadow: 0 0 40px #A0D68333;
            padding: 36px 0 48px 0;
            margin-bottom: 48px;
        }
        h2 {
            color: #A0D683;
            font-size: 2.1rem;
            font-weight: bold;
            text-align: center;
            letter-spacing: 0.01em;
            background: linear-gradient(90deg, #A0D683 70%, #8b5cf6 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
            margin-bottom: 2rem;
        }
        .form-section {
            margin-bottom: 30px;
        }
        .form-section h3 {
            color: #A0D683;
            display: flex;
            align-items: center;
            gap: 0.5rem;
        }
        .form-container form {
            width: 100%;
            max-width: 100%;
            background: #23272b;
            border-radius: 14px;
            padding: 32px 24px 16px 24px;
            box-shadow: 0 2px 16px #A0D68322;
            margin: 0 auto;
        }
        label, .form-label {
            background: transparent;
            color: #A0D683;
            font-weight: 700;
            margin-bottom: 8px;
            margin-top: 6px;
            padding: 0;
            border-radius: 0;
            display: flex;
            align-items: center;
            gap: 0.5em;
            font-size: 1rem;
            box-shadow: none;
        }
        .form-control, .form-select, textarea {
            background: #181c1f;
            color: #f7f7f7;
            border: 1.5px solid #A0D683;
            border-radius: 7px;
            font-size: 1.13rem;
            margin-bottom: 18px;
            margin-top: 3px;
            padding: 12px 11px;
            transition: border 0.3s, background 0.3s;
            font-family: inherit;
        }
        .form-control:focus, .form-select:focus, textarea:focus {
            border-color: #8B5CF6;
            background: #23272b;
            outline: none;
        }
        .form-control.is-invalid, .form-select.is-invalid, textarea.is-invalid {
            border-color: #ff4d4d;
            background: #2a2323;
            color: #ffb3b3;
        }
        textarea.form-control {
            min-height: 58px;
            resize: vertical;
        }
        .btn-group {
            display: flex;
            flex-wrap: wrap;
            gap: 10px;
            justify-content: center;
        }
        .btn {
            background: linear-gradient(90deg, #A0D683 0%, #7DD23B 100%);
            color: #23272b;
            font-weight: bold;
            padding: 12px 24px;
            border-radius: 7px;
            border: none;
            box-shadow: 0 2px 8px 0 rgba(160, 214, 131, 0.12);
            font-size: 1.13rem;
            transition: background 0.3s, color 0.3s, filter 0.2s, box-shadow 0.2s;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 0.5rem;
            outline: none;
        }
        /* CORES INTENSIDADE --- ORIGINAL */
        .btn-group.intensidade-group .btn-dark    { background: linear-gradient(90deg, #424242 60%, #23272b 100%) !important; color: #fff !important; }
        .btn-group.intensidade-group .btn-warning { background: linear-gradient(90deg, #ffe082 60%, #ffb300 100%) !important; color: #333 !important; }
        .btn-group.intensidade-group .btn-danger  { background: linear-gradient(90deg, #ff5252 60%, #d32f2f 100%) !important; color: #fff !important; }
        .btn-group.intensidade-group .btn:focus,
        .btn-group.intensidade-group .btn:hover { filter: brightness(0.95); }
        .btn-group.intensidade-group .btn-check:checked + .btn {
            box-shadow: 0 0 0 3px #8B5CF677, 0 0 14px 3px #7DD23B33;
            filter: brightness(1.08) saturate(1.3);
            border: 2px solid #8B5CF6 !important;
            z-index: 1;
        }
        /* --- FIM CORES INTENSIDADE --- */
        .btn-group.avaliacao-group .btn {
            min-width: 44px;
            max-width: 44px;
            width: 44px;
            justify-content: center;
            padding-left: 0;
            padding-right: 0;
            font-size: 1.08rem;
            font-weight: bold;
            text-align: center;
        }
        .btn-group.avaliacao-group .btn-dark      { background: linear-gradient(90deg, #23272b 60%, #424242 100%) !important; color: #fff !important; }
        .btn-group.avaliacao-group .btn-danger    { background: linear-gradient(90deg, #f44336 60%, #ff8a65 100%) !important; color: #fff !important; }
        .btn-group.avaliacao-group .btn-warning    { background: linear-gradient(90deg, #ffb74d 60%, #fbc02d 100%) !important; color: #23272b !important; }
        .btn-group.avaliacao-group .btn-info      { background: linear-gradient(90deg, #29b6f6 60%, #0288d1 100%) !important; color: #fff !important; }
        .btn-group.avaliacao-group .btn-success    { background: linear-gradient(90deg, #66bb6a 60%, #2e7d32 100%) !important; color: #fff !important; }
        .btn-group.avaliacao-group {
            justify-content: space-between;
            gap: 8px;
        }
        .btn-group.avaliacao-group .btn:focus,
        .btn-group.avaliacao-group .btn:hover { filter: brightness(0.96); }
        .btn-group.avaliacao-group .btn-check:checked + .btn {
            box-shadow: 0 0 0 3px #8B5CF677, 0 0 14px 3px #7DD23B33;
            filter: brightness(1.09) saturate(1.15);
            border: 2px solid #8B5CF6 !important;
            z-index: 1;
        }
        .img-upload-box {
            width: 92px;
            height: 92px;
            border: 1.5px solid #A0D683;
            border-radius: 10px;
            background: #23272b;
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            overflow: hidden;
            position: relative;
            margin-top: 6px;
        }
        .img-upload-box i {
            font-size: 2rem;
            color: #A0D683;
            transition: color 0.3s;
            z-index: 2;
            position: absolute;
            left: 0; top: 0; right: 0; bottom: 0;
            margin: auto;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        .img-upload-box img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            border-radius: 10px;
            display: none;
            background: #23272b;
            z-index: 1;
            position: absolute;
            left: 0; top: 0;
        }
        .img-upload-box.has-img i {
            display: none;
        }
        .img-upload-box.has-img img {
            display: block;
        }
        
        input, textarea, select {
            color: #fff !important;
        }
    </style>
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
    <h2><i class="ph ph-calendar-blank"></i> Inserir Informações do dia <%= diaAtual %>/<%= mesAtual %></h2>
    <form action="salvar-dados" method="post" id="formDiario" novalidate enctype="multipart/form-data">
        <div class="form-section" style="display:none;">
            <div class="row">
                <div class="mb-3 col-md-4">
                    <label for="dia" class="form-label">Dia</label>
                    <input type="number" id="dia" name="dia" class="form-control" value="<%= diaAtual %>" readonly>
                </div>
                <div class="mb-3 col-md-4">
                    <label for="mes" class="form-label">Mês</label>
                    <input type="number" id="mes" name="mes" class="form-control" value="<%= mesAtual %>" readonly>
                </div>
                <div class="mb-3 col-md-4">
                    <label for="ano" class="form-label">Ano</label>
                    <input type="number" id="ano" name="ano" class="form-control" value="<%= anoAtual %>" readonly>
                </div>
            </div>
        </div>
        <div class="row">
            <div class="form-section col-md-3">
                <h3><i class="ph ph-fork-knife"></i> Alimentação</h3>
                <div class="mb-3">
                    <label for="cafe_da_manha" class="form-label">Café da Manhã</label>
                    <div class="row g-2 align-items-center">
                        <div class="col-8">
                            <textarea id="cafe_da_manha" name="cafe_da_manha" class="form-control" maxlength="500" required></textarea>
                            <div class="invalid-feedback">Por favor, descreva seu café da manhã</div>
                        </div>
                        <div class="col-4">
                            <input type="number" id="cafe_da_manha_calorias" name="cafe_da_manha_calorias" class="form-control" placeholder="Calorias">
                        </div>
                        <div class="col-4 d-flex flex-column align-items-center">
                            <div class="img-upload-box" id="cafe_da_manha_imgBox" onclick="triggerInput('imageUploadCafe')">
                                <i class="fas fa-camera"></i>
                                <img id="cafe_da_manha_preview" alt="Prévia Café da Manhã">
                            </div>
                            <input type="file" id="imageUploadCafe" name="cafe_imagem" accept="image/*" onchange="uploadImage(this, 'cafe_da_manha')" hidden>
                            <button type="button" class="btn btn-sm btn-outline-danger mt-1" id="cafe_da_manha_removeBtn" onclick="removeImage('cafe_da_manha')" style="display:none; font-size: 0.75rem; padding: 2px 5px;"><i class="ph ph-trash-simple"></i> Remover</button>
                        </div>
                    </div>
                </div>
                <div class="mb-3">
                    <label for="almoco" class="form-label">Almoço</label>
                    <div class="row g-2 align-items-center">
                        <div class="col-8">
                            <textarea id="almoco" name="almoco" class="form-control" maxlength="500" required></textarea>
                            <div class="invalid-feedback">Por favor, descreva seu almoço</div>
                        </div>
                        <div class="col-4">
                            <input type="number" id="almoco_calorias" name="almoco_calorias" class="form-control" placeholder="Calorias">
                        </div>
                        <div class="col-4 d-flex flex-column align-items-center">
                            <div class="img-upload-box" id="almoco_imgBox" onclick="triggerInput('imageUploadAlmoco')">
                                <i class="fas fa-camera"></i>
                                <img id="almoco_preview" alt="Prévia Almoço">
                            </div>
                            <input type="file" id="imageUploadAlmoco" name="almoco_imagem" accept="image/*" onchange="uploadImage(this, 'almoco')" hidden>
                            <button type="button" class="btn btn-sm btn-outline-danger mt-1" id="almoco_removeBtn" onclick="removeImage('almoco')" style="display:none; font-size: 0.75rem; padding: 2px 5px;"><i class="ph ph-trash-simple"></i> Remover</button>
                        </div>
                    </div>
                </div>
                <div class="mb-3">
                    <label for="jantar" class="form-label">Jantar</label>
                    <div class="row g-2 align-items-center">
                        <div class="col-8">
                            <textarea id="jantar" name="jantar" class="form-control" maxlength="500" required></textarea>
                            <div class="invalid-feedback">Por favor, descreva seu jantar</div>
                        </div>
                        <div class="col-4">
                            <input type="number" id="jantar_calorias" name="jantar_calorias" class="form-control" placeholder="Calorias">
                        </div>
                        <div class="col-4 d-flex flex-column align-items-center">
                            <div class="img-upload-box" id="jantar_imgBox" onclick="triggerInput('imageUploadJantar')">
                                <i class="fas fa-camera"></i>
                                <img id="jantar_preview" alt="Prévia Jantar">
                            </div>
                            <input type="file" id="imageUploadJantar" name="jantar_imagem" accept="image/*" onchange="uploadImage(this, 'jantar')" hidden>
                            <button type="button" class="btn btn-sm btn-outline-danger mt-1" id="jantar_removeBtn" onclick="removeImage('jantar')" style="display:none; font-size: 0.75rem; padding: 2px 5px;"><i class="ph ph-trash-simple"></i> Remover</button>
                        </div>
                    </div>
                </div>
                <div class="mb-3">
                    <label for="lanches" class="form-label">Lanches</label>
                    <div class="row g-2 align-items-center">
                        <div class="col-8">
                            <textarea id="lanches" name="lanches" class="form-control" maxlength="500"></textarea>
                        </div>
                        <div class="col-4">
                            <input type="number" id="lanches_calorias" name="lanches_calorias"  class="form-control" placeholder="Calorias">
                        </div>
                        <div class="col-4 d-flex flex-column align-items-center">
                            <div class="img-upload-box" id="lanches_imgBox" onclick="triggerInput('imageUploadLanches')">
                                <i class="fas fa-camera"></i>
                                <img id="lanches_preview" alt="Prévia Lanches">
                            </div>
                            <input type="file" id="imageUploadLanches" name="lanches_imagem" accept="image/*" onchange="uploadImage(this, 'lanches')" hidden>
                            <button type="button" class="btn btn-sm btn-outline-danger mt-1" id="lanches_removeBtn" onclick="removeImage('lanches')" style="display:none; font-size: 0.75rem; padding: 2px 5px;"><i class="ph ph-trash-simple"></i> Remover</button>
                        </div>
                    </div>
                </div>
                <div class="mb-3">
                    <label for="observacoes_alimentacao" class="form-label">Observações</label>
                    <textarea id="observacoes_alimentacao" name="observacoes_alimentacao" class="form-control" maxlength="500"></textarea>
                </div>
            </div>
            <div class="form-section col-md-3">
                <h3><i class="ph ph-drop"></i> Ingestão de Líquidos</h3>
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
            <div class="form-section col-md-3">
                <h3><i class="ph ph-barbell"></i> Exercícios</h3>
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
                    <div class="btn-group intensidade-group w-100" role="group" aria-label="Intensidade do treino">
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
            <div class="form-section col-md-3">
                <h3><i class="ph ph-user"></i> Avaliação Pessoal</h3>
                <div class="mb-3">
                    <label class="form-label">Nível de Fome (1-5)</label>
                    <div class="btn-group avaliacao-group w-100" role="group" aria-label="Nível de fome">
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
                <div class="mb-3">
                    <label class="form-label">Nível de Energia (1-5)</label>
                    <div class="btn-group avaliacao-group w-100" role="group" aria-label="Nível de energia">
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
                <div class="mb-3">
                    <label class="form-label">Qualidade do Sono (1-5)</label>
                    <div class="btn-group avaliacao-group w-100" role="group" aria-label="Qualidade do sono">
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
                <div class="mb-3">
                    <label for="observacoes_avaliacao" class="form-label">Outras Observações</label>
                    <textarea id="observacoes_avaliacao" name="observacoes_avaliacao" class="form-control" maxlength="500"></textarea>
                </div>
            </div>
        </div>
        <div class="btn-group mt-3">
            <button type="submit" class="btn btn-primary"><i class="ph ph-check-circle"></i> Salvar</button>
        </div>
    </form>
</div>
<div class="modal fade" id="modalMensagem" tabindex="-1" aria-labelledby="modalMensagemLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content" style="background: #23272b; color: #fff;">
            <div class="modal-header" style="border-bottom: 1px solid #A0D683;">
                <h5 class="modal-title" id="modalMensagemLabel" style="color:#ff6b6b;">
                    <i class="ph ph-warning"></i> Atenção
                </h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Fechar"></button>
            </div>
            <div class="modal-body" style="font-size:1.1rem;">
                Por favor, corrija os campos destacados antes de enviar o formulário.
            </div>
            <div class="modal-footer" style="border-top: 1px solid #A0D683;">
                <button type="button" class="btn btn-outline-light" data-bs-dismiss="modal">
                    <i class="ph ph-x"></i> Fechar
                </button>
            </div>
        </div>
    </div>
</div>
<div class="modal fade" id="modalSucesso" tabindex="-1" aria-labelledby="modalSucessoLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content" style="background: #23272b; color: #A0D683;">
            <div class="modal-header">
                <h5 class="modal-title" id="modalSucessoLabel"><i class="ph ph-check-circle"></i> Sucesso</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Fechar"></button>
            </div>
            <div class="modal-body">
                Dados salvos com sucesso!
            </div>
        </div>
    </div>
</div>
<div class="modal fade" id="modalLoading" tabindex="-1" aria-labelledby="modalLoadingLabel" aria-hidden="true" data-bs-backdrop="static" data-bs-keyboard="false">
    <div class="modal-dialog modal-sm modal-dialog-centered">
        <div class="modal-content text-center" style="background: #23272b; color: #A0D683; border-radius: 12px; border: 1px solid #A0D683;">
            <div class="modal-body py-4">
                <div class="spinner-border text-success" role="status" style="width: 3rem; height: 3rem;">
                    <span class="visually-hidden">Processando...</span>
                </div>
                <p class="mt-3 mb-0" id="modalLoadingLabel" style="font-weight: 600;">Analisando Imagem...</p>
            </div>
        </div>
    </div>
</div>

<%@ include file="WEB-INF/jspf/footer.jspf" %>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
    // Inicializa o modal de carregamento uma única vez
    const modalLoading = new bootstrap.Modal(document.getElementById('modalLoading'));

    function triggerInput(inputId) {
        document.getElementById(inputId).click();
    }
    
    // Função para remover a imagem e limpar os campos relacionados
    function removeImage(refId) {
        const inputElementId = 'imageUpload' + refId.charAt(0).toUpperCase() + refId.slice(1);
        const inputElement = document.getElementById(inputElementId);
        const imgBox = document.getElementById(refId + "_imgBox");
        const preview = document.getElementById(refId + "_preview");
        const removeBtn = document.getElementById(refId + "_removeBtn");
        const textarea = document.getElementById(refId);
        const caloriasInput = document.getElementById(refId + "_calorias");

        if (inputElement) {
            inputElement.value = ""; 
        }

        if (imgBox) {
            imgBox.classList.remove("has-img");
        }
        if (preview) {
            preview.src = "";
        }
        if (removeBtn) {
            removeBtn.style.display = 'none';
        }
        if (textarea) textarea.value = "";
        if (caloriasInput) caloriasInput.value = "";

        console.log(`Imagem de ${refId} removida.`);
    }

    // Função para fazer o upload da imagem, mostrar loading e analisar
    function uploadImage(inputElement, refId) {
        const file = inputElement.files[0];
        const imgBox = document.getElementById(refId + "_imgBox");
        const preview = document.getElementById(refId + "_preview");
        const removeBtn = document.getElementById(refId + "_removeBtn");
        
        if (!file) {
            removeImage(refId); 
            return;
        }

        // Exibir preview da imagem
        if (preview && imgBox) {
            const reader = new FileReader();
            reader.onload = function(e) {
                preview.src = e.target.result;
                imgBox.classList.add("has-img");
                if (removeBtn) {
                    removeBtn.style.display = 'inline-block';
                }
            };
            reader.readAsDataURL(file);
        }

        // Mostrar o loading antes de enviar
        modalLoading.show();

        // Envia imagem para o ImageAnalysisServlet via AJAX
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
            // Esconder o loading após o sucesso
            modalLoading.hide();

            const textarea = document.getElementById(refId);
            const caloriasInput = document.getElementById(refId + "_calorias");
            
            if (data.items && Array.isArray(data.items) && data.items.length > 0) {
                const texto = data.items.map(item => {
                    const nome = item.alimento?.trim() || 'Alimento desconhecido';
                    const quantidade = item.quantidade?.trim() || 'Sem Quantidade Definida';
                    return `- ` + nome +' ' + quantidade;
                }).join('\n');
                if (textarea) textarea.value = texto;
                const totalCalorias = data.items.reduce((soma, item) =>
                    soma + (parseInt(item.calorias) || 0), 0);
                if (caloriasInput) caloriasInput.value = totalCalorias;
            } else if (data.response) {
                if (textarea) textarea.value = data.response;
                if (caloriasInput) caloriasInput.value = "";
            } else {
                if (textarea) textarea.value = "Não foi possível analisar a imagem.";
                if (caloriasInput) caloriasInput.value = "";
            }
        })
        .catch(error => {
            // Esconder o loading após o erro
            modalLoading.hide();
            console.error("Erro na análise da imagem:", error);
            // Opcional: mostrar um modal de erro ou alerta para o usuário
        });
    }

    // Validação do formulário (mantida)
    const formularioDiario = document.getElementById('formDiario');
    formularioDiario.addEventListener('submit', function(event) {
        let isValid = true;
        document.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));
        document.querySelectorAll('.invalid-feedback').forEach(el => el.style.display = 'none');
        const requiredFields = formularioDiario.querySelectorAll('[required]');
        requiredFields.forEach(field => {
            if (!field.value) {
                if (field.type === 'radio') {
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
    
function showSuccessModal() {
    const modalSucesso = new bootstrap.Modal(document.getElementById('modalSucesso'), {
        backdrop: 'static',
        keyboard: false
    });
    modalSucesso.show();
    setTimeout(() => {
        modalSucesso.hide();
        window.location.href = "home.jsp";
    }, 2000);
}
</script>
</body>
</html>