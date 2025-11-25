package servlets;

// Removendo a importação antiga do banco de dados (que provavelmente contém DatabaseConnection)
// import com.liveteam.database.DatabaseConnection; 
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
// Adicionando imports necessários para conexão direta JDBC
import jakarta.servlet.ServletContext;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;
import java.sql.SQLException; // Adicionando importação de SQLException para tratamento mais limpo

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;

@WebServlet("/UltimoPlanoServlet")
public class UltimoPlanoServlet extends HttpServlet {

    // Helper para obter a conexão com o banco, espelhando a lógica do JSP.
    // Usa o ServletContext para carregar o arquivo db.properties
    private Connection getConnection(ServletContext context) throws Exception {
        Properties props = new Properties();
        // Carrega o arquivo de propriedades a partir de /WEB-INF/classes
        try (InputStream input = context.getResourceAsStream("/WEB-INF/classes/db.properties")) {
            if (input == null) {
                // Lança exceção se o arquivo não for encontrado
                throw new Exception("Arquivo db.properties não encontrado. Verifique a pasta WEB-INF/classes.");
            }
            props.load(input);
        }

        // Obtém os dados de conexão
        String url = props.getProperty("db.url");
        String username = props.getProperty("db.username");
        String password = props.getProperty("db.password");
        String driver = props.getProperty("db.driver");

        // Registra o driver e estabelece a conexão
        // Class.forName pode lançar ClassNotFoundException (geralmente por falta do JAR)
        Class.forName(driver); 
        return DriverManager.getConnection(url, username, password);
    }

