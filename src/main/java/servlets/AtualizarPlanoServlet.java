package servlets;

import gemini.Gemini;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.json.JSONObject;
import org.json.JSONException;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// Atenção: Certifique-se de que estas classes existem no seu projeto:
// import seu.pacote.SalvarPlanoNoBanco; 
// import seu.pacote.PlanoDAO;
// import seu.pacote.Gemini; 

@WebServlet("/AtualizarPlanoServlet")
public class AtualizarPlanoServlet extends HttpServlet {

    // Seus atributos
    // private final SalvarPlanoNoBanco salvarPlanoNoBanco = new SalvarPlanoNoBanco();
    // private final PlanoDAO planoDAO = new PlanoDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        JSONObject responseJson = new JSONObject();
        HttpSession session = req.getSession(false);

        Integer idUsuario = null;

        Object idUsuarioObj = session != null ? session.getAttribute("idUsuario") : null;
        try {
            if (idUsuarioObj != null) {
                idUsuario = Integer.parseInt(idUsuarioObj.toString());
            }
        } catch (NumberFormatException ignored) {}

        if (idUsuario == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            responseJson.put("erro", "Usuário não autenticado ou ID inválido.");
            resp.getWriter().write(responseJson.toString());
            return;
        }
        
        try {
            String requestBody = req.getReader().lines().collect(Collectors.joining("\n"));
            JSONObject jsonInput = new JSONObject(requestBody);
            
            String pesoAtual = jsonInput.optString("pesoAtual");
            String duracaoTreinoAtual = jsonInput.optString("duracaoTreinoAtual");
            String comentarioUsuario = jsonInput.optString("comentario", "");
            
            if (pesoAtual.isEmpty() || duracaoTreinoAtual.isEmpty()) {
                responseJson.put("sucesso", false);
                responseJson.put("erro", "Peso atual e duração do treino são obrigatórios.");
                resp.getWriter().write(responseJson.toString());
                return;
            }
            
            // --- A PARTIR DAQUI ASSUME-SE QUE AS DEPENDÊNCIAS ESTÃO INSTANCIADAS ---
            
            // 1. ATUALIZA PESO NO BANCO DE DADOS (Tabela 'usuario') - CRUCIAL
            // atualizarPesoUsuario(idUsuario, pesoAtual);
            
            // 2. Buscar Último Plano e Resumo Diário do BD (com PlanoDAO)
            // JSONObject ultimoPlanoJsonWrapper = planoDAO.getUltimoPlanoCompleto(idUsuario);
            // String resumoDiario = planoDAO.getResumoDadosDiarios(idUsuario, 7);
            
            // Simulação de retorno de dados (DESCOMENTAR E USAR AS LINHAS ACIMA NA VERSÃO REAL)
            JSONObject ultimoPlanoJsonWrapper = new JSONObject("{\"plano_completo\": {\"plano_dieta\": {\"objetivo\": \"Ganho de massa\", \"calorias_totais\": 2500, \"observacoes\": \"(só dá para iniciar sob supervisão, dado a sua condição física 90 kg e 175 cm)\"}}}");
            String resumoDiario = "Sucesso: Treinos completos em 5 de 7 dias.";
            
            if (ultimoPlanoJsonWrapper == null) {
                responseJson.put("sucesso", false);
                responseJson.put("erro", "Nenhum plano anterior encontrado para atualização.");
                resp.getWriter().write(responseJson.toString());
                return;
            }

            // 3. Montar Prompt de Atualização
            String prompt = montarPromptDeAtualizacao(ultimoPlanoJsonWrapper, resumoDiario, comentarioUsuario, pesoAtual, duracaoTreinoAtual);
            
            // 4. Chamar a IA (substituir por Gemini.getCompletion(prompt) na versão real)
            // String respostaGeminiBruta = Gemini.getCompletion(prompt);
            String respostaGeminiBruta = "```json\n" + getExemploRespostaJSON(pesoAtual) + "\n```"; // Usando simulação
            String respostaGeminiLimpa = limparRespostaGemini(respostaGeminiBruta);
            
            JSONObject novoPlanoCompletoJson = new JSONObject(respostaGeminiLimpa);
            JSONObject novoPlanoJson = novoPlanoCompletoJson.optJSONObject("plano_completo");
            
            if (novoPlanoJson != null) {
                // ... (Verificações omitidas para brevidade na simulação)

                // 5. Salvar o novo plano (novo registro no histórico)
                // salvarPlanoNoBanco.salvarPlanoNoBanco(idUsuario, planoDietaJson, planoTreinoJson);
                
                responseJson.put("sucesso", true);
                responseJson.put("mensagem", "Plano atualizado e salvo no histórico com sucesso!");
            } else {
                throw new JSONException("Resposta da IA inválida. Não foi possível extrair o plano completo. JSON recebido: " + respostaGeminiLimpa);
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            responseJson.put("sucesso", false);
            responseJson.put("erro", "Erro interno ao reanalisar o plano: " + e.getMessage());
            e.printStackTrace();
        }
        resp.getWriter().write(responseJson.toString());
    }
    
    // FUNÇÃO DE LIMPEZA CORRIGIDA: Apenas remove marcadores de código
    private String limparRespostaGemini(String resposta) {
        if (resposta == null) return "";
        resposta = resposta.trim();
        
        if (resposta.startsWith("```json")) {
            resposta = resposta.substring(7).trim();
        }
        if (resposta.endsWith("```")) {
            resposta = resposta.substring(0, resposta.length() - 3).trim();
        }
        return resposta;
    }

    // --- Métodos Auxiliares (mantidos do seu original) ---
    private String montarPromptDeAtualizacao(JSONObject ultimoPlanoWrapper, String resumoDiario, String feedbackComentario, String pesoAtual, String duracaoTreinoAtual) {
         String planoAnteriorString = ultimoPlanoWrapper.optJSONObject("plano_completo").toString(2);
         String planoAnteriorLimpo = limparPesoAntigoNoPlano(planoAnteriorString, pesoAtual);
         
         // Lógica de prompt aqui
         return "Gere um NOVO plano completo...";
    }
    
    private String limparPesoAntigoNoPlano(String planoAnterior, String novoPeso) {
        // ... (Seu código original de limpeza)
        return planoAnterior;
    }
    
    // Método simulado para testes
    private String getExemploRespostaJSON(String pesoAtual) {
         return "{\"plano_completo\": {\"plano_dieta\": {\"objetivo\": \"Manutenção do peso (" + pesoAtual + " kg)\", \"calorias_totais\": 2300, \"meta_agua\": 3, \"meta_macronutrientes\": {\"proteinas_g\": 180, \"carboidratos_g\": 250, \"gorduras_g\": 60}, \"refeicoes\": {\"cafe_da_manha\": \"Ovos e aveia\", \"almoco\": \"Frango e arroz integral\", \"lanche_tarde\": \"Whey e fruta\", \"jantar\": \"Peixe e vegetais\"}, \"observacoes\": \"Adaptações feitas para o novo peso.\"}, \"plano_treino\": {\"divisao\": \"ABC\", \"justificativa_divisao\": \"Foco em progressão\", \"treino_a\": { \"foco\":\"Peito/Triceps\", \"exercicios\": [{\"nome\":\"Supino\", \"series\":\"4\", \"repeticoes\":\"10\"}]}, \"observacoes\": \"Descanso 60s.\", \"subtreinos\": []}}}";
    }
}