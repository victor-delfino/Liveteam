<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="jakarta.servlet.http.HttpSession" %>
<%@ page import="java.sql.*" %>
<%@ page import="java.util.Properties" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Perfil do Usuário</title>
    <%@ include file="WEB-INF/jspf/html-head.jspf" %>
    <link rel="stylesheet" href="https://unpkg.com/@phosphor-icons/web@2.0.3/src/regular/style.css" />

    <style>
        /* ------------- SEU CSS COMPLETO RESTAURADO E AJUSTADO ------------- */
        body {
            background: #181c1f;
            color: #f7f7f7;
            font-family: 'Inter', 'Segoe UI', Arial, sans-serif;
        }
        .info-row {
            display: flex;
            align-items: center;
            gap: 0.7em;
            margin: 10px 0;
            font-size: 1.14rem;
        }
        .info-row strong {
            color: #A0D683;
            font-weight: 700;
            min-width: 115px;
            display: flex;
            align-items: center;
            gap: 0.4em;
        }
        .ph {
            font-size: 1.28em;
            vertical-align: middle;
            color: #A0D683;
            transition: color 0.2s;
            background: linear-gradient(90deg, #A0D683 55%, #8b5cf6 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
        }
        h1, h2 {
            color: #A0D683;
            text-align: center;
            margin-bottom: 18px;
        }
        h1 { font-size: 2.2rem; font-weight: bold; }
        h2 { font-size: 1.4rem; }
        @media (max-width: 600px) {
            .info-row { font-size: 1rem; }
            h1 { font-size: 1.4rem; }
        }
        #planoModal .modal-content::-webkit-scrollbar {
            width: 10px;
            background: #181c1f;
        }
        #planoModal .modal-content::-webkit-scrollbar-thumb {
            background: linear-gradient(180deg, #A0D683 70%, #8b5cf6 100%);
            border-radius: 7px;
            border: 2px solid #23272b;
        }
        #planoModal .modal-content::-webkit-scrollbar-thumb:hover {
            background: linear-gradient(180deg, #8b5cf6 0%, #A0D683 100%);
        }
        #planoModal .modal-content {
            scrollbar-width: thin;
            scrollbar-color: #A0D683 #181c1f;
        }
        .modal {
            display: none;
            position: fixed;
            z-index: 1050;
            left: 0;
            top: 0;
            width: 100vw;
            height: 100vh;
            overflow: auto;
            background: rgba(24, 28, 31, 0.93);
            justify-content: center;
            align-items: center;
        }
        .modal-content {
            background: #23272b;
            color: #f7f7f7;
            margin: 18px auto;
            padding: 32px 28px 24px 28px;
            border-radius: 14px;
            border: 1.5px solid #A0D683;
            box-shadow: 0 4px 28px #A0D68333, 0 0 0 1.5px #A0D683;
            max-width: 600px;
            width: 92vw;
            position: relative;
            animation: modalShow 0.22s cubic-bezier(.6,.2,.5,1.2);
            max-height: 90vh;
            overflow-y: auto;
        }
        @keyframes modalShow {
            from { opacity: 0; transform: translateY(-32px) scale(0.98);}
            to  { opacity: 1; transform: translateY(0) scale(1);}
        }
        .modal-content h2 {
            color: #A0D683;
            font-size: 1.6rem;
            font-weight: bold;
            margin-bottom: 1.4rem;
            text-align: center;
            letter-spacing: 0.01em;
            background: linear-gradient(90deg, #A0D683 70%, #8b5cf6 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
        }
        .close {
            color: #A0D683;
            position: absolute;
            top: 18px;
            right: 18px;
            font-size: 2.2rem;
            font-weight: bold;
            cursor: pointer;
            background: none !important;
            border: none !important;
            z-index: 10;
            transition: color 0.18s;
            outline: none !important;
            box-shadow: none !important;
            padding: 0 !important;
            appearance: none !important;
            -webkit-appearance: none !important;
            -moz-appearance: none !important;
        }
        .close:focus,
        .close:active,
        .close:focus-visible,
        .close:hover {
            color: #8b5cf6;
            outline: none !important;
            box-shadow: none !important;
            background: none !important;
        }
        .modal-content label {
            color: #A0D683;
            font-weight: 700;
            margin-bottom: 8px;
            display: flex;
            align-items: center;
            gap: 0.5em;
            font-size: 1rem;
        }
        .modal-content input[type="password"],
        .modal-content input[type="number"] {
            background: #181c1f;
            color: #f7f7f7;
            border: 1.5px solid #A0D683;
            border-radius: 7px;
            font-size: 1.13rem;
            margin-bottom: 18px;
            margin-top: 3px;
            padding: 12px 11px;
            width: 100%;
            transition: border 0.3s, background 0.3s;
            font-family: inherit;
        }
        .modal-content input[type="password"]:focus,
        .modal-content input[type="number"]:focus {
            border-color: #8B5CF6;
            background: #23272b;
            outline: none;
        }
        .modal-content button[type="submit"], .modal-content button.atualizar-btn {
            background: linear-gradient(90deg, #A0D683 0%, #7DD23B 100%);
            font-weight: bold;
            padding: 12px 24px;
            border-radius: 7px;
            border: none;
            box-shadow: 0 2px 8px 0 rgba(160, 214, 131, 0.16);
            font-size: 1.13rem;
            width: 100%;
            margin-top: 8px;
            transition: background 0.3s, color 0.3s, filter 0.2s;
            cursor: pointer;
            color: #23272b; /* Corrigido para preto/escuro */
        }
        .modal-content button[type="submit"]:hover,
        .modal-content button[type="submit"]:focus,
        .modal-content button.atualizar-btn:hover,
        .modal-content button.atualizar-btn:focus {
            background: linear-gradient(90deg, #7DD23B 0%, #A0D683 100%);
            color: #181c1f;
            filter: brightness(0.97);
        }
        /* Estilo para desabilitar o botão durante o processamento */
        .modal-content button.disabled {
            opacity: 0.7;
            cursor: not-allowed;
        }
        #planoModal .modal-content {
            max-width: 600px;
            font-size: 1.08rem;
        }
        /* ESTILO REVERTIDO: O JSON será exibido em <pre> */
        #conteudoPlano pre {
            white-space: pre-wrap;
            font-family: 'Consolas', 'Courier New', monospace;
            font-size: 0.95em;
            color: #D6D6D6;
            background: #1f2327;
            padding: 15px;
            border-radius: 8px;
            margin-top: 1em;
            overflow-x: auto;
        }
        /* --- FIM ESTILOS DE FORMATAÇÃO DE CONTEÚDO --- */
        #modalAtualizarPlano .modal-content {
            max-width: 540px;
            font-size: 1.08rem;
        }
        #modalAtualizarPlano textarea {
            background: #181c1f;
            color: #f7f7f7;
            border: 1.5px solid #A0D683;
            border-radius: 7px;
            font-size: 1.12rem;
            padding: 12px 11px;
            width: 100%;
            min-height: 100px;
            margin-bottom: 16px;
            font-family: inherit;
            transition: border 0.3s, background 0.3s;
        }
        #modalAtualizarPlano textarea:focus {
            border-color: #8B5CF6;
            background: #23272b;
            outline: none;
        }
        #modalAtualizarPlano .resposta-atualizacao {
            margin-top: 18px;
            color: #A0D683;
        }
        @media (max-width: 600px) {
            .modal-content {
                padding: 18px 6vw 18px 6vw;
                min-width: unset;
                max-width: 98vw;
            }
            #planoModal .modal-content, #modalAtualizarPlano .modal-content {
                max-width: 98vw;
            }
        }
        /* ------------- FIM CSS ------------- */
    </style>
