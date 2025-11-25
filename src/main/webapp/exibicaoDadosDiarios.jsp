<%@ page import="java.sql.Connection" %>
<%@ page import="java.sql.DriverManager" %>
<%@ page import="java.sql.PreparedStatement" %>
<%@ page import="java.sql.ResultSet" %>
<%@ page import="java.util.Properties" %>
<%@ page import="java.io.InputStream" %>
<%@ page import="java.sql.SQLException" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <title>Dados do Dia</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/pages/exibir-dados-page.css">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://unpkg.com/@phosphor-icons/web@2.0.3/src/regular/style.css" />
    <%@ include file="WEB-INF/jspf/html-head.jspf" %>

    <style>
        .multi-line-cell {
            /* Permite que o conteúdo dentro da célula se quebre em várias linhas */
            white-space: normal;
            /* Garante o alinhamento superior para listas */
            vertical-align: top;
            /* Estilo da fonte para melhor leitura da lista */
            font-size: 0.95rem; /* Ajuste se necessário */
        }
        /* Ajuste para que a observação de Alimentação e Treino também fique com a quebra */
        .observacoes-cell {
            white-space: pre-wrap; /* Mantém a formatação de espaço e quebras de linha (se vierem do DB) */
            vertical-align: top;
            font-style: italic;
        }
    </style>
    
</head>
<body>

    <%@ include file="WEB-INF/jspf/header.jspf" %>
