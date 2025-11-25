package servlets;

import gemini.Gemini;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.json.JSONObject;
import org.json.JSONException;
import java.io.*;
import java.util.stream.Collectors;

@WebServlet("/ReanalisarDietaServlet")
public class ReanalisarDietaServlet extends HttpServlet {

    private final SalvarPlanoNoBanco salvarPlanoNoBanco = new SalvarPlanoNoBanco();
    private final PlanoDAO planoDAO = new PlanoDAO(); 

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        JSONObject responseJson = new JSONObject();
        HttpSession session = request.getSession(false);

        // 1. Verificação de autenticação e ID
        Object idUsuarioObj = session != null ? session.getAttribute("idUsuario") : null;
        Integer idUsuario = null;
        try {
            if (idUsuarioObj instanceof String) {
                idUsuario = Integer.valueOf((String) idUsuarioObj);
            } else if (idUsuarioObj instanceof Integer) {
                idUsuario = (Integer) idUsuarioObj;
            }
        } catch (NumberFormatException ignored) {}

        if (idUsuario == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            responseJson.put("erro", "Usuário não autenticado ou ID inválido.");
            response.getWriter().write(responseJson.toString());
            return;
        }

        // Recuperar o comentário do usuário
        String comentario = new BufferedReader(request.getReader()).lines().collect(Collectors.joining("\n"));
        JSONObject inputJson = new JSONObject(comentario);
        String feedbackComentario = inputJson.optString("comentario", "");
        

        try {
            // 2. Obter o último plano e resumo diário
            JSONObject ultimoPlano = planoDAO.getUltimoPlanoCompleto(idUsuario);
            String resumoDiario = planoDAO.getResumoDadosDiarios(idUsuario, 7); // Últimos 7 dias

            if (ultimoPlano == null) {
                 responseJson.put("sucesso", false);
                 responseJson.put("erro", "Nenhum plano anterior encontrado para atualização. Crie um novo plano primeiro.");
                 response.getWriter().write(responseJson.toString());
                 return;
            }

            // 3. Montar o prompt de atualização
            String prompt = montarPromptDeAtualizacao(ultimoPlano, resumoDiario, feedbackComentario);
            
            // 4. Chamar a IA
            String respostaGeminiBruta = Gemini.getCompletion(prompt);
            String respostaGeminiLimpa = limparRespostaGemini(respostaGeminiBruta);
            
            JSONObject novoPlanoCompletoJson = new JSONObject(respostaGeminiLimpa);
            JSONObject novoPlanoJson = novoPlanoCompletoJson.optJSONObject("plano_completo");
            
            if (novoPlanoJson != null) {
                JSONObject planoDietaJson = novoPlanoJson.optJSONObject("plano_dieta");
                JSONObject planoTreinoJson = novoPlanoJson.optJSONObject("plano_treino");

                if (planoDietaJson == null || planoTreinoJson == null) {
                    throw new JSONException("Campos 'plano_dieta' ou 'plano_treino' ausentes no novo plano.");
                }

                // 5. Salvar o novo plano (novo registro no histórico)
                salvarPlanoNoBanco.salvarPlanoNoBanco(idUsuario, planoDietaJson, planoTreinoJson);
                
                responseJson.put("sucesso", true);
                responseJson.put("mensagem", "Plano atualizado e salvo no histórico com sucesso.");
            } else {
                throw new JSONException("Seção 'plano_completo' ausente ou inválida na resposta da IA. Resposta bruta: " + respostaGeminiLimpa.substring(0, Math.min(respostaGeminiLimpa.length(), 200)));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            responseJson.put("sucesso", false);
            responseJson.put("erro", "Erro durante a reanálise do plano: " + e.getMessage());
            e.printStackTrace();
        }
        response.getWriter().write(responseJson.toString());
    }

    private String montarPromptDeAtualizacao(JSONObject ultimoPlano, String resumoDiario, String feedbackComentario) {
        String planoAnterior = ultimoPlano.toString(2); // Formatação para IA entender melhor
        
        return "Com base no último plano do usuário e nos dados diários fornecidos, gere um NOVO plano completo (dieta e treino). " +
               "Analise o feedback dos dados diários e o comentário do usuário para fazer ajustes e melhorias.\n\n" +
               "--- Último Plano (para referência de contexto) ---\n" + planoAnterior + "\n\n" +
               "--- Resumo dos Dados Diários ---\n" + resumoDiario + "\n\n" +
               "--- Comentário do Usuário ---\n" + (feedbackComentario.isEmpty() ? "Nenhum comentário adicional." : feedbackComentario) + "\n\n" +
               "Instrução: Gere o NOVO plano usando a seguinte estrutura JSON EXATA. Não use `meta_macronutrientes` dentro de `plano_dieta`\n\n" +
               getJsonSchema();
    }

    private String getJsonSchema() {
        return "{\n" +
               "  \"plano_completo\": {\n" +
               "    \"plano_dieta\": {\n" +
               "      \"objetivo\": \"[string: objetivo principal da dieta]\",\n" +
               "      \"calorias_totais\": \"[int: estimativa de calorias totais em kcal]\",\n" +
               "      \"meta_agua\": \"[int: litros de água por dia]\",\n" +
               "      \"meta_macronutrientes\": {\n" +
               "        \"proteinas_g\": \"[int: Gramas de Proteínas]\",\n" +
               "        \"carboidratos_g\": \"[int: Gramas de Carboidratos]\",\n" +
               "        \"gorduras_g\": \"[int: Gramas de Gorduras]\"\n" +
               "      },\n" +
               "      \"refeicoes\": {\n" +
               "        \"cafe_da_manha\": \"[string: sugestão para o café da manhã]\",\n" +
               "        \"almoco\": \"[string: sugestão para o almoço]\",\n" +
               "        \"lanche_tarde\": \"[string: sugestão para o lanche da tarde]\",\n" +
               "        \"jantar\": \"[string: sugestão para o jantar]\"\n" +
               "      },\n" +
               "      \"observacoes\": \"[string: observações adicionais sobre a dieta]\"\n" +
               "    },\n" +
               "    \"plano_treino\": {\n" +
               "      \"divisao\": \"[string: 'ABC', 'ABAB' ou outra divisão]\",\n" +
               "      \"justificativa_divisao\": \"[string: justificativa para a escolha da divisão]\",\n" +
               "      \"treino_a\": { /* ... estrutura de foco e exercícios ... */ },\n" +
               "      \"treino_b\": { /* ... estrutura de foco e exercícios ... */ },\n" +
               "      \"treino_c\": { /* ... estrutura de foco e exercícios ... */ },\n" +
               "      \"observacoes\": \"[string: observações adicionais sobre o treino, como descanso, aquecimento, etc.]\"\n" +
               "    }\n" +
               "  }\n" +
               "}";
    }

    private String limparRespostaGemini(String resposta) {
        if (resposta == null) return "";
        resposta = resposta.trim();
        resposta = resposta.replace("```json", "").trim();
        resposta = resposta.replace("```", "").trim();
        resposta = resposta.replace("|#]", "").trim();
        // Remove quebras de linha e tabulações para garantir que o JSONObject seja lido
        resposta = resposta.replace("\n", "").replace("\t", "");
        return resposta;
    }
}