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

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);

        // Verifica se o usuário está logado
        if (session == null || session.getAttribute("usuarioLogado") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        // Obter e validar ID do usuário
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

        // Extrair e validar campos de data
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

        // Extrair demais campos do formulário
        String cafeDaManha = request.getParameter("cafe_da_manha");
        int cafe_da_manha_calorias = Integer.parseInt(request.getParameter("cafe_da_manha_calorias"));
        System.out.println("Café da manha calorias " +cafe_da_manha_calorias);
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

        // Tratar campos opcionais
        observacoesAlimentacao = (observacoesAlimentacao == null || observacoesAlimentacao.trim().isEmpty()) ? null : observacoesAlimentacao;
        observacoesLiquidos = (observacoesLiquidos == null || observacoesLiquidos.trim().isEmpty()) ? null : observacoesLiquidos;
        detalhesExercicio = (detalhesExercicio == null || detalhesExercicio.trim().isEmpty()) ? null : detalhesExercicio;
        observacoesExercicio = (observacoesExercicio == null || observacoesExercicio.trim().isEmpty()) ? null : observacoesExercicio;
        observacoesAvaliacao = (observacoesAvaliacao == null || observacoesAvaliacao.trim().isEmpty()) ? null : observacoesAvaliacao;

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            Properties props = new Properties();
            props.load(getServletContext().getResourceAsStream("/WEB-INF/classes/db.properties"));

            Class.forName(props.getProperty("db.driver"));
            conn = DriverManager.getConnection(
                    props.getProperty("db.url"),
                    props.getProperty("db.username"),
                    props.getProperty("db.password")
            );

            // Verificar duplicidade por data
            String checkSql = "SELECT COUNT(*) FROM dados_diarios WHERE id_usuario = ? AND dia = ? AND mes = ? AND ano = ?";
            pstmt = conn.prepareStatement(checkSql);
            pstmt.setInt(1, idUsuario);
            pstmt.setInt(2, dia); // Usando os valores corretos
            pstmt.setInt(3, mes);
            pstmt.setInt(4, ano);
            rs = pstmt.executeQuery();

            rs.next();
            int count = rs.getInt(1);

            if (count > 0) {
                response.sendRedirect("home.jsp?status=warning&message=Já%20existe%20registro%20para%20essa%20data.");
                return;
            }

            // Ajustando o SQL para incluir as colunas de calorias
                String sql = "INSERT INTO dados_diarios (" +
                        "id_usuario, dia, mes, ano, " +
                        "cafe_da_manha, cafe_da_manha_calorias, " +
                        "almoco, almoco_calorias, " +
                        "jantar, jantar_calorias, " +
                        "lanches, lanches_calorias, " +
                        "observacoes_alimentacao, " +
                        "agua, outros_liquidos, observacoes_liquidos, " +
                        "tipo_treino, duracao_treino, intensidade_treino, detalhes_exercicio, observacoes_exercicio, " +
                        "nivel_fome, nivel_energia, qualidade_sono, observacoes_avaliacao" +
                        ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, idUsuario);
                pstmt.setInt(2, dia);
                pstmt.setInt(3, mes);
                pstmt.setInt(4, ano);
                pstmt.setString(5, cafeDaManha);
                pstmt.setInt(6, cafe_da_manha_calorias);
                pstmt.setString(7, almoco);
                pstmt.setInt(8, almoco_calorias);
                pstmt.setString(9, jantar);
                pstmt.setInt(10, jantar_calorias);
                pstmt.setString(11, lanches);
                pstmt.setInt(12, lanches_calorias);
                pstmt.setString(13, observacoesAlimentacao);
                pstmt.setString(14, agua);
                pstmt.setString(15, outrosLiquidos);
                pstmt.setString(16, observacoesLiquidos);
                pstmt.setString(17, tipoTreino);
                pstmt.setString(18, duracaoTreino);
                pstmt.setString(19, intensidadeTreino);
                pstmt.setString(20, detalhesExercicio);
                pstmt.setString(21, observacoesExercicio);
                pstmt.setString(22, nivelFome);
                pstmt.setString(23, nivelEnergia);
                pstmt.setString(24, qualidadeSono);
                pstmt.setString(25, observacoesAvaliacao);

                int rowsInserted = pstmt.executeUpdate();


            if (rowsInserted > 0) {
                response.sendRedirect("home.jsp?status=success&message=Dados%20salvos%20com%20sucesso!");
            } else {
                response.sendRedirect("home.jsp?status=error&message=Erro%20ao%20salvar%20dados.");
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
}