</head>
<body>

<%@ include file="WEB-INF/jspf/header.jspf" %>

<%
    HttpSession sessao = request.getSession(false);
    String nomeUsuario = null;
    String emailUsuario = null;
    String idUsuario = null;
    String roleUsuario = null;
    String idade = null;
    String alturaCm = null;
    String pesoKg = null;

    if (sessao != null) {
        nomeUsuario = (String) sessao.getAttribute("usuarioLogado");
        emailUsuario = (String) sessao.getAttribute("usuarioEmail");
        roleUsuario = (String) sessao.getAttribute("usuarioRole");
        idUsuario = (String) sessao.getAttribute("idUsuario");
    }

    if (nomeUsuario == null || emailUsuario == null) {
        response.sendRedirect("login.jsp?error=notLoggedIn");
        return;
    }

    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;

    try {
        Properties props = new Properties();
        props.load(application.getResourceAsStream("/WEB-INF/classes/db.properties"));

        Class.forName(props.getProperty("db.driver"));
        conn = DriverManager.getConnection(
                props.getProperty("db.url"),
                props.getProperty("db.username"),
                props.getProperty("db.password")
        );

        String sql = "SELECT idade, altura_cm, peso_kg FROM usuario WHERE LOWER(email) = LOWER(?)";
        stmt = conn.prepareStatement(sql);
        stmt.setString(1, emailUsuario.trim());
        rs = stmt.executeQuery();

        if (rs.next()) {
            idade = rs.getString("idade");
            alturaCm = rs.getString("altura_cm");

            if (rs.getString("peso_kg") != null) {
                try {
                    double pesoValue = rs.getDouble("peso_kg");
                    pesoKg = String.format("%.2f", pesoValue).replace(',', '.');
                } catch(Exception ignored) {
                    pesoKg = rs.getString("peso_kg");
                }
            }
        }

    } catch (Exception e) {
        e.printStackTrace();

    } finally {
        try { if (rs != null) rs.close(); } catch (Exception ignored) {}
        try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
        try { if (conn != null) conn.close(); } catch (Exception ignored) {}
    }