<%
    // Recuperar parâmetros da data
    String dia = request.getParameter("dia");
    String mes = request.getParameter("mes");
    String ano = request.getParameter("ano");

    // Format the date for display, handle nulls for robustness
    String formattedDate = "Data Inválida";
    try {
        if (dia != null && mes != null && ano != null) {
            LocalDate dateFromParams = LocalDate.of(Integer.parseInt(ano), Integer.parseInt(mes), Integer.parseInt(dia));
            formattedDate = dateFromParams.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
    } catch (Exception e) {
        // Log or handle the parsing error if needed
        System.err.println("Error parsing date parameters: " + e.getMessage());
    }
%>
    
    <div class="container data-container">
        <h1 class="mt-4 data-title"><i class="ph ph-calendar-blank"></i> Dados do Dia (<%= formattedDate %>)</h1>
        <%
            // Verificar se o usuário está logado
            String idUsuarioStr = (String) session.getAttribute("idUsuario");
            Integer idUsuario = (idUsuarioStr != null) ? Integer.parseInt(idUsuarioStr) : null;
            if (request.getSession(false) == null || request.getSession(false).getAttribute("usuarioLogado") == null) {
                response.sendRedirect("login.jsp");
                return;
            }


            // Inicialização de variáveis para armazenar os dados do banco
            String cafeDaManha = "nenhum", almoco = "nenhum", jantar = "nenhum", lanches = "nenhum", observacoesAlimentacao = "nenhum";
            String agua = "nenhum", outrosLiquidos = "nenhum", observacoesLiquidos = "nenhum";
            String tipoTreino = "nenhum", duracaoTreino = "nenhum", intensidadeTreino = "nenhum", detalhesExercicio = "nenhum", observacoesExercicio = "nenhum";
            String nivelFome = "nenhum", nivelEnergia = "nenhum", qualidadeSono = "nenhum", observacoesAvaliacao = "nenhum";

            // Conexão com o banco de dados
            Connection conn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                // Carregar as propriedades de conexão com o banco de dados
                Properties props = new Properties();
                InputStream input = getServletContext().getResourceAsStream("/WEB-INF/classes/db.properties");
                if (input == null) {
                    throw new Exception("Arquivo db.properties não encontrado.");
                }
                props.load(input);

                // Obter dados de conexão
                String url = props.getProperty("db.url");
                String username = props.getProperty("db.username");
                String password = props.getProperty("db.password");
                String driver = props.getProperty("db.driver");

                // Registrar o driver e estabelecer a conexão
                Class.forName(driver);
                conn = DriverManager.getConnection(url, username, password);

                // Preparar a consulta para recuperar os dados do banco de dados
                String sql = "SELECT * FROM dados_diarios WHERE dia = ? AND mes = ? AND ano = ? and id_usuario = ?";
                ps = conn.prepareStatement(sql);
                ps.setInt(1, Integer.parseInt(dia));
                ps.setInt(2, Integer.parseInt(mes));
                ps.setInt(3, Integer.parseInt(ano));
                ps.setInt(4, idUsuario);
                rs = ps.executeQuery();

                // Se dados encontrados, atribui os valores à variáveis
                if (rs.next()) {
                    // *** MODIFICAÇÃO DE DADOS PARA QUEBRA DE LINHA ***
                    // Substitui '-' por '<br>' para forçar a quebra de linha.
                    // Adiciona classe CSS para garantir a exibição correta (white-space: normal).
                    
                    cafeDaManha = rs.getString("cafe_da_manha") != null ? rs.getString("cafe_da_manha").replace("-", "<br>") : "nenhum";
                    almoco = rs.getString("almoco") != null ? rs.getString("almoco").replace("-", "<br>") : "nenhum";
                    jantar = rs.getString("jantar") != null ? rs.getString("jantar").replace("-", "<br>") : "nenhum";
                    lanches = rs.getString("lanches") != null ? rs.getString("lanches").replace("-", "<br>") : "nenhum";
                    // Observações de Alimentação
                    observacoesAlimentacao = rs.getString("observacoes_alimentacao") != null ? rs.getString("observacoes_alimentacao").replace("-", "<br>") : "nenhum";

                    // Líquidos (não é necessário a quebra aqui, pois são valores únicos, mas mantido o tratamento de null)
                    agua = rs.getString("agua") != null ? rs.getString("agua") : "nenhum";
                    outrosLiquidos = rs.getString("outros_liquidos") != null ? rs.getString("outros_liquidos") : "nenhum";
                    observacoesLiquidos = rs.getString("observacoes_liquidos") != null ? rs.getString("observacoes_liquidos") : "nenhum";

                    // Treino
                    tipoTreino = rs.getString("tipo_treino") != null ? rs.getString("tipo_treino").replace("-", "<br>") : "nenhum";
                    duracaoTreino = rs.getString("duracao_treino") != null ? rs.getString("duracao_treino") : "nenhum";
                    intensidadeTreino = rs.getString("intensidade_treino") != null ? rs.getString("intensidade_treino") : "nenhum";
                    detalhesExercicio = rs.getString("detalhes_exercicio") != null ? rs.getString("detalhes_exercicio").replace("-", "<br>") : "nenhum";
                    // Observações de Treino
                    observacoesExercicio = rs.getString("observacoes_exercicio") != null ? rs.getString("observacoes_exercicio").replace("-", "<br>") : "nenhum";

                    // Avaliação Pessoal (não é necessário a quebra aqui, são níveis)
                    nivelFome = rs.getString("nivel_fome") != null ? rs.getString("nivel_fome") : "nenhum";
                    nivelEnergia = rs.getString("nivel_energia") != null ? rs.getString("nivel_energia") : "nenhum";
                    qualidadeSono = rs.getString("qualidade_sono") != null ? rs.getString("qualidade_sono") : "nenhum";
                    observacoesAvaliacao = rs.getString("observacoes_avaliacao") != null ? rs.getString("observacoes_avaliacao") : "nenhum";
                } else {
                    // Caso não encontre dados para o dia
                    out.println("<script type=\"text/javascript\">");
                    out.println("alert('Nenhum dado para o dia selecionado!');");
                    out.println("window.location.href = 'calendario.jsp';");
                    out.println("</script>");
                }
            } catch (SQLException | ClassNotFoundException e) {
                // Erro de banco de dados ou driver
                e.printStackTrace();
                out.println("<script type=\"text/javascript\">");
                out.println("alert('Erro ao buscar dados!');");
                out.println("window.location.href = 'calendario.jsp';");
                out.println("</script>");
            } catch (Exception e) {
                // Erro inesperado
                e.printStackTrace();
                out.println("<script type=\"text/javascript\">");
                out.println("alert('Erro inesperado: " + e.getMessage() + "');");
                out.println("window.location.href = 'calendario.jsp';");
                out.println("</script>");
            } finally {
                // Fechar recursos após o uso
                if (rs != null) try { rs.close(); } catch (SQLException ignored) {}
                if (ps != null) try { ps.close(); } catch (SQLException ignored) {}
                if (conn != null) try { conn.close(); } catch (SQLException ignored) {}
            }
        %>

        <div class="table-section">
            <h2 class="section-title"><i class="ph ph-fork-knife"></i> Alimentação</h2>
            <table class="table table-custom">
                <thead>
                <tr>
                    <th><i class="ph ph-coffee"></i> Café da Manhã</th>
                    <th><i class="ph ph-bowl-food"></i> Almoço</th>
                    <th><i class="ph ph-knife"></i> Jantar</th>
                    <th><i class="ph ph-cake"></i> Lanches</th>
                    <th><i class="ph ph-note"></i> Observações</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td class="multi-line-cell"><%= cafeDaManha %></td>
                    <td class="multi-line-cell"><%= almoco %></td>
                    <td class="multi-line-cell"><%= jantar %></td>
                    <td class="multi-line-cell"><%= lanches %></td>
                    <td class="observacoes-cell"><%= observacoesAlimentacao %></td>
                </tr>
                </tbody>
            </table>
        </div>
        <div class="table-section">
            <h2 class="section-title"><i class="ph ph-drop"></i> Líquidos</h2>
            <table class="table table-custom">
                <thead>
                <tr>
                    <th><i class="ph ph-drop"></i> Água (Litros)</th>
                    <th><i class="ph ph-drop"></i> Outros Líquidos</th>
                    <th><i class="ph ph-note"></i> Observações</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td><%= agua %></td>
                    <td><%= outrosLiquidos %></td>
                    <td><%= observacoesLiquidos %></td>
                </tr>
                </tbody>
            </table>
        </div>
        <div class="table-section">
            <h2 class="section-title"><i class="ph ph-barbell"></i> Treino</h2>
            <table class="table table-custom">
                <thead>
                <tr>
                    <th><i class="ph ph-barbell"></i> Tipo de Treino</th>
                    <th><i class="ph ph-timer"></i> Duração</th>
                    <th><i class="ph ph-fire"></i> Intensidade</th>
                    <th><i class="ph ph-list-bullets"></i> Detalhes</th>
                    <th><i class="ph ph-note"></i> Observações</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td class="multi-line-cell"><%= tipoTreino %></td>
                    <td><%= duracaoTreino %></td>
                    <td><%= intensidadeTreino %></td>
                    <td class="multi-line-cell"><%= detalhesExercicio %></td>
                    <td class="observacoes-cell"><%= observacoesExercicio %></td>
                </tr>
                </tbody>
            </table>
        </div>
        <div class="table-section">
            <h2 class="section-title"><i class="ph ph-user"></i> Avaliação Pessoal</h2>
            <table class="table table-custom">
                <thead>
                <tr>
                    <th><i class="ph ph-bowl-food"></i> Nível de Fome</th>
                    <th><i class="ph ph-battery-charging"></i> Nível de Energia</th>
                    <th><i class="ph ph-moon-stars"></i> Qualidade do Sono</th>
                    <th><i class="ph ph-note"></i> Observações</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td><%= nivelFome %></td>
                    <td><%= nivelEnergia %></td>
                    <td><%= qualidadeSono %></td>
                    <td><%= observacoesAvaliacao %></td>
                </tr>
                </tbody>
            </table>
        </div>
        <div style="display: flex; justify-content: center; margin: 32px 0;">
            <button 
                onclick="window.location.href='/Liveteam/dashboard?dia=<%= dia %>&mes=<%= mes %>&ano=<%= ano %>'"
                style="
                    background: #23272b;
                    color: #f7f7f7;
                    border: none;
                    border-radius: 8px;
                    padding: 12px 32px;
                    font-size: 1.1rem;
                    font-family: 'Open Sans', 'Roboto', Arial, sans-serif;
                    font-weight: bold;
                    box-shadow: 2px 2px 6px #222a;
                    cursor: pointer;
                    transition: background 0.2s, color 0.2s;
                "
                onmouseover="this.style.background='#4caf50'; this.style.color='#181c1f';"
                onmouseout="this.style.background='#23272b'; this.style.color='#f7f7f7';"
            >
                Ir para o Dashboard
            </button>
        </div>
    </div>

    <%@ include file="WEB-INF/jspf/footer.jspf" %>
</body>
</html>