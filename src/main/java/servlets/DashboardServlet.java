package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDate;
import java.util.Properties;
import java.sql.SQLException;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        int idUsuario = -1;

        if (session != null && session.getAttribute("idUsuario") != null) {
            Object idObj = session.getAttribute("idUsuario");
            try {
                idUsuario = Integer.parseInt(idObj.toString());
            } catch (NumberFormatException e) {
                response.sendRedirect("login.jsp?error=sessaoInvalida");
                return;
            }
        } else {
            response.sendRedirect("login.jsp?error=naoLogado");
            return;
        }

        String view = request.getParameter("view") != null ? request.getParameter("view") : "today";
        request.setAttribute("view", view);

        Connection conn = null;
        try {
            Properties props = new Properties();
            try (InputStream input = getServletContext().getResourceAsStream("/WEB-INF/classes/db.properties")) {
                props.load(input);
            }
            Class.forName(props.getProperty("db.driver"));
            conn = DriverManager.getConnection(props.getProperty("db.url"), props.getProperty("db.username"), props.getProperty("db.password"));
            
            LocalDate hoje = LocalDate.now();

            // --- BUSCAR METAS GERAIS (ÁGUA E CALORIAS) ---
            int metaAgua = 0, metaCalorias = 0;
            String metaSql = "SELECT meta_agua, calorias_totais FROM dieta d JOIN plano p ON d.plano_id = p.id WHERE p.id_usuario = ? ORDER BY d.id DESC LIMIT 1";
            try (PreparedStatement metaStmt = conn.prepareStatement(metaSql)) {
                metaStmt.setInt(1, idUsuario);
                ResultSet rs = metaStmt.executeQuery();
                if (rs.next()) {
                    metaAgua = rs.getInt("meta_agua");
                    metaCalorias = rs.getInt("calorias_totais");
                }
            }
            request.setAttribute("metaAgua", metaAgua);
            request.setAttribute("metaCalorias", metaCalorias);

            // --- BUSCA DADOS DE HOJE ---
            String sqlHoje = "SELECT *, (cafe_da_manha_calorias + almoco_calorias + jantar_calorias + lanches_calorias) as total_calorias FROM dados_diarios WHERE id_usuario = ? AND dia = ? AND mes = ? AND ano = ?";
            PreparedStatement stmtHoje = conn.prepareStatement(sqlHoje);
            stmtHoje.setInt(1, idUsuario);
            stmtHoje.setInt(2, hoje.getDayOfMonth());
            stmtHoje.setInt(3, hoje.getMonthValue());
            stmtHoje.setInt(4, hoje.getYear());
            ResultSet rsHoje = stmtHoje.executeQuery();

            if (rsHoje.next()) {
                request.setAttribute("cafeHoje", rsHoje.getInt("cafe_da_manha_calorias"));
                request.setAttribute("almocoHoje", rsHoje.getInt("almoco_calorias"));
                request.setAttribute("jantarHoje", rsHoje.getInt("jantar_calorias"));
                request.setAttribute("lanchesHoje", rsHoje.getInt("lanches_calorias"));
                request.setAttribute("caloriasHoje", rsHoje.getInt("total_calorias"));
                request.setAttribute("fomeHoje", rsHoje.getString("nivel_fome"));
                request.setAttribute("energiaHoje", rsHoje.getString("nivel_energia"));
                request.setAttribute("sonoHoje", rsHoje.getString("qualidade_sono"));
                double aguaHoje = 0;
                try { aguaHoje = Double.parseDouble(rsHoje.getString("agua").replace(",",".")); } catch(Exception e){}
                request.setAttribute("aguaHoje", aguaHoje);
            } else {
                // Seta valores padrão se não houver registro para hoje
                request.setAttribute("cafeHoje", 0); request.setAttribute("almocoHoje", 0); request.setAttribute("jantarHoje", 0);
                request.setAttribute("lanchesHoje", 0); request.setAttribute("caloriasHoje", 0);
                request.setAttribute("fomeHoje", "N/A"); request.setAttribute("energiaHoje", "N/A"); request.setAttribute("sonoHoje", "N/A");
                request.setAttribute("aguaHoje", 0.0);
            }

            // --- SE A VISÃO FOR SEMANAL, BUSCA DADOS ADICIONAIS ---
            if ("weekly".equals(view)) {
                LocalDate seteDiasAtras = hoje.minusDays(7);
                
                String sqlMedia = "SELECT " +
                        "AVG(cafe_da_manha_calorias) as media_cafe, AVG(almoco_calorias) as media_almoco, AVG(jantar_calorias) as media_jantar, AVG(lanches_calorias) as media_lanches, " +
                        "MODE() WITHIN GROUP (ORDER BY nivel_fome) as moda_fome, MODE() WITHIN GROUP (ORDER BY nivel_energia) as moda_energia, MODE() WITHIN GROUP (ORDER BY qualidade_sono) as moda_sono " +
                        "FROM dados_diarios WHERE id_usuario = ? AND TO_DATE(ano || '-' || mes || '-' || dia, 'YYYY-MM-DD') BETWEEN ? AND ?";
                PreparedStatement stmtMedia = conn.prepareStatement(sqlMedia);
                stmtMedia.setInt(1, idUsuario);
                stmtMedia.setDate(2, java.sql.Date.valueOf(seteDiasAtras));
                stmtMedia.setDate(3, java.sql.Date.valueOf(hoje.minusDays(1)));
                ResultSet rsMedia = stmtMedia.executeQuery();
                if (rsMedia.next()) {
                    // (seta os atributos de média como antes para os gráficos comparativos)
                    double mediaCafe = rsMedia.getDouble("media_cafe");
                    double mediaAlmoco = rsMedia.getDouble("media_almoco");
                    double mediaJantar = rsMedia.getDouble("media_jantar");
                    double mediaLanches = rsMedia.getDouble("media_lanches");
                    request.setAttribute("cafeMedia", mediaCafe);
                    request.setAttribute("almocoMedia", mediaAlmoco);
                    request.setAttribute("jantarMedia", mediaJantar);
                    request.setAttribute("lanchesMedia", mediaLanches);
                    request.setAttribute("caloriasMedia", mediaCafe + mediaAlmoco + mediaJantar + mediaLanches);
                    request.setAttribute("fomeMedia", rsMedia.getString("moda_fome"));
                    request.setAttribute("energiaMedia", rsMedia.getString("moda_energia"));
                    request.setAttribute("sonoMedia", rsMedia.getString("moda_sono"));
                }

                // LÓGICA ATUALIZADA: Contar dias em que a meta de ÁGUA e CALORIAS foi atingida
                String sqlConsistencia = "SELECT " +
                                         "SUM(CASE WHEN CAST(REPLACE(agua, ',', '.') AS NUMERIC) >= ? THEN 1 ELSE 0 END) as dias_agua, " +
                                         "SUM(CASE WHEN (cafe_da_manha_calorias + almoco_calorias + jantar_calorias + lanches_calorias) >= ? THEN 1 ELSE 0 END) as dias_calorias " +
                                         "FROM dados_diarios WHERE id_usuario = ? AND TO_DATE(ano || '-' || mes || '-' || dia, 'YYYY-MM-DD') BETWEEN ? AND ?";
                PreparedStatement stmtConsistencia = conn.prepareStatement(sqlConsistencia);
                stmtConsistencia.setInt(1, metaAgua);
                stmtConsistencia.setInt(2, metaCalorias);
                stmtConsistencia.setInt(3, idUsuario);
                stmtConsistencia.setDate(4, java.sql.Date.valueOf(seteDiasAtras));
                stmtConsistencia.setDate(5, java.sql.Date.valueOf(hoje.minusDays(1)));
                ResultSet rsConsistencia = stmtConsistencia.executeQuery();
                if (rsConsistencia.next()) {
                    request.setAttribute("diasMetaAgua", rsConsistencia.getInt("dias_agua"));
                    request.setAttribute("diasMetaCalorias", rsConsistencia.getInt("dias_calorias"));
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "Ocorreu um erro ao carregar os dados do dashboard.");
        } finally {
            try { if (conn != null && !conn.isClosed()) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }

        request.getRequestDispatcher("/dashboard.jsp").forward(request, response);
    }
}