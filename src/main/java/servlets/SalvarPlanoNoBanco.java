package servlets;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONObject;

// Esta classe contém a lógica para salvar o plano gerado pela IA no banco de dados.
public class SalvarPlanoNoBanco {
    
    // [KEEPING parseJsonInt HERE]
    private int parseJsonInt(JSONObject json, String key) {
        Object value = json.opt(key);
        if (value == null) return 0;
        
        String s = value.toString().toLowerCase().trim();
        s = s.replaceAll("[^0-9.]", ""); 
        
        try {
            return Integer.parseInt(s); 
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Salva o plano completo (dieta e treino) no banco de dados.
     */
    public void salvarPlanoNoBanco(int idUsuario, JSONObject planoDietaJson, JSONObject planoTreinoJson) throws Exception {
        
        Connection conn = null;
        PreparedStatement psPlano = null, psDieta = null, psMacro = null, psRefeicoes = null;
        PreparedStatement psTreino = null; 
        ResultSet rsKeys = null;

        try {
            // 1. Configurar Conexão (Criando a conexão aqui, como estava antes)
            Properties props = new Properties();
            InputStream input = getClass().getClassLoader().getResourceAsStream("db.properties");
            if (input == null) throw new Exception("Arquivo db.properties não encontrado.");
            props.load(input);

            Class.forName(props.getProperty("db.driver"));
            conn = DriverManager.getConnection(
                props.getProperty("db.url"),
                props.getProperty("db.username"),
                props.getProperty("db.password")
            );
            conn.setAutoCommit(false); // Inicia a transação
            
            // 2. Inserir plano (ID)
            String sqlPlano = "INSERT INTO plano (id_usuario) VALUES (?)";
            psPlano = conn.prepareStatement(sqlPlano, Statement.RETURN_GENERATED_KEYS);
            psPlano.setInt(1, idUsuario); 
            psPlano.executeUpdate();
            rsKeys = psPlano.getGeneratedKeys();
            if (!rsKeys.next()) throw new Exception("Falha ao obter o ID do plano.");
            int planoId = rsKeys.getInt(1);
            rsKeys.close();
            
            // 3. Inserir dieta
            String sqlDieta = "INSERT INTO dieta (plano_id, objetivo, calorias_totais, observacoes, meta_agua) VALUES (?, ?, ?, ?, ?)";
            psDieta = conn.prepareStatement(sqlDieta, Statement.RETURN_GENERATED_KEYS);
            psDieta.setInt(1, planoId);
            psDieta.setString(2, planoDietaJson.optString("objetivo"));
            
            int caloriasTotais = parseJsonInt(planoDietaJson, "calorias_totais");
            psDieta.setInt(3, caloriasTotais); 
            
            psDieta.setString(4, planoDietaJson.optString("observacoes"));

            int metaAgua = parseJsonInt(planoDietaJson, "meta_agua");
            if (metaAgua > 0) {
                 psDieta.setInt(5, metaAgua);
            } else {
                 psDieta.setNull(5, java.sql.Types.INTEGER);
            }

            psDieta.executeUpdate();
            rsKeys = psDieta.getGeneratedKeys();
            if (!rsKeys.next()) throw new Exception("Falha ao obter o ID da dieta.");
            int dietaId = rsKeys.getInt(1);
            rsKeys.close();

            // 4. Inserir macronutrientes 
            JSONObject macro = planoDietaJson.optJSONObject("meta_macronutrientes");
            if (macro != null) {
                String sqlMacro = "INSERT INTO macronutrientes (dieta_id, proteinas, carboidratos, gorduras) VALUES (?, ?, ?, ?)";
                psMacro = conn.prepareStatement(sqlMacro);
                psMacro.setInt(1, dietaId);
                
                psMacro.setInt(2, parseJsonInt(macro, "proteinas_g")); 
                psMacro.setInt(3, parseJsonInt(macro, "carboidratos_g"));
                psMacro.setInt(4, parseJsonInt(macro, "gorduras_g"));
                
                psMacro.executeUpdate();
            }

            // 5. Inserir refeicoes 
            JSONObject refeicoes = planoDietaJson.optJSONObject("refeicoes");
            if (refeicoes != null) {
                String sqlRefeicoes = "INSERT INTO refeicoes (dieta_id, cafe_da_manha, almoco, lanche_tarde, jantar) VALUES (?, ?, ?, ?, ?)";
                psRefeicoes = conn.prepareStatement(sqlRefeicoes);
                psRefeicoes.setInt(1, dietaId);
                psRefeicoes.setString(2, refeicoes.optString("cafe_da_manha"));
                psRefeicoes.setString(3, refeicoes.optString("almoco"));
                psRefeicoes.setString(4, refeicoes.optString("lanche_tarde"));
                psRefeicoes.setString(5, refeicoes.optString("jantar"));
                psRefeicoes.executeUpdate();
            }

            // 6. Inserir treino
            String sqlTreino = "INSERT INTO treino (plano_id, divisao, justificativa_divisao, observacoes) VALUES (?, ?, ?, ?)";
            psTreino = conn.prepareStatement(sqlTreino, Statement.RETURN_GENERATED_KEYS);
            psTreino.setInt(1, planoId);
            psTreino.setString(2, planoTreinoJson.optString("divisao"));
            psTreino.setString(3, planoTreinoJson.optString("justificativa_divisao"));
            psTreino.setString(4, planoTreinoJson.optString("observacoes"));
            psTreino.executeUpdate();
            rsKeys = psTreino.getGeneratedKeys();
            if (!rsKeys.next()) throw new Exception("Falha ao obter o ID do treino.");
            int treinoId = rsKeys.getInt(1);
            rsKeys.close();

            // 7. Salvar subtreinos e exercícios (A, B, C, etc.)
            salvarSubtreinoComExercicios(conn, treinoId, "A", planoTreinoJson.optJSONObject("treino_a"));
            salvarSubtreinoComExercicios(conn, treinoId, "B", planoTreinoJson.optJSONObject("treino_b"));
            salvarSubtreinoComExercicios(conn, treinoId, "C", planoTreinoJson.optJSONObject("treino_c"));

            conn.commit(); // Finaliza a transação com sucesso

        } catch (Exception e) {
            if (conn != null) {
                 System.err.println("Erro na transação. Realizando rollback.");
                 conn.rollback();
            }
            throw e; // Re-lança para ser pego pelo Servlet chamador
        } finally {
            if (rsKeys != null) try { rsKeys.close(); } catch(SQLException e) {}
            if (psPlano != null) try { psPlano.close(); } catch(SQLException e) {}
            if (psDieta != null) try { psDieta.close(); } catch(SQLException e) {}
            if (psMacro != null) try { psMacro.close(); } catch(SQLException e) {}
            if (psRefeicoes != null) try { psRefeicoes.close(); } catch(SQLException e) {}
            if (psTreino != null) try { psTreino.close(); } catch(SQLException e) {}
            if (conn != null) try { conn.close(); } catch(SQLException e) {}
        }
    }

    private void salvarSubtreinoComExercicios(Connection conn, int treinoId, String nomeSubtreino, JSONObject subtreinoJson) throws SQLException {
        if (subtreinoJson == null || subtreinoJson.optString("foco").isEmpty()) return;

        PreparedStatement psSubtreino = null, psExercicio = null;
        ResultSet rsKeys = null;

        try {
            // Inserir Subtreino
            String sqlSubtreino = "INSERT INTO subtreino (treino_id, nome, foco) VALUES (?, ?, ?)";
            psSubtreino = conn.prepareStatement(sqlSubtreino, Statement.RETURN_GENERATED_KEYS);
            psSubtreino.setInt(1, treinoId);
            psSubtreino.setString(2, nomeSubtreino);
            psSubtreino.setString(3, subtreinoJson.optString("foco"));
            psSubtreino.executeUpdate();
            rsKeys = psSubtreino.getGeneratedKeys();
            if (!rsKeys.next()) throw new SQLException("Falha ao obter o ID do subtreino.");
            int subtreinoId = rsKeys.getInt(1);
            rsKeys.close();

            // Inserir Exercícios em Batch
            JSONArray exercicios = subtreinoJson.optJSONArray("exercicios");
            if (exercicios != null) {
                String sqlExercicio = "INSERT INTO exercicio (subtreino_id, nome, series, repeticoes) VALUES (?, ?, ?, ?)";
                psExercicio = conn.prepareStatement(sqlExercicio);
                for (int i = 0; i < exercicios.length(); i++) {
                    JSONObject exercicio = exercicios.getJSONObject(i);
                    psExercicio.setInt(1, subtreinoId);
                    psExercicio.setString(2, exercicio.optString("nome"));
                    psExercicio.setString(3, exercicio.optString("series"));
                    psExercicio.setString(4, exercicio.optString("repeticoes"));
                    psExercicio.addBatch();
                }
                psExercicio.executeBatch();
            }
        } finally {
            if (rsKeys != null) try { rsKeys.close(); } catch(SQLException e) {}
            if (psSubtreino != null) try { psSubtreino.close(); } catch(SQLException e) {}
            if (psExercicio != null) try { psExercicio.close(); } catch(SQLException e) {}
        }
    }
}