%>

<main class="inicio-page container my-5">
    <h1>Perfil do Usuário</h1>
    <h2>Bem-vindo, <%= nomeUsuario %>!</h2>

    <div class="info-row">
        <strong><i class="ph ph-envelope"></i> Email:</strong>
        <span><%= emailUsuario %></span>
    </div>

    <div class="info-row">
        <strong><i class="ph ph-user-circle"></i> Função:</strong>
        <span><%= roleUsuario %></span>
    </div>

    <div class="info-row">
        <strong><i class="ph ph-calendar-blank"></i> Idade:</strong>
        <span><%= (idade != null && !idade.isEmpty()) ? idade : "-" %></span>
    </div>

    <div class="info-row">
        <strong><i class="ph ph-arrow-up"></i> Altura (cm):</strong>
        <span><%= (alturaCm != null && !alturaCm.isEmpty()) ? alturaCm : "-" %></span>
    </div>

    <div class="info-row">
        <strong><i class="ph ph-barbell"></i> Peso (kg):</strong>
        <span><%= (pesoKg != null && !pesoKg.isEmpty()) ? pesoKg : "-" %></span>
    </div>

    <button id="redefinirSenhaBtn" class="btn btn-primary" style="margin-top:22px;">
        <i class="ph ph-key"></i> Redefinir Senha
    </button>

    <% if ("administrador".equalsIgnoreCase(roleUsuario)) { %>
        <div class="mt-3">
            <a href="admin.jsp" class="btn btn-warning">
                <i class="ph ph-shield-star"></i> Ir para o Painel de Administração
            </a>
        </div>
    <% } %>

    <div id="senhaModal" class="modal">
        <div class="modal-content">
            <button class="close" id="closeModal" title="Fechar" tabindex="0" aria-label="Fechar">&times;</button>
            <h2>Redefinir Senha</h2>
            <form action="RedefinirSenhaServlet" method="post" autocomplete="off">
                <label for="senhaAtual">Senha Atual:</label>
                <input type="password" id="senhaAtual" name="senhaAtual" required autocomplete="current-password">

                <label for="novaSenha">Nova Senha:</label>
                <input type="password" id="novaSenha" name="novaSenha" required autocomplete="new-password">

                <label for="confirmarSenha">Confirmar Nova Senha:</label>
                <input type="password" id="confirmarSenha" name="confirmarSenha" required autocomplete="new-password">

                <button type="submit" class="btn btn-success">Redefinir Senha</button>
            </form>
        </div>
    </div>

    <% if (request.getAttribute("error") != null) { %>
        <div class="alert alert-danger mt-3" id="alertMensagem">
            <%= request.getAttribute("error") %>
        </div>
    <% } %>

    <% if (request.getAttribute("success") != null) { %>
        <div class="alert alert-success mt-3" id="alertMensagem">
            <%= request.getAttribute("success") %>
        </div>
    <% } %>

    <div style="margin-top:2rem; text-align:center;">
        <button id="openPlanoModal" class="btn btn-info" style="background:#A0D683;color:#23272b;font-weight:bold;">
            <i class="ph ph-list-bullets"></i> Ver Último Plano Gerado (IA)
        </button>
    </div>

    <div id="planoModal" class="modal">
        <div class="modal-content">
            <button class="close" id="closePlanoModal" title="Fechar" tabindex="0" aria-label="Fechar">&times;</button>
            <h2>Último Plano de Dieta/Treino</h2>

            <div id="conteudoPlano"></div>

            <button id="btnAtualizarPlano" class="btn atualizar-btn" style="margin-top:18px; color: #F7F7F7 !important;">
                <i class="ph ph-magic-wand"></i> Atualizar Plano
            </button>
        </div>
    </div>

    <div id="modalAtualizarPlano" class="modal">
        <div class="modal-content">
            <button class="close" id="closeAtualizarPlanoModal" title="Fechar" tabindex="0" aria-label="Fechar">&times;</button>
            <h2>Atualizar Plano Diário</h2>

            <form id="formAtualizarPlano">

                <h3 style="color:#A0D683; font-size:1.1rem;">
                    <i class="ph ph-note-pencil"></i> Informações para Recalibragem
                </h3>

                <label><i class="ph ph-barbell"></i> Peso Anterior (kg)</label>
                <input type="number" id="pesoAnterior" disabled
                        value="<%= (pesoKg != null) ? pesoKg : "" %>">

                <label for="pesoAtual"><i class="ph ph-barbell"></i> Peso Atual (kg)</label>
                <input type="number" id="pesoAtual" name="pesoAtual"
                        value="<%= (pesoKg != null) ? pesoKg : "" %>" required step="0.1">

                <label for="duracaoTreinoAtual"><i class="ph ph-timer"></i> Duração Média do Treino (min)</label>
                <input type="number" id="duracaoTreinoAtual" name="duracaoTreinoAtual" required>

                <label for="comentarioAtualiza"><i class="ph ph-chat-text"></i> Observações Adicionais</label>
                <textarea id="comentarioAtualiza" name="comentarioAtualiza" rows="5"></textarea>

                <button type="submit" class="btn btn-success" style="margin-top:14px; color: #F7F7F7 !important;">
                    <i class="ph ph-magic-wand"></i> Enviar Recalibragem
                </button>
            </form>

            <div id="respostaAtualizacao" class="resposta-atualizacao"></div>

        </div>
    </div>

