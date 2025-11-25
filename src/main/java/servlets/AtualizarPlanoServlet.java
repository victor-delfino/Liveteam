package servlets;

import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.sql.*;
import java.util.Properties;
import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet("/BuscarUltimoPlano")
public class AtualizarPlanoServlet extends HttpServlet {
    
    // Função utilitária para pegar Integer ou null
    private Integer getInteger(ResultSet rs, String columnLabel) throws SQLException {
        int value = rs.getInt(columnLabel);
        return rs.wasNull() ? null : value;
    }
    
    // Função utilitária para pegar String ou String vazia
    private String getString(ResultSet rs, String columnLabel) throws SQLException {
        String value = rs.getString(columnLabel);
        // Garante que null se torne "" (string vazia) para evitar problemas no JSON
        return value != null ? value : "";
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuarioEmail") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String email = (String) session.getAttribute("usuarioEmail");
        JSONObject result = new JSONObject();

        try {
            // Carrega propriedades do banco
            Properties props = new Properties();
            InputStream input = getClass().getClassLoader().getResourceAsStream("db.properties");
            if (input == null) throw new Exception("Arquivo db.properties não encontrado.");
            props.load(input);

            Class.forName(props.getProperty("db.driver"));
            try (Connection conn = DriverManager.getConnection(
                    props.getProperty("db.url"), props.getProperty("db.username"), props.getProperty("db.password"))) {

                // Busca ID do usuário
                int idUsuario = -1;
                try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM usuario WHERE email = ?")) {
                    ps.setString(1, email);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) idUsuario = rs.getInt("id");
                    }
                }
                if (idUsuario == -1) throw new Exception("Usuário não encontrado!");

                // Busca o último plano (maior id)
                int planoId = -1;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT id FROM plano WHERE id_usuario = ? ORDER BY id DESC LIMIT 1")) {
                    ps.setInt(1, idUsuario);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) planoId = rs.getInt("id");
                    }
                }
                if (planoId == -1) {
                    resp.setContentType("application/json");
                    resp.getWriter().write("{\"error\":\"Nenhum preenchimento de plano encontrado.\"}");
                    return;
                }

                // --- 1. Busca dieta e detalhes ---
                JSONObject dietaJson = new JSONObject();
                int dietaId = -1;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT id, objetivo, calorias_totais, observacoes, meta_agua FROM dieta WHERE plano_id = ?")) {
                    ps.setInt(1, planoId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            dietaId = rs.getInt("id");
                            
                            dietaJson.put("objetivo", getString(rs, "objetivo"));
                            dietaJson.put("calorias_totais", getInteger(rs, "calorias_totais"));
                            dietaJson.put("meta_agua", getInteger(rs, "meta_agua"));
                            dietaJson.put("observacoes", getString(rs, "observacoes"));
                        }
                    }
                }
                
                // --- Macronutrientes ---
                if (dietaId != -1) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "SELECT proteinas, carboidratos, gorduras FROM macronutrientes WHERE dieta_id = ?")) {
                        ps.setInt(1, dietaId);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                JSONObject macro = new JSONObject();
                                macro.put("proteinas", getInteger(rs, "proteinas"));
                                macro.put("carboidratos", getInteger(rs, "carboidratos"));
                                macro.put("gorduras", getInteger(rs, "gorduras"));
                                dietaJson.put("macronutrientes", macro);
                            }
                        }
                    }
                    // Refeições
                    try (PreparedStatement ps = conn.prepareStatement(
                            "SELECT cafe_da_manha, almoco, lanche_tarde, jantar FROM refeicoes WHERE dieta_id = ?")) {
                        ps.setInt(1, dietaId);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                JSONObject refeicoes = new JSONObject();
                                refeicoes.put("cafe_da_manha", getString(rs, "cafe_da_manha"));
                                refeicoes.put("almoco", getString(rs, "almoco"));
                                refeicoes.put("lanche_tarde", getString(rs, "lanche_tarde"));
                                refeicoes.put("jantar", getString(rs, "jantar"));
                                dietaJson.put("refeicoes", refeicoes);
                            }
                        }
                    }
                }
                result.put("dieta", dietaJson);

                // --- 2. Busca treino e detalhes ---
                JSONObject treinoJson = new JSONObject();
                int treinoId = -1;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT id, divisao, justificativa_divisao, observacoes FROM treino WHERE plano_id = ?")) {
                    ps.setInt(1, planoId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            treinoId = rs.getInt("id");
                            treinoJson.put("divisao", getString(rs, "divisao"));
                            treinoJson.put("justificativa_divisao", getString(rs, "justificativa_divisao"));
                            treinoJson.put("observacoes", getString(rs, "observacoes"));
                        }
                    }
                }

                // Subtreinos e exercícios
                JSONArray subtreinosArray = new JSONArray();
                if (treinoId != -1) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "SELECT id, nome, foco FROM subtreino WHERE treino_id = ?")) {
                        ps.setInt(1, treinoId);
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                JSONObject subtreino = new JSONObject();
                                int subtreinoId = rs.getInt("id");
                                subtreino.put("nome", getString(rs, "nome"));
                                subtreino.put("foco", getString(rs, "foco"));

                                // Exercícios do subtreino
                                JSONArray exerciciosArray = new JSONArray();
                                try (PreparedStatement psEx = conn.prepareStatement(
                                        "SELECT nome, series, repeticoes FROM exercicio WHERE subtreino_id = ?")) {
                                    psEx.setInt(1, subtreinoId);
                                    try (ResultSet rsEx = psEx.executeQuery()) {
                                        while (rsEx.next()) {
                                            JSONObject ex = new JSONObject();
                                            ex.put("nome", getString(rsEx, "nome"));
                                            ex.put("series", getString(rsEx, "series"));
                                            ex.put("repeticoes", getString(rsEx, "repeticoes"));
                                            exerciciosArray.put(ex);
                                        }
                                    }
                                }
                                subtreino.put("exercicios", exerciciosArray);
                                subtreinosArray.put(subtreino);
                            }
                        }
                    }
                }
                treinoJson.put("subtreinos", subtreinosArray);
                result.put("treino", treinoJson);

                // Retorna JSON
                resp.setContentType("application/json");
                resp.getWriter().write(result.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().write("{\"error\":\"Erro ao buscar dados do plano.\"}");
        }
    }
}