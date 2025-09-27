package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Properties;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int idUsuario = -1;
        HttpSession session = request.getSession(false);

        if (session != null) {
            Object idObj = session.getAttribute("idUsuario");

            if (idObj != null) {
                try {
                    idUsuario = Integer.parseInt(idObj.toString());
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

        // --- MODIFICAÇÃO AQUI: RECEBER DIA, MÊS E ANO DOS PARÂMETROS DA REQUISIÇÃO ---
        int dia;
        int mes;
        int ano;

        // Tenta obter os parâmetros 'dia', 'mes', 'ano' da URL.
        // Se não existirem (e.g., primeira vez que entra no dashboard sem ter vindo de um dia específico),
        // usa a data atual.
        try {
            String diaParam = request.getParameter("dia");
            String mesParam = request.getParameter("mes");
            String anoParam = request.getParameter("ano");

            if (diaParam != null && mesParam != null && anoParam != null) {
                dia = Integer.parseInt(diaParam);
                mes = Integer.parseInt(mesParam);
                ano = Integer.parseInt(anoParam);
            } else {
                // Se os parâmetros não foram fornecidos, use a data atual
                LocalDate today = LocalDate.now();
                dia = today.getDayOfMonth();
                mes = today.getMonthValue();
                ano = today.getYear();
            }
        } catch (NumberFormatException e) {
            // Caso os parâmetros sejam inválidos (não números), use a data atual
            LocalDate today = LocalDate.now();
            dia = today.getDayOfMonth();
            mes = today.getMonthValue();
            ano = today.getYear();
            System.err.println("Parâmetros de data inválidos. Usando a data atual: " + e.getMessage());
        }

        Connection conn = null;

        try {
            Properties props = new Properties();
            try (InputStream input = getServletContext().getResourceAsStream("/WEB-INF/classes/db.properties")) {
                props.load(input);
            }

            Class.forName(props.getProperty("db.driver"));
            conn = DriverManager.getConnection(
                    props.getProperty("db.url"),
                    props.getProperty("db.username"),
                    props.getProperty("db.password")
            );

            // DADOS DO DIA ATUAL
            String sql = "SELECT * FROM dados_diarios WHERE id_usuario = ? AND dia = ? AND mes = ? AND ano = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idUsuario);
            stmt.setInt(2, dia);
            stmt.setInt(3, mes);
            stmt.setInt(4, ano);

            ResultSet rs = stmt.executeQuery();
            int cafe = 0, almoco = 0, jantar = 0, lanches = 0;
            String nivel_fome = "-";
            if (rs.next()) {
                cafe = rs.getInt("cafe_da_manha_calorias");
                almoco = rs.getInt("almoco_calorias");
                jantar = rs.getInt("jantar_calorias");
                lanches = rs.getInt("lanches_calorias");
                String nf = rs.getString("nivel_fome");
                nivel_fome = (nf != null && !nf.isEmpty()) ? nf : "-";
                request.setAttribute("cafe", cafe);
                request.setAttribute("almoco", almoco);
                request.setAttribute("jantar", jantar);
                request.setAttribute("lanches", lanches);
                request.setAttribute("agua", rs.getString("agua"));
                request.setAttribute("energia", rs.getString("nivel_energia"));
                request.setAttribute("sono", rs.getString("qualidade_sono"));
                request.setAttribute("tipo_treino", rs.getString("tipo_treino"));
                request.setAttribute("duracao_treino", rs.getString("duracao_treino"));
                request.setAttribute("intensidade_treino", rs.getString("intensidade_treino"));
                request.setAttribute("nivel_fome", rs.getString("nivel_fome"));

            } else {
                request.setAttribute("cafe", 0);
                request.setAttribute("almoco", 0);
                request.setAttribute("jantar", 0);
                request.setAttribute("lanches", 0);
                request.setAttribute("agua", "");
                request.setAttribute("energia", "");
                request.setAttribute("sono", "");
                request.setAttribute("tipo_treino", "-");
                request.setAttribute("duracao_treino", "-");
                request.setAttribute("intensidade_treino", "-");
                request.setAttribute("nivel_fome", "-");
            }   
            request.setAttribute("nivel_fome", nivel_fome);

            int totalCalorias = cafe + almoco + jantar + lanches;
            request.setAttribute("totalCalorias", totalCalorias);

            // Buscar meta de calorias e meta de água do usuário (última dieta cadastrada)
            String metaSql = "SELECT d.calorias_totais, d.meta_agua FROM dieta d " +
                            "JOIN plano p ON d.plano_id = p.id " +
                            "WHERE p.id_usuario = ? ORDER BY d.id DESC LIMIT 1";
            try (PreparedStatement metaStmt = conn.prepareStatement(metaSql)) {
                metaStmt.setInt(1, idUsuario);
                ResultSet metaRs = metaStmt.executeQuery();
                if (metaRs.next()) {
                    int metaCalorias = metaRs.getInt("calorias_totais");
                    int metaAgua = metaRs.getInt("meta_agua");
                    request.setAttribute("metaCalorias", metaCalorias);
                    request.setAttribute("metaAgua", metaAgua);
                } else {
                    request.setAttribute("metaCalorias", 0);
                    request.setAttribute("metaAgua", 0);
                }
            }

            // ----------- DADOS DOS ÚLTIMOS 7 DIAS PARA GRÁFICOS SEMANAIS -----------
            LocalDate hoje = LocalDate.of(ano, mes, dia);
            int[] caloriasSemana = new int[7];
            int[] metaCaloriasSemana = new int[7];
            int[] bateuMetaCaloriasSemana = new int[7];
            int[] bateuMetaAguaSemana = new int[7];
            String[] diasSemana = new String[7];

            for (int i = 6; i >= 0; i--) {
                LocalDate diaLoop = hoje.minusDays(i);
                diasSemana[6 - i] = diaLoop.getDayOfWeek().getDisplayName(TextStyle.SHORT, new Locale("pt", "BR"));

                String sqlDia = "SELECT cafe_da_manha_calorias, almoco_calorias, jantar_calorias, lanches_calorias, agua " +
                                "FROM dados_diarios WHERE id_usuario = ? AND dia = ? AND mes = ? AND ano = ?";
                try (PreparedStatement stmtDia = conn.prepareStatement(sqlDia)) {
                    stmtDia.setInt(1, idUsuario);
                    stmtDia.setInt(2, diaLoop.getDayOfMonth());
                    stmtDia.setInt(3, diaLoop.getMonthValue());
                    stmtDia.setInt(4, diaLoop.getYear());
                    ResultSet rsDia = stmtDia.executeQuery();
                    int total = 0;
                    double agua = 0;
                    if (rsDia.next()) {
                        total = rsDia.getInt("cafe_da_manha_calorias") + rsDia.getInt("almoco_calorias") +
                                rsDia.getInt("jantar_calorias") + rsDia.getInt("lanches_calorias");
                        String aguaStr = rsDia.getString("agua");
                        if (aguaStr != null && !aguaStr.isEmpty()) {
                            try { agua = Double.parseDouble(aguaStr.replace(",", ".")); } catch(Exception e) { agua = 0; }
                        }
                    }
                    caloriasSemana[6 - i] = total;

                    String metaSqlDia = "SELECT d.calorias_totais, d.meta_agua FROM dieta d " +
                            "JOIN plano p ON d.plano_id = p.id " +
                            "WHERE p.id_usuario = ? ORDER BY d.id DESC LIMIT 1";
                    try (PreparedStatement metaStmtDia = conn.prepareStatement(metaSqlDia)) {
                        metaStmtDia.setInt(1, idUsuario);
                        ResultSet metaRsDia = metaStmtDia.executeQuery();
                        int metaCal = 0, metaAg = 0;
                        if (metaRsDia.next()) {
                            metaCal = metaRsDia.getInt("calorias_totais");
                            metaAg = metaRsDia.getInt("meta_agua");
                        }
                        metaCaloriasSemana[6 - i] = metaCal;
                        bateuMetaCaloriasSemana[6 - i] = (total >= metaCal && metaCal > 0) ? 1 : 0;
                        bateuMetaAguaSemana[6 - i] = (agua >= metaAg && metaAg > 0) ? 1 : 0;
                    }
                }
            }

            // Envie como JSON para o JSP (para funcionar com JSON.parse no JS)
            request.setAttribute("caloriasSemana", "[" + String.join(",", java.util.Arrays.stream(caloriasSemana).mapToObj(String::valueOf).toArray(String[]::new)) + "]");
            request.setAttribute("metaCaloriasSemana", "[" + String.join(",", java.util.Arrays.stream(metaCaloriasSemana).mapToObj(String::valueOf).toArray(String[]::new)) + "]");
            request.setAttribute("diasSemana", "[\"" + String.join("\",\"", diasSemana) + "\"]");
            request.setAttribute("bateuMetaCaloriasSemana", "[" + String.join(",", java.util.Arrays.stream(bateuMetaCaloriasSemana).mapToObj(String::valueOf).toArray(String[]::new)) + "]");
            request.setAttribute("bateuMetaAguaSemana", "[" + String.join(",", java.util.Arrays.stream(bateuMetaAguaSemana).mapToObj(String::valueOf).toArray(String[]::new)) + "]");

        } catch (IOException | ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null && !conn.isClosed()) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        request.getRequestDispatcher("/dashboard.jsp").forward(request, response);
    }
}