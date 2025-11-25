package servlets;

import java.sql.*;
import java.io.InputStream;
import java.util.Properties;
import org.json.JSONObject;
import org.json.JSONArray;

public class PlanoDAO {

    private Connection getConnection() throws Exception {
        Properties props = new Properties();
        InputStream input = getClass().getClassLoader().getResourceAsStream("db.properties");
        if (input == null) throw new Exception("Arquivo db.properties não encontrado.");
        props.load(input);

        Class.forName(props.getProperty("db.driver"));
        return DriverManager.getConnection(
            props.getProperty("db.url"), 
            props.getProperty("db.username"), 
            props.getProperty("db.password")
        );
    }

    /**
     * Busca o último plano completo (Dieta e Treino) de um usuário e o retorna no formato JSON.
     */
    public JSONObject getUltimoPlanoCompleto(int idUsuario) throws Exception {
        
        // 1. Encontra o ID do plano mais recente
        String sqlPlano = "SELECT id FROM plano WHERE id_usuario = ? ORDER BY data_criacao DESC LIMIT 1";
        Integer planoId = null;

        try (Connection conn = getConnection();
             PreparedStatement psPlano = conn.prepareStatement(sqlPlano)) {
            
            psPlano.setInt(1, idUsuario);
            try (ResultSet rs = psPlano.executeQuery()) {
                if (rs.next()) {
                    planoId = rs.getInt("id");
                }
            }
        }
        
        if (planoId == null) return null;

        // 2. Busca os dados de Dieta e Treino baseados no planoId
        JSONObject planoCompleto = new JSONObject();
        
        try (Connection conn = getConnection()) {
            JSONObject planoDieta = getDietaJson(conn, planoId);
            JSONObject planoTreino = getTreinoJson(conn, planoId);
            
            planoCompleto.put("plano_completo", new JSONObject()
                .put("plano_dieta", planoDieta)
                .put("plano_treino", planoTreino)
            );
            return planoCompleto;
        }
    }