    // Função para obter o ID do usuário (inclui contingência por e-mail, se necessário)
    private Integer getUserIdFromSession(HttpSession session, ServletContext context) {
        if (session == null) return null;
        Object idObj = session.getAttribute("idUsuario");
        if (idObj != null) {
            try {
                return Integer.valueOf(idObj.toString());
            } catch (NumberFormatException ignored) {}
        }
        
        // Contingência por Email: Agora usa o novo método de conexão
        String emailUsuario = (String) session.getAttribute("usuarioEmail");
        if (emailUsuario != null) {
            try (Connection conn = getConnection(context)) { // Usa a nova conexão
                String sql = "SELECT id FROM usuario WHERE LOWER(email) = LOWER(?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, emailUsuario.trim());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int id = rs.getInt("id");
                        session.setAttribute("idUsuario", String.valueOf(id));
                        return id;
                    }
                }
            } catch (Exception e) {
                // Captura ClassNotFoundException ou SQLException aqui, logando o problema
                System.err.println("PLANO SERVLET ERRO: Falha na Contingência por Email: " + e.getMessage());
            }
        }
        return null;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        JSONObject responseJson = new JSONObject();

        HttpSession session = request.getSession(false);
        // Passa o ServletContext para a função auxiliar
        Integer idUsuario = getUserIdFromSession(session, request.getServletContext());

        if (idUsuario == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            responseJson.put("erro", "Usuário não autenticado ou ID inválido.");
            response.getWriter().write(responseJson.toString());
            return;
        }

        Integer planoId = null;
        
        // Abre a conexão UMA ÚNICA VEZ usando o novo método.
        try (Connection conn = getConnection(request.getServletContext())) {

            // 1. Encontrar o ID do último PLANO (usa o 'conn' aberto)
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT id FROM plano WHERE id_usuario = ? ORDER BY data_criacao DESC LIMIT 1"
            )) {
                stmt.setInt(1, idUsuario);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        planoId = rs.getInt("id");
                    }
                }
            } // PreparedStatement é fechado
            
            // Verifica se o planoId foi encontrado
            if (planoId == null) {
                responseJson.put("erro", "Nenhum plano gerado encontrado para este usuário.");
                response.getWriter().write(responseJson.toString());
                return;
            }

            // 2. Reconstruir o JSON Completo, USANDO O MESMO 'conn'
            JSONObject planoCompleto = new JSONObject();
            
            // --- A) Reconstruir Dieta (inclui Macronutrientes e Refeições)
            JSONObject planoDieta = getPlanoDieta(conn, planoId);
            
            // --- B) Reconstruir Treino (inclui Subtreinos e Exercícios)
            JSONObject planoTreino = getPlanoTreino(conn, planoId);
            
            planoCompleto.put("plano_dieta", planoDieta);
            planoCompleto.put("plano_treino", planoTreino);

            responseJson.put("data_geracao", getPlanoDataCriacao(conn, planoId));
            responseJson.put("plano_completo", planoCompleto);
            
        } catch (Exception e) {
            // Captura qualquer erro de SQL/DB ou de ClassNotFound (driver)
            // É essencial logar a exceção completa para depuração
            System.err.println("Erro irrecuperável durante a operação do plano (Verifique Driver/Properties/Conexão): " + e.getMessage());
            e.printStackTrace(); // ⭐️ IMPORTANTE: Imprime o Stack Trace completo no console do servidor
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            responseJson.put("erro", "Erro interno ao buscar dados do plano. Contate o administrador.");
        }
        
        response.getWriter().write(responseJson.toString());
    }
    
    // --- MÉTODOS AUXILIARES (Inalterados, usam a conexão passada) ---

    private String getPlanoDataCriacao(Connection conn, int planoId) throws Exception {
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT data_criacao FROM plano WHERE id = ?")) {
            stmt.setInt(1, planoId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("data_criacao");
                }
            }
        }
        return "N/A";
    }

    private JSONObject getPlanoDieta(Connection conn, int planoId) throws Exception {
        JSONObject dietaJson = new JSONObject();
        int dietaId = -1;

        // 2.1. Buscar dados de DIETA e obter dieta_id
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT id, objetivo, calorias_totais, observacoes, meta_agua FROM dieta WHERE plano_id = ?")) {
            stmt.setInt(1, planoId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    dietaId = rs.getInt("id");
                    dietaJson.put("objetivo", rs.getString("objetivo"));
                    dietaJson.put("calorias_totais", rs.getInt("calorias_totais"));
                    dietaJson.put("observacoes", rs.getString("observacoes"));
                    dietaJson.put("meta_agua", rs.getInt("meta_agua"));
                }
            }
        }
        
        if (dietaId == -1) return dietaJson;

        // 2.2. Buscar MACRONUTRIENTES
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT proteinas, carboidratos, gorduras FROM macronutrientes WHERE dieta_id = ?")) {
            stmt.setInt(1, dietaId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    JSONObject macro = new JSONObject();
                    macro.put("proteinas_g", rs.getInt("proteinas"));
                    macro.put("carboidratos_g", rs.getInt("carboidratos"));
                    macro.put("gorduras_g", rs.getInt("gorduras"));
                    dietaJson.put("meta_macronutrientes", macro);
                }
            }
        }

        // 2.3. Buscar REFEIÇÕES
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT cafe_da_manha, almoco, lanche_tarde, jantar FROM refeicoes WHERE dieta_id = ?")) {
            stmt.setInt(1, dietaId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    JSONObject refeicoes = new JSONObject();
                    refeicoes.put("cafe_da_manha", rs.getString("cafe_da_manha"));
                    refeicoes.put("almoco", rs.getString("almoco"));
                    refeicoes.put("lanche_tarde", rs.getString("lanche_tarde"));
                    refeicoes.put("jantar", rs.getString("jantar"));
                    dietaJson.put("refeicoes", refeicoes);
                }
            }
        }
        
        return dietaJson;
    }
    
    private JSONObject getPlanoTreino(Connection conn, int planoId) throws Exception {
        JSONObject treinoJson = new JSONObject();
        int treinoId = -1;

        // 3.1. Buscar dados de TREINO e obter treino_id
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT id, divisao, justificativa_divisao, observacoes FROM treino WHERE plano_id = ?")) {
            stmt.setInt(1, planoId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    treinoId = rs.getInt("id");
                    treinoJson.put("divisao", rs.getString("divisao"));
                    treinoJson.put("justificativa_divisao", rs.getString("justificativa_divisao"));
                    treinoJson.put("observacoes", rs.getString("observacoes"));
                }
            }
        }
        
        if (treinoId == -1) return treinoJson;

        // 3.2. Buscar SUBTREINOS (A, B, C...)
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT id, nome, foco FROM subtreino WHERE treino_id = ? ORDER BY nome")) {
            stmt.setInt(1, treinoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int subtreinoId = rs.getInt("id");
                    String nome = rs.getString("nome").toLowerCase();
                    JSONObject subtreino = new JSONObject();
                    subtreino.put("foco", rs.getString("foco"));
                    
                    // 3.3. Buscar EXERCÍCIOS para cada SUBTREINO
                    JSONArray exerciciosArray = new JSONArray();
                    try (PreparedStatement stmtEx = conn.prepareStatement(
                            "SELECT nome, series, repeticoes FROM exercicio WHERE subtreino_id = ?")) {
                        stmtEx.setInt(1, subtreinoId);
                        try (ResultSet rsEx = stmtEx.executeQuery()) {
                            while (rsEx.next()) {
                                JSONObject exercicio = new JSONObject();
                                exercicio.put("nome", rsEx.getString("nome"));
                                exercicio.put("series", rsEx.getString("series"));
                                exercicio.put("repeticoes", rsEx.getString("repeticoes"));
                                exerciciosArray.put(exercicio);
                            }
                        }
                    }
                    subtreino.put("exercicios", exerciciosArray);
                    
                    // Adiciona o subtreino ao JSON principal com a chave dinâmica (treino_a, treino_b...)
                    treinoJson.put("treino_" + nome, subtreino);
                }
            }
        }
        
        return treinoJson;
    }
}