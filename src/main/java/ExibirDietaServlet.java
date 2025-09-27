import gemini.Gemini;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Properties;
import servlets.SalvarPlanoNoBanco;

@WebServlet("/ExibirDietaServlet")
public class ExibirDietaServlet extends HttpServlet {

    private final SalvarPlanoNoBanco salvarPlanoNoBanco = new SalvarPlanoNoBanco();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        exibirPlanoCompleto(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        exibirPlanoCompleto(request, response);
    }

    private void exibirPlanoCompleto(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int idUsuario = -1; // valor padrão inválido
        HttpSession session = request.getSession(false);

        if (session != null) {
            String idUsuarioStr = (String) session.getAttribute("idUsuario");
            if (idUsuarioStr != null) {
                try {
                    idUsuario = Integer.parseInt(idUsuarioStr);
                    System.out.println("ID do usuário logado: " + idUsuario);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
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

        // Recupera os dados enviados pelo formulário
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
        String cafeDaManha = request.getParameter("cafe_da_manha");
        String almoco = request.getParameter("almoco");
        String jantar = request.getParameter("jantar");

        // ---------- ATUALIZA IDADE, ALTURA E PESO NO BANCO SEMPRE QUE O FORMULÁRIO FOR ENVIADO ----------
        // (Apenas estes três campos, sem afetar outros dados do usuário)
        if (idade != null && alturaCm != null && pesoKg != null) {
            Connection conn = null;
            PreparedStatement pstmt = null;
            try {
                Properties props = new Properties();
                props.load(getServletContext().getResourceAsStream("/WEB-INF/classes/db.properties"));

                Class.forName(props.getProperty("db.driver"));
                conn = DriverManager.getConnection(
                    props.getProperty("db.url"),
                    props.getProperty("db.username"),
                    props.getProperty("db.password")
                );
                String sql = "UPDATE usuario SET idade = ?, altura_cm = ?, peso_kg = ? WHERE id = ?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, Integer.parseInt(idade));
                pstmt.setBigDecimal(2, new java.math.BigDecimal(alturaCm));
                pstmt.setBigDecimal(3, new java.math.BigDecimal(pesoKg));
                pstmt.setInt(4, idUsuario);
                pstmt.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace(); // log ou trate conforme desejar
            } finally {
                try { if (pstmt != null) pstmt.close(); } catch (Exception ignored) {}
                try { if (conn != null) conn.close(); } catch (Exception ignored) {}
            }
        }
        // ----------------------------------------------------------------------------------------------

        // Monta a mensagem a ser enviada para o Gemini com especificação detalhada do JSON
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
                "Café da Manhã (sugestão): " + cafeDaManha + "\n" +
                "Almoço (sugestão): " + almoco + "\n" +
                "Jantar (sugestão): " + jantar + "\n\n" +
                "Retorne a resposta em um objeto JSON com a seguinte estrutura EXATA:\n\n" +
                "{\n" +
                "  \"plano_completo\": {\n" +
                "    \"plano_dieta\": {\n" +
                "      \"objetivo\": \"[string: objetivo principal da dieta]\",\n" +
                "      \"calorias_totais\": \"[int: estimativa de calorias totais]\",\n" +
                "       \"meta_agua\": \"[int: litros ]\"\n" +
                "      \"macronutrientes\": {\n" +
                "        \"proteinas\": \"[int: Gramas d]\",\n" +
                "        \"carboidratos\": \"[int: Gramas ]\",\n" +
                "        \"gorduras\": \"[int: Gramas ]\"\n" +
                "        \"meta_agua\": \"[int: litros ]\"\n" +
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
                "      \"treino_a\": {\n" +
                "        \"foco\": \"[string: grupo muscular principal do treino A]\",\n" +
                "        \"exercicios\": [\n" +
                "          {\n" +
                "            \"nome\": \"[string: nome do exercício]\",\n" +
                "            \"series\": \"[string: número de séries (ex: '3') ]\",\n" +
                "            \"repeticoes\": \"[string: número de repetições (ex: '8-12')]\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"nome\": \"[string: nome do exercício]\",\n" +
                "            \"series\": \"[string: número de séries (ex: '3')]\",\n" +
                "            \"repeticoes\": \"[string: número de repetições (ex: '10-15')]\"\n" +
                "          }\n" +
                "          // ... mais exercícios seguindo a mesma estrutura ...\n" +
                "        ]\n" +
                "      },\n" +
                "      \"treino_b\": {\n" +
                "        \"foco\": \"[string: grupo muscular principal do treino B]\",\n" +
                "        \"exercicios\": [\n" +
                "          {\n" +
                "            \"nome\": \"[string: nome do exercício]\",\n" +
                "            \"series\": \"[string: número de séries (ex: '3')]\",\n" +
                "            \"repeticoes\": \"[string: número de repetições (ex: '8-12')]\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"nome\": \"[string: nome do exercício]\",\n" +
                "            \"series\": \"[string: número de séries (ex: '3')]\",\n" +
                "            \"repeticoes\": \"[string: número de repetições (ex: '10-15')]\"\n" +
                "          }\n" +
                "          // ... mais exercícios seguindo a mesma estrutura ...\n" +
                "        ]\n" +
                "      },\n" +
                "      \"treino_c\": {\n" +
                "        \"foco\": \"[string: grupo muscular principal do treino C]\",\n" +
                "        \"exercicios\": [\n" +
                "          {\n" +
                "            \"nome\": \"[string: nome do exercício]\",\n" +
                "            \"series\": \"[string: número de séries (ex: '3')]\",\n" +
                "            \"repeticoes\": \"[string: número de repetições (ex: '8-12')]\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"nome\": \"[string: nome do exercício]\",\n" +
                "            \"series\": \"[string: número de séries (ex: '3')]\",\n" +
                "            \"repeticoes\": \"[string: número de repetições (ex: '10-15')]\"\n" +
                "          }\n" +
                "          // ... mais exercícios seguindo a mesma estrutura (apenas se divisão ABC) ...\n" +
                "        ]\n" +
                "      },\n" +
                "      \"observacoes\": \"[string: observações adicionais sobre o treino, como descanso, aquecimento, etc.]\"\n" +
                "    }\n" +
                "  }\n" +
                "}";

        String respostaGemini = "";
        JSONObject respostaJson = null;
        JSONObject planoCompletoJson = null;
        JSONObject planoDietaJson = null;
        JSONObject planoTreinoJson = null;

        try {
            respostaGemini = Gemini.getCompletion(mensagem);
            System.out.println("Resposta Bruta do Gemini (no Servlet - Original): " + respostaGemini);

            // Limpeza da resposta
            respostaGemini = respostaGemini.trim();
            respostaGemini = respostaGemini.replace("```json", "").trim();
            respostaGemini = respostaGemini.replace("```", "").trim();
            respostaGemini = respostaGemini.replace("|#]", "").trim();
            respostaGemini = respostaGemini.replace("\n", "");
            respostaGemini = respostaGemini.replace("\t", "");

            System.out.println("Resposta Bruta do Gemini (no Servlet - Limpa): " + respostaGemini);

            respostaJson = new JSONObject(respostaGemini);
            planoCompletoJson = respostaJson.optJSONObject("plano_completo");

            if (planoCompletoJson != null) {
                planoDietaJson = planoCompletoJson.optJSONObject("plano_dieta");
                planoTreinoJson = planoCompletoJson.optJSONObject("plano_treino");
                System.out.println("planoDietaJson (no Servlet): " + planoDietaJson);
                System.out.println("planoTreinoJson (no Servlet): " + planoTreinoJson);
                salvarPlanoNoBanco.salvarPlanoNoBanco(idUsuario, planoDietaJson, planoTreinoJson);
                System.out.println("Plano salvo no banco com sucesso.");
            } else {
                System.err.println("Seção 'plano_completo' não encontrada no JSON.");
            }

        } catch (JSONException e) {
            System.err.println("Erro ao analisar JSON: " + e.getMessage());
            if (respostaGemini != null && respostaGemini.length() > 500) {
                System.err.println("Últimos 500 caracteres da resposta Gemini:\n" + respostaGemini.substring(respostaGemini.length() - 500));
            } else if (respostaGemini != null) {
                System.err.println("Resposta Gemini completa:\n" + respostaGemini);
            }
            respostaGemini = "Erro ao obter e analisar a resposta do Gemini: " + e.getMessage();
            e.printStackTrace();
        } catch (Exception e) {
            respostaGemini = "Erro ao obter resposta do Gemini: " + e.getMessage();
            e.printStackTrace();
        }

        // Configura a resposta para HTML
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html lang=\"pt-BR\">");
        out.println("<head>");
        out.println("<meta charset=\"UTF-8\">");
        out.println("<title>Plano de Dieta e Treino</title>");
        // INÍCIO CSS ADICIONADO
        out.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        out.println("<link rel=\"stylesheet\" href=\"https://unpkg.com/@phosphor-icons/web@2.0.3/src/regular/style.css\" />");
        out.println("<style>");
        out.println("body { background: #181c1f; color: #f7f7f7; font-family: 'Inter', 'Segoe UI', Arial, sans-serif; }");
        out.println(".container { background: #23272b; border-radius: 18px; box-shadow: 0 0 40px #A0D68333; padding: 36px 32px 36px 32px; margin: 56px auto 48px auto; max-width: 720px; }");
        out.println("h1 { color: #A0D683; font-size: 2.1rem; font-weight: bold; text-align: center; margin-bottom: 1.4em; background: linear-gradient(90deg, #A0D683 70%, #8b5cf6 100%); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; }");
        out.println("h2 { color: #A0D683; font-size: 1.35rem; font-weight: bold; margin-top: 2.2em; margin-bottom: 1.2em; text-align:left; letter-spacing:0.01em; }");
        out.println("h3 { color: #8b5cf6; font-size:1.11rem; font-weight:bold; margin-top:1.2em; margin-bottom:0.6em; }");
        out.println("ul { margin-bottom: 1.6em; }");
        out.println("li { margin-bottom: 0.55em; }");
        out.println("strong { color: #A0D683; }");
        out.println(".btn { background: linear-gradient(90deg, #A0D683 0%, #7DD23B 100%); color: #23272b; font-weight: bold; padding: 13px 24px; border-radius: 7px; border: none; box-shadow: 0 2px 8px 0 rgba(160, 214, 131, 0.12); font-size: 1.13rem; transition: background 0.3s, color 0.3s, filter 0.2s; display: inline-flex; align-items: center; gap: 0.5rem; cursor: pointer; margin-top: 2rem; }");
        out.println(".btn:hover, .btn:focus { background: linear-gradient(90deg, #7DD23B 0%, #A0D683 100%); color: #181c1f; filter: brightness(0.98); outline: none; }");
        out.println("@media (max-width: 700px) { .container { padding: 1.3rem 0.6rem; max-width: 98vw; } h1 { font-size: 1.35rem; } .btn { width: 100%; justify-content: center; } }");
        out.println("</style>");
        // FIM CSS ADICIONADO
        out.println("</head>");
        out.println("<body>");
        out.println("<div class=\"container\">");
        out.println("<h1><i class=\"ph ph-leaf\"></i> Plano de Dieta e Treino</h1>");

        if (planoCompletoJson != null) {
            out.println("<h2><i class='ph ph-bowl-food'></i> Plano de Dieta</h2>");
            if (planoDietaJson != null) {
                out.println("<p><strong>Objetivo:</strong> " + planoDietaJson.optString("objetivo") + "</p>");
                out.println("<p><strong>Calorias Totais Estimadas:</strong> " + planoDietaJson.optString("calorias_totais") + "kcal</p>");

                JSONObject macroJson = planoDietaJson.optJSONObject("macronutrientes");
                if (macroJson != null) {
                    out.println("<h3>Macronutrientes:</h3>");
                    out.println("<ul>");
                    out.println("<li><strong>Proteínas:</strong> " + macroJson.optString("proteinas") + "G</li>");
                    out.println("<li><strong>Carboidratos:</strong> " + macroJson.optString("carboidratos") + "G</li>");
                    out.println("<li><strong>Gorduras:</strong> " + macroJson.optString("gorduras") + "G</li>");
                    out.println("</ul>");
                }

                JSONObject refeicoesJson = planoDietaJson.optJSONObject("refeicoes");
                if (refeicoesJson != null) {
                    out.println("<h3>Cardápio:</h3>");
                    out.println("<ul>");
                    out.println("<li><strong>Café da Manhã:</strong> " + refeicoesJson.optString("cafe_da_manha") + "</li>");
                    out.println("<li><strong>Almoço:</strong> " + refeicoesJson.optString("almoco") + "</li>");
                    out.println("<li><strong>Lanche da Tarde:</strong> " + refeicoesJson.optString("lanche_tarde") + "</li>");
                    out.println("<li><strong>Jantar:</strong> " + refeicoesJson.optString("jantar") + "</li>");
                    out.println("</ul>");
                }

                String observacoesDieta = planoDietaJson.optString("observacoes");
                if (!observacoesDieta.isEmpty()) {
                    out.println("<h3>Observações da Dieta:</h3>");
                    out.println("<p>" + observacoesDieta + "</p>");
                }
            } else {
                out.println("<p>Erro ao exibir o plano de dieta.</p>");
            }

            out.println("<h2><i class='ph ph-dumbbell'></i> Plano de Treino</h2>");
            if (planoTreinoJson != null) {
                out.println("<p><strong>Divisão do Treino:</strong> " + planoTreinoJson.optString("divisao") + "</p>");
                out.println("<p><strong>Justificativa da Divisão:</strong> " + planoTreinoJson.optString("justificativa_divisao") + "</p>");

                exibirTreino(out, planoTreinoJson.optJSONObject("treino_a"), "A");
                exibirTreino(out, planoTreinoJson.optJSONObject("treino_b"), "B");
                exibirTreino(out, planoTreinoJson.optJSONObject("treino_c"), "C");

                String observacoesTreino = planoTreinoJson.optString("observacoes");
                if (!observacoesTreino.isEmpty()) {
                    out.println("<h3>Observações do Treino:</h3>");
                    out.println("<p>" + observacoesTreino + "</p>");
                }

            } else {
                out.println("<p>Erro ao exibir o plano de treino.</p>");
            }

        } else {
            out.println("<p>Erro ao exibir o plano completo.</p>");
            out.println("<p>Resposta Bruta do Gemini (Limpa): " + respostaGemini + "</p>");
        }

        // BOTÃO DE REDIRECIONAMENTO ADICIONADO ABAIXO
        out.println("<form action=\"home.jsp\" method=\"get\" style=\"text-align:center;\">");
        out.println("<button class=\"btn\" type=\"submit\"><i class=\"ph ph-house\"></i> Voltar para Home</button>");
        out.println("</form>");

        out.println("</div>");
        out.println("</body>");
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