    private JSONObject getDietaJson(Connection conn, int planoId) throws SQLException {
        String sqlDieta = "SELECT id, objetivo, calorias_totais, observacoes, meta_agua FROM dieta WHERE plano_id = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sqlDieta)) {
            ps.setInt(1, planoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int dietaId = rs.getInt("id");
                    JSONObject dieta = new JSONObject()
                        .put("objetivo", rs.getString("objetivo"))
                        .put("calorias_totais", rs.getInt("calorias_totais"))
                        .put("observacoes", rs.getString("observacoes") != null ? rs.getString("observacoes") : "")
                        .put("meta_agua", rs.getInt("meta_agua"));
                    
                    dieta.put("meta_macronutrientes", getMacronutrientesJson(conn, dietaId));
                    dieta.put("refeicoes", getRefeicoesJson(conn, dietaId));
                    return dieta;
                }
            }
        }
        return null;
    }
    
    private JSONObject getMacronutrientesJson(Connection conn, int dietaId) throws SQLException {
        String sqlMacro = "SELECT proteinas, carboidratos, gorduras FROM macronutrientes WHERE dieta_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sqlMacro)) {
            ps.setInt(1, dietaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new JSONObject()
                        .put("proteinas_g", rs.getInt("proteinas"))
                        .put("carboidratos_g", rs.getInt("carboidratos"))
                        .put("gorduras_g", rs.getInt("gorduras"));
                }
            }
        }
        return new JSONObject();
    }
    
    private JSONObject getRefeicoesJson(Connection conn, int dietaId) throws SQLException {
        String sqlRefeicoes = "SELECT cafe_da_manha, almoco, lanche_tarde, jantar FROM refeicoes WHERE dieta_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sqlRefeicoes)) {
            ps.setInt(1, dietaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new JSONObject()
                        .put("cafe_da_manha", rs.getString("cafe_da_manha"))
                        .put("almoco", rs.getString("almoco"))
                        .put("lanche_tarde", rs.getString("lanche_tarde"))
                        .put("jantar", rs.getString("jantar"));
                }
            }
        }
        return new JSONObject();
    }

    private JSONObject getTreinoJson(Connection conn, int planoId) throws SQLException {
        String sqlTreino = "SELECT id, divisao, justificativa_divisao, observacoes FROM treino WHERE plano_id = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sqlTreino)) {
            ps.setInt(1, planoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int treinoId = rs.getInt("id");
                    JSONObject treino = new JSONObject()
                        .put("divisao", rs.getString("divisao"))
                        .put("justificativa_divisao", rs.getString("justificativa_divisao"))
                        .put("observacoes", rs.getString("observacoes") != null ? rs.getString("observacoes") : "");
                    
                    treino.put("treino_a", getSubtreinoJson(conn, treinoId, "A"));
                    treino.put("treino_b", getSubtreinoJson(conn, treinoId, "B"));
                    treino.put("treino_c", getSubtreinoJson(conn, treinoId, "C"));
                    return treino;
                }
            }
        }
        return null;
    }

    private JSONObject getSubtreinoJson(Connection conn, int treinoId, String nome) throws SQLException {
        String sqlSubtreino = "SELECT id, foco FROM subtreino WHERE treino_id = ? AND nome = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sqlSubtreino)) {
            ps.setInt(1, treinoId);
            ps.setString(2, nome);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int subtreinoId = rs.getInt("id");
                    JSONObject subtreino = new JSONObject()
                        .put("foco", rs.getString("foco"));
                    
                    subtreino.put("exercicios", getExerciciosJson(conn, subtreinoId));
                    return subtreino;
                }
            }
        }
        return null;
    }
    
    private JSONArray getExerciciosJson(Connection conn, int subtreinoId) throws SQLException {
        String sqlExercicio = "SELECT nome, series, repeticoes FROM exercicio WHERE subtreino_id = ?";
        JSONArray exerciciosArray = new JSONArray();
        
        try (PreparedStatement ps = conn.prepareStatement(sqlExercicio)) {
            ps.setInt(1, subtreinoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    JSONObject exercicio = new JSONObject()
                        .put("nome", rs.getString("nome"))
                        .put("series", rs.getString("series"))
                        .put("repeticoes", rs.getString("repeticoes"));
                    exerciciosArray.put(exercicio);
                }
            }
        }
        return exerciciosArray;
    }

    /**
     * Gera um resumo dos dados diários do usuário.
     */
    public String getResumoDadosDiarios(int idUsuario, int dias) throws Exception {
        
        String sql = "SELECT " +
            "AVG(cafe_da_manha_calorias + almoco_calorias + jantar_calorias + lanches_calorias) AS avg_calorias, " +
            "AVG(CAST(agua AS NUMERIC)) AS avg_agua, " +
            "AVG(CAST(nivel_energia AS NUMERIC)) AS avg_energia, " +
            "AVG(CAST(qualidade_sono AS NUMERIC)) AS avg_sono, " +
            "COUNT(CASE WHEN tipo_treino IS NOT NULL AND duracao_treino IS NOT NULL THEN 1 END) AS dias_treino " +
            "FROM dados_diarios " +
            "WHERE id_usuario = ? " +
            "ORDER BY ano DESC, mes DESC, dia DESC LIMIT ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            ps.setInt(2, dias); 

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double avgCalorias = rs.getDouble("avg_calorias");
                    double avgAgua = rs.getDouble("avg_agua");
                    double avgEnergia = rs.getDouble("avg_energia");
                    double avgSono = rs.getDouble("avg_sono");
                    int diasTreino = rs.getInt("dias_treino");
                    
                    if (rs.wasNull()) return "Nenhum dado diário encontrado nos últimos " + dias + " dias. Não é possível reanalisar o plano. Gere um novo plano se necessário.";

                    return String.format(
                        "Resumo dos Últimos %d Dias:\n" +
                        "- Consumo calórico médio diário: %.0f kcal\n" +
                        "- Ingestão média de água: %.1f litros\n" +
                        "- Nível de Energia médio (1-5): %.1f\n" +
                        "- Qualidade do Sono média (1-5): %.1f\n" +
                        "- Frequência de treino registrada: %d dias.",
                        dias, avgCalorias, avgAgua, avgEnergia, avgSono, diasTreino
                    );
                }
            }
        } catch (SQLException e) {
             System.err.println("Erro SQL ao buscar dados diários: " + e.getMessage());
        }
        return "Nenhum dado diário recente disponível para análise.";
    }
}