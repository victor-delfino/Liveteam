package servlets;

import java.io.IOException;
import java.sql.*;
import java.util.Properties;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@MultipartConfig
@WebServlet("/salvar-dados")
public class SalvarDadosServlet extends HttpServlet {

    // Lista de colunas de conteúdo para UPDATE/INSERT (excluindo id_usuario, dia, mes, ano)
    private static final String COLUMNS_CONTENT = 
        "cafe_da_manha, cafe_da_manha_calorias, almoco, almoco_calorias, jantar, jantar_calorias, " +
        "lanches, lanches_calorias, observacoes_alimentacao, agua, outros_liquidos, observacoes_liquidos, " +
        "tipo_treino, duracao_treino, intensidade_treino, detalhes_exercicio, observacoes_exercicio, " +
        "nivel_fome, nivel_energia, qualidade_sono, observacoes_avaliacao";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);

        // [Bloco de Verificação de Login e ID do Usuário (mantido)]
        if (session == null || session.getAttribute("usuarioLogado") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        Object idUsuarioObj = session.getAttribute("idUsuario");
        Integer idUsuario = null;

        if (idUsuarioObj instanceof String) {
            try {
                idUsuario = Integer.parseInt((String) idUsuarioObj);
            } catch (NumberFormatException ignored) {}
        } else if (idUsuarioObj instanceof Integer) {
            idUsuario = (Integer) idUsuarioObj;
        }

        if (idUsuario == null) {
            response.sendRedirect("home.jsp?status=error&message=ID%20de%20usu%C3%A1rio%20inv%C3%A1lido.");
            return;
        }

        // [Bloco de Extração e Validação de Data (mantido)]
        String diaStr = request.getParameter("dia");
        String mesStr = request.getParameter("mes");
        String anoStr = request.getParameter("ano");

        if (diaStr == null || mesStr == null || anoStr == null ||
            diaStr.trim().isEmpty() || mesStr.trim().isEmpty() || anoStr.trim().isEmpty()) {
            response.sendRedirect("home.jsp?status=error&message=Parâmetros%20de%20data%20são%20obrigatórios.");
            return;
        }

        int dia, mes, ano;
        try {
            dia = Integer.parseInt(diaStr);
            mes = Integer.parseInt(mesStr);
            ano = Integer.parseInt(anoStr);
        } catch (NumberFormatException e) {
            response.sendRedirect("home.jsp?status=error&message=Parâmetros%20de%20data%20inválidos.");
            return;
        }

        // [Bloco de Extração e Tratamento de Campos do Formulário (mantido)]
        // Extrair demais campos do formulário
        String cafeDaManha = request.getParameter("cafe_da_manha");
        // O parseInt pode lançar NumberFormatException se o campo estiver vazio/nulo. 
        // É recomendado tratar calorias com valor padrão 0 em caso de erro, mas aqui mantemos como no original.
        int cafe_da_manha_calorias = Integer.parseInt(request.getParameter("cafe_da_manha_calorias"));
        String almoco = request.getParameter("almoco");
        int almoco_calorias = Integer.parseInt(request.getParameter("almoco_calorias"));
        String jantar = request.getParameter("jantar");
        int jantar_calorias = Integer.parseInt(request.getParameter("jantar_calorias"));
        String lanches = request.getParameter("lanches");
        int lanches_calorias = Integer.parseInt(request.getParameter("lanches_calorias"));
        String observacoesAlimentacao = request.getParameter("observacoes_alimentacao");
        String agua = request.getParameter("agua");
        String outrosLiquidos = request.getParameter("outros_liquidos");
        String observacoesLiquidos = request.getParameter("observacoes_liquidos");
        String tipoTreino = request.getParameter("tipo_treino");
        String duracaoTreino = request.getParameter("duracao_treino");
        String intensidadeTreino = request.getParameter("intensidade_treino");
        String detalhesExercicio = request.getParameter("detalhes_exercicio");
        String observacoesExercicio = request.getParameter("observacoes_exercicio");
        String nivelFome = request.getParameter("nivel_fome");
        String nivelEnergia = request.getParameter("nivel_energia");
        String qualidadeSono = request.getParameter("qualidade_sono");
        String observacoesAvaliacao = request.getParameter("observacoes_avaliacao");

        // Tratar campos opcionais (mantido)
        observacoesAlimentacao = (observacoesAlimentacao == null || observacoesAlimentacao.trim().isEmpty()) ? null : observacoesAlimentacao;
        observacoesLiquidos = (observacoesLiquidos == null || observacoesLiquidos.trim().isEmpty()) ? null : observacoesLiquidos;
        detalhesExercicio = (detalhesExercicio == null || detalhesExercicio.trim().isEmpty()) ? null : detalhesExercicio;
        observacoesExercicio = (observacoesExercicio == null || observacoesExercicio.trim().isEmpty()) ? null : observacoesExercicio;
        observacoesAvaliacao = (observacoesAvaliacao == null || observacoesAvaliacao.trim().isEmpty()) ? null : observacoesAvaliacao;

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            // [Bloco de Conexão com o Banco de Dados (mantido)]
            Properties props = new Properties();
            props.load(getServletContext().getResourceAsStream("/WEB-INF/classes/db.properties"));

            Class.forName(props.getProperty("db.driver"));
            conn = DriverManager.getConnection(
                    props.getProperty("db.url"),
                    props.getProperty("db.username"),
                    props.getProperty("db.password")
            );

            // 1. Verificar se o registro já existe para o usuário e data
            String checkSql = "SELECT COUNT(*) FROM dados_diarios WHERE id_usuario = ? AND dia = ? AND mes = ? AND ano = ?";
            pstmt = conn.prepareStatement(checkSql);
            pstmt.setInt(1, idUsuario);
            pstmt.setInt(2, dia);
            pstmt.setInt(3, mes);
            pstmt.setInt(4, ano);
            rs = pstmt.executeQuery();

            rs.next();
            int count = rs.getInt(1);
            rs.close(); // Fechar o ResultSet após uso
            pstmt.close(); // Fechar o PreparedStatement após uso

            // Variável para rastrear o sucesso da operação (INSERT ou UPDATE)
            int rowsAffected = 0;

            if (count > 0) {
                // 2. Se o registro EXISTE (count > 0), fazer UPDATE
                String sqlUpdate = "UPDATE dados_diarios SET " +
                    "cafe_da_manha = ?, cafe_da_manha_calorias = ?, almoco = ?, almoco_calorias = ?, " +
                    "jantar = ?, jantar_calorias = ?, lanches = ?, lanches_calorias = ?, observacoes_alimentacao = ?, " +
                    "agua = ?, outros_liquidos = ?, observacoes_liquidos = ?, tipo_treino = ?, duracao_treino = ?, " +
                    "intensidade_treino = ?, detalhes_exercicio = ?, observacoes_exercicio = ?, nivel_fome = ?, " +
                    "nivel_energia = ?, qualidade_sono = ?, observacoes_avaliacao = ? " +
                    "WHERE id_usuario = ? AND dia = ? AND mes = ? AND ano = ?";

                pstmt = conn.prepareStatement(sqlUpdate);

                // Configurar os parâmetros de conteúdo (21 parâmetros)
                setPstmtParameters(pstmt, cafeDaManha, cafe_da_manha_calorias, almoco, almoco_calorias, jantar, jantar_calorias, 
                                   lanches, lanches_calorias, observacoesAlimentacao, agua, outrosLiquidos, observacoesLiquidos, 
                                   tipoTreino, duracaoTreino, intensidadeTreino, detalhesExercicio, observacoesExercicio, 
                                   nivelFome, nivelEnergia, qualidadeSono, observacoesAvaliacao);

                // Configurar os parâmetros WHERE (últimas 4 posições: 22 a 25)
                pstmt.setInt(22, idUsuario);
                pstmt.setInt(23, dia);
                pstmt.setInt(24, mes);
                pstmt.setInt(25, ano);

                rowsAffected = pstmt.executeUpdate();
                
            } else {
                // 3. Se o registro NÃO EXISTE (count = 0), fazer INSERT
                String sqlInsert = "INSERT INTO dados_diarios (id_usuario, dia, mes, ano, " + COLUMNS_CONTENT + 
                    ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"; // 4 + 21 = 25 '?'

                pstmt = conn.prepareStatement(sqlInsert);
                
                // Configurar os parâmetros de ID e Data (1 a 4)
                pstmt.setInt(1, idUsuario);
                pstmt.setInt(2, dia);
                pstmt.setInt(3, mes);
                pstmt.setInt(4, ano);
                
                // Configurar os parâmetros de conteúdo (5 a 25)
                setPstmtParameters(pstmt, cafeDaManha, cafe_da_manha_calorias, almoco, almoco_calorias, jantar, jantar_calorias, 
                                   lanches, lanches_calorias, observacoesAlimentacao, agua, outrosLiquidos, observacoesLiquidos, 
                                   tipoTreino, duracaoTreino, intensidadeTreino, detalhesExercicio, observacoesExercicio, 
                                   nivelFome, nivelEnergia, qualidadeSono, observacoesAvaliacao, 5); // Inicia no índice 5

                rowsAffected = pstmt.executeUpdate();
            }

            if (rowsAffected > 0) {
                // Mensagem de sucesso unificada para INSERT ou UPDATE
                String msg = (count > 0) ? "Dados%20atualizados%20com%20sucesso!" : "Dados%20salvos%20com%20sucesso!";
                response.sendRedirect("home.jsp?status=success&message=" + msg);
            } else {
                response.sendRedirect("home.jsp?status=error&message=Erro%20ao%20salvar%20ou%20atualizar%20dados.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("home.jsp?status=error&message=Erro%20no%20servidor.");
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (pstmt != null) pstmt.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
    }

    /**
     * Método auxiliar para configurar os parâmetros de conteúdo no PreparedStatement.
     */
    private void setPstmtParameters(PreparedStatement pstmt, String cafeDaManha, int cafe_da_manha_calorias, 
                                    String almoco, int almoco_calorias, String jantar, int jantar_calorias, 
                                    String lanches, int lanches_calorias, String observacoesAlimentacao, 
                                    String agua, String outrosLiquidos, String observacoesLiquidos, 
                                    String tipoTreino, String duracaoTreino, String intensidadeTreino, 
                                    String detalhesExercicio, String observacoesExercicio, String nivelFome, 
                                    String nivelEnergia, String qualidadeSono, String observacoesAvaliacao, 
                                    int startIndex) throws SQLException {
        
        pstmt.setString(startIndex++, cafeDaManha);
        pstmt.setInt(startIndex++, cafe_da_manha_calorias);
        pstmt.setString(startIndex++, almoco);
        pstmt.setInt(startIndex++, almoco_calorias);
        pstmt.setString(startIndex++, jantar);
        pstmt.setInt(startIndex++, jantar_calorias);
        pstmt.setString(startIndex++, lanches);
        pstmt.setInt(startIndex++, lanches_calorias);
        pstmt.setString(startIndex++, observacoesAlimentacao);
        pstmt.setString(startIndex++, agua);
        pstmt.setString(startIndex++, outrosLiquidos);
        pstmt.setString(startIndex++, observacoesLiquidos);
        pstmt.setString(startIndex++, tipoTreino);
        pstmt.setString(startIndex++, duracaoTreino);
        pstmt.setString(startIndex++, intensidadeTreino);
        pstmt.setString(startIndex++, detalhesExercicio);
        pstmt.setString(startIndex++, observacoesExercicio);
        pstmt.setString(startIndex++, nivelFome);
        pstmt.setString(startIndex++, nivelEnergia);
        pstmt.setString(startIndex++, qualidadeSono);
        pstmt.setString(startIndex, observacoesAvaliacao);
    }
    
    // Sobrecarga para o caso de UPDATE onde o índice inicial é sempre 1
    private void setPstmtParameters(PreparedStatement pstmt, String cafeDaManha, int cafe_da_manha_calorias, 
                                    String almoco, int almoco_calorias, String jantar, int jantar_calorias, 
                                    String lanches, int lanches_calorias, String observacoesAlimentacao, 
                                    String agua, String outrosLiquidos, String observacoesLiquidos, 
                                    String tipoTreino, String duracaoTreino, String intensidadeTreino, 
                                    String detalhesExercicio, String observacoesExercicio, String nivelFome, 
                                    String nivelEnergia, String qualidadeSono, String observacoesAvaliacao) throws SQLException {
        setPstmtParameters(pstmt, cafeDaManha, cafe_da_manha_calorias, almoco, almoco_calorias, jantar, jantar_calorias, 
                           lanches, lanches_calorias, observacoesAlimentacao, agua, outrosLiquidos, observacoesLiquidos, 
                           tipoTreino, duracaoTreino, intensidadeTreino, detalhesExercicio, observacoesExercicio, 
                           nivelFome, nivelEnergia, qualidadeSono, observacoesAvaliacao, 1);
    }
}