package servlets;

import gemini.Gemini; 
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.ServletException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

@WebServlet("/ExibirDietaServlet")
public class ExibirDietaServlet extends HttpServlet {

    // Assumindo que SalvarPlanoNoBanco.java existe e tem o método salvarPlanoNoBanco(Connection conn, int idUsuario, ...)
    private final SalvarPlanoNoBanco salvarPlanoNoBanco = new SalvarPlanoNoBanco(); 

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        exibirPlanoCompleto(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        exibirPlanoCompleto(request, response);
    }

    // Método auxiliar para buscar uma conexão JDBC
    private Connection getConnection() throws Exception {
        Properties props = new Properties();
        // Usando getResourceAsStream para carregar db.properties
        props.load(getServletContext().getResourceAsStream("/WEB-INF/classes/db.properties")); 

        Class.forName(props.getProperty("db.driver"));
        return DriverManager.getConnection(
            props.getProperty("db.url"),
            props.getProperty("db.username"),
            props.getProperty("db.password")
        );
    }

    /**
     * Gerencia a transação de salvar o plano da IA no banco de dados.
     */
    private boolean salvarDadosPlano(int idUsuario, JSONObject planoDietaJson, JSONObject planoTreinoJson) throws Exception {
        Connection conn = null;
        boolean sucesso = false;
        try {
            conn = getConnection();
            conn.setAutoCommit(false); // Inicia a transação
            
            // CORREÇÃO: CHAMA O MÉTODO DE SALVAMENTO PASSANDO O idUsuario
            salvarPlanoNoBanco.salvarPlanoNoBanco(idUsuario, planoDietaJson, planoTreinoJson);
            
            conn.commit(); // Confirma a transação
            sucesso = true;
            
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Desfaz em caso de erro
                } catch (SQLException rollbackE) {
                    System.err.println("Erro durante o rollback: " + rollbackE.getMessage());
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
        return sucesso;
    }


    private void exibirPlanoCompleto(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int idUsuario = -1; 
        HttpSession session = request.getSession(false);
        String emailUsuario = ""; 
        String nomeUsuario = "";

        // [Bloco de Verificação de Login]
        if (session != null) {
            Object idUsuarioObj = session.getAttribute("idUsuario");
            emailUsuario = (String) session.getAttribute("usuarioEmail");
            nomeUsuario = (String) session.getAttribute("usuarioLogado");

            if (idUsuarioObj != null) {
                try {
                    // O ID está sendo salvo como String no LoginServlet, então precisamos converter.
                    idUsuario = Integer.parseInt(idUsuarioObj.toString());
                } catch (NumberFormatException e) {
                    response.sendRedirect("login.jsp?error=idInvalido");
                    return;
                }
            } else {
                response.sendRedirect("login.jsp?error=naoLogado");
                return;
            }
        } else {
            response.sendRedirect("login.jsp?error=naoLogado");
            return;
        }


        // [Bloco de Recuperação de Dados do Formulário]
        String idade = request.getParameter("idade");
        String sexo = request.getParameter("sexo");
        String alturaCm = request.getParameter("altura_cm");
        String pesoKg = request.getParameter("peso_kg");
        String objetivoPrincipal = request.getParameter("objetivo_principal");
        String frequenciaSemanalTreino = request.getParameter("frequencia_semanal_treino");
        String duracaoMediaTreino = request.getParameter("duracao_media_treino_minutos");
        String tipoAtividadeFisica = request.getParameter("tipo_atividade_fisica");
        String objetivosTreino = request.getParameter("objetivos_treino");
        String nacionalidade = request.getParameter("nacionalidade");
        String residenciaAtual = request.getParameter("residencia_atual");
        String alimentosFavoritos = request.getParameter("alimentos_favoritos");
        String alimentosQueEvita = request.getParameter("alimentos_que_evita");
        String alimentosParaIncluirExcluir = request.getParameter("alimentos_para_incluir_excluir");
        String usaSuplementos = request.getParameter("usa_suplementos");
        String suplementosUsados = request.getParameter("suplementos_usados");
        String tempoPorTreino = request.getParameter("tempo_por_treino_minutos");
        String cardapioDia = request.getParameter("cardapio_dia");


        // 1. ATUALIZA IDADE, ALTURA E PESO NO BANCO (USANDO CONEXÃO SEPARADA PARA EVITAR CONFLITOS DE TRANSAÇÃO)
        if (idade != null && alturaCm != null && pesoKg != null) {
            Connection conn = null;
            PreparedStatement pstmt = null;
            try {
                conn = getConnection();
                String sql = "UPDATE usuario SET idade = ?, altura_cm = ?, peso_kg = ? WHERE id = ?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, Integer.parseInt(idade));
                pstmt.setBigDecimal(2, new java.math.BigDecimal(alturaCm));
                pstmt.setBigDecimal(3, new java.math.BigDecimal(pesoKg));
                pstmt.setInt(4, idUsuario);
                pstmt.executeUpdate();
            } catch (Exception e) {
                System.err.println("Erro ao atualizar dados do usuário no banco: " + e.getMessage());
            } finally {
                try { if (pstmt != null) pstmt.close(); } catch (Exception ignored) {}
                try { if (conn != null) conn.close(); } catch (Exception ignored) {}
            }
        }
        // ----------------------------------------------------------------------------------------------

        // 2. Monta a mensagem a ser enviada para o Gemini (JSON formatado no prompt)
        String mensagem = "Por favor, crie um plano completo de dieta e treino para academia com base nas seguintes informações:\n\n" +
                 "Idade: " + idade + "\n" +
                 "Sexo: " + sexo + "\n" +
                 "Altura (cm): " + alturaCm + "\n" +
                 "Peso (kg): " + pesoKg + "\n" +
                 "Objetivo Principal: " + objetivoPrincipal + "\n" +
                 "Frequência Semanal de Treino: " + frequenciaSemanalTreino + "\n" +
                 "Duração Média do Treino (minutos): " + duracaoMediaTreino + "\n" +
                 "Tipo de Atividade Física: " + tipoAtividadeFisica + "\n" +
                 "Objetivos do Treino: " + objetivosTreino + "\n" +
                 "Nacionalidade: " + nacionalidade + "\n" +
                 "Residência Atual: " + residenciaAtual + "\n" +
                 "Alimentos Favoritos: " + alimentosFavoritos + "\n" +
                 "Alimentos que Evita: " + alimentosQueEvita + "\n" +
                 "Alimentos para Incluir/Excluir: " + alimentosParaIncluirExcluir + "\n" +
                 "Usa Suplementos: " + usaSuplementos + "\n" +
                 "Suplementos Usados: " + suplementosUsados + "\n" +
                 "Tempo por Treino (minutos): " + tempoPorTreino + "\n" +
                 "Cardápio do Dia (sugestão): " + cardapioDia + "\n\n" +
                 "Retorne a resposta em um objeto JSON com a seguinte estrutura EXATA:\n\n" +
                 "{\n" +
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
                 "      \"treino_a\": {\"foco\":\"[string: foco]\", \"exercicios\": [{\"nome\":\"[string: nome]\", \"series\":\"[string: 3]\", \"repeticoes\":\"[string: 8-12]\"}]},\n" +
                 "      \"treino_b\": {\"foco\":\"[string: foco]\", \"exercicios\": [{\"nome\":\"[string: nome]\", \"series\":\"[string: 3]\", \"repeticoes\":\"[string: 8-12]\"}]},\n" +
                 "      \"treino_c\": {\"foco\":\"[string: foco]\", \"exercicios\": [{\"nome\":\"[string: nome]\", \"series\":\"[string: 3]\", \"repeticoes\":\"[string: 8-12]\"}]},\n" +
                 "      \"observacoes\": \"[string: observações adicionais sobre o treino, como descanso, aquecimento, etc.]\"\n" +
                 "    }\n" +
                 "  }\n" +
                 "}";


        String respostaGemini = "";
        JSONObject planoCompletoJson = null;
        JSONObject planoDietaJson = null;
        JSONObject planoTreinoJson = null;
        boolean sucessoSalvamento = false;

        try {
            // 3. Chamar a IA
            // Note: Você deve ter a classe Gemini configurada para funcionar
            respostaGemini = Gemini.getCompletion(mensagem);
            
            // 4. Limpeza da resposta (removendo markdown e quebras de linha)
            respostaGemini = respostaGemini.trim();
            respostaGemini = respostaGemini.replace("```json", "").trim();
            respostaGemini = respostaGemini.replace("```", "").trim();
            respostaGemini = respostaGemini.replace("|#]", "").trim();
            respostaGemini = respostaGemini.replace("\n", "");
            respostaGemini = respostaGemini.replace("\t", "");

            // 5. Tentar parsear o JSON
            JSONObject respostaJson = new JSONObject(respostaGemini);
            planoCompletoJson = respostaJson.optJSONObject("plano_completo");

            if (planoCompletoJson != null) {
                planoDietaJson = planoCompletoJson.optJSONObject("plano_dieta");
                planoTreinoJson = planoCompletoJson.optJSONObject("plano_treino");
                
                // 6. Salvar no banco (Transação)
                if (planoDietaJson != null && planoTreinoJson != null) {
                    sucessoSalvamento = salvarDadosPlano(idUsuario, planoDietaJson, planoTreinoJson);
                } else {
                    System.err.println("Seções de dieta/treino não encontradas dentro de 'plano_completo'.");
                }
            } else {
                System.err.println("Seção 'plano_completo' não encontrada no JSON da IA.");
            }

        } catch (JSONException e) {
            System.err.println("Erro JSON ao processar resposta do Gemini: " + e.getMessage());
            // Atualiza a mensagem de erro para o front-end
            respostaGemini = "Erro ao analisar JSON. Verifique a estrutura da IA. Erro: " + e.getMessage();
        } catch (Exception e) {
            System.err.println("Erro durante a execução ou salvamento do plano: " + e.getMessage());
            respostaGemini = "Erro ao obter resposta do Gemini ou salvar: " + e.getMessage();
        }

        // [Bloco de Geração do HTML de Resposta]
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html>");
        // ... (HTML para exibição do plano omitido por brevidade) ...
        // ... (O código aqui exibe o planoCompletoJson, que é a resposta da IA) ...
        out.println("</html>");
    }

    private void exibirTreino(PrintWriter out, JSONObject treinoJson, String nomeTreino) {
        if (treinoJson != null) {
            out.println("<h3>Treino " + nomeTreino + " (" + treinoJson.optString("foco") + "):</h3>");
            JSONArray exercicios = treinoJson.optJSONArray("exercicios");
            if (exercicios != null) {
                out.println("<ul>");
                for (int i = 0; i < exercicios.length(); i++) {
                    try {
                        JSONObject exercicio = exercicios.getJSONObject(i);
                        out.println("<li><strong>" + exercicio.optString("nome") + ":</strong> " +
                                 exercicio.optString("series") + " séries de " + exercicio.optString("repeticoes") + " repetições</li>");
                    } catch (JSONException e) {
                        out.println("<p>Erro ao exibir exercício no Treino " + nomeTreino + ": " + e.getMessage() + "</p>");
                        e.printStackTrace();
                    }
                }
                out.println("</ul>");
            }
        }
    }
}