</main>

<%@ include file="WEB-INF/jspf/footer.jspf" %>


<script>
document.addEventListener("DOMContentLoaded", () => {

    // ------------------ MODAL DE SENHA ------------------
    const senhaModal = document.getElementById("senhaModal");
    const abrirSenha = document.getElementById("redefinirSenhaBtn");
    const fecharSenha = document.getElementById("closeModal");

    abrirSenha.addEventListener("click", () => {
        senhaModal.style.display = "flex";
    });

    fecharSenha.addEventListener("click", () => {
        senhaModal.style.display = "none";
    });

    // ------------------ MODAL VER PLANO ------------------
    const planoModal = document.getElementById("planoModal");
    const abrirPlano = document.getElementById("openPlanoModal");
    const fecharPlano = document.getElementById("closePlanoModal");
    const conteudoPlano = document.getElementById("conteudoPlano");

    abrirPlano.addEventListener("click", async () => {
        planoModal.style.display = "flex";

        conteudoPlano.innerHTML = "<p style='text-align:center;color:#A0D683;'>Carregando...</p>";

        try {
            const resposta = await fetch("UltimoPlanoServlet");
            const data = await resposta.json();
            
            if (data.erro) {
                // Caso o Servlet retorne um erro (ex: não encontrado ou falha interna)
                conteudoPlano.innerHTML = `<p style='color:red; text-align:center;'>Erro: ${data.erro}</p>`;
            } else if (data.plano_completo) {
                // Caso o Servlet retorne o JSON completo
                conteudoPlano.innerHTML = `
                    <p style="font-size:0.9em; color:#ddd; text-align:center; margin-bottom:1em;">
                        Plano gerado em: ${data.data_geracao || 'N/A'}
                    </p>
                    <pre>${JSON.stringify(data.plano_completo, null, 4)}</pre>
                `;
            } else {
                 // Resposta inesperada
                 conteudoPlano.innerHTML = "<p style='color:red; text-align:center;'>Resposta do servidor inválida.</p>";
            }

        } catch (e) {
            console.error("Erro ao carregar dados do plano:", e);
            conteudoPlano.innerHTML = "<p style='color:red; text-align:center;'>Erro ao carregar dados. Verifique o log do console.</p>";
        }
    });

    fecharPlano.addEventListener("click", () => {
        planoModal.style.display = "none";
    });


    // ------------------ MODAL ATUALIZAR PLANO ------------------
    const modalAtualizarPlano = document.getElementById("modalAtualizarPlano");
    const btnAtualizarPlano = document.getElementById("btnAtualizarPlano");
    const fecharAtualizar = document.getElementById("closeAtualizarPlanoModal");

    btnAtualizarPlano.addEventListener("click", () => {
        modalAtualizarPlano.style.display = "flex";
    });

    fecharAtualizar.addEventListener("click", () => {
        modalAtualizarPlano.style.display = "none";
    });


    // fechar modal clicando fora
    window.onclick = function(event) {
        if (event.target === senhaModal) senhaModal.style.display = "none";
        if (event.target === planoModal) planoModal.style.display = "none";
        if (event.target === modalAtualizarPlano) modalAtualizarPlano.style.display = "none";
    };

});
</script>
</body>
</html>