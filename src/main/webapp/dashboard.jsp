<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page session="true" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ include file="WEB-INF/jspf/header.jspf" %>
<%@ include file="WEB-INF/jspf/html-head.jspf" %>
<style>
    body {
        background: #181c1f; color: #f7f7f7; font-family: 'Open Sans', 'Roboto', Arial, sans-serif;
        margin: 0; padding: 0; min-height: 100vh;
    }
    .main-container { max-width: 1200px; margin: 0 auto; padding: 40px 16px 24px 16px; }
    .dashboard { text-align: center; margin-top: 30px; }
    .card {
        display: inline-block; width: 220px; padding: 15px; margin: 10px;
        background: #23272b; border-radius: 10px; box-shadow: 2px 2px 6px #222a;
        text-align: center; color: #f7f7f7; vertical-align: top;
    }
    .card hr { border-color: #444; margin: 8px 0; }
    .card small { color: #bbb; }
    .graficos-lado-lado { display: flex; justify-content: center; gap: 40px; margin: 40px 0; flex-wrap: wrap; }
    .metas-container { display: flex; flex-direction: column; gap: 20px; width: 100%; max-width: 340px; }
    .progress-bar-container { width: 100%; }
    .progress-bar-title { margin-bottom: 5px; font-weight: bold; }
    .progress-bar-bg { background: #333; border-radius: 8px; height: 28px; width: 100%; position: relative; }
    .progress-bar-fill {
        height: 100%; border-radius: 8px; text-align: center; color: #fff;
        font-weight: bold; line-height: 28px; white-space: nowrap;
        overflow: hidden; font-size: 14px; transition: width 0.5s ease-in-out;
    }
    .view-selector { text-align: center; margin-bottom: 40px; }
    .view-btn {
        padding: 10px 25px; border: 1px solid #4caf50; background: transparent;
        color: #4caf50; font-size: 16px; cursor: pointer; transition: all 0.2s ease;
    }
    .view-btn:first-of-type { border-radius: 8px 0 0 8px; }
    .view-btn:last-of-type { border-radius: 0 8px 8px 0; }
    .view-btn.active, .view-btn:hover { background: #4caf50; color: #fff; }

    .consistencia-card {
        background-color: #23272b; padding: 20px; border-radius: 10px;
        text-align: center; width: 100%; max-width: 450px;
    }
    .consistencia-card h4 { margin-top: 0; color: #4caf50; }
    .consistencia-dias { font-size: 2.5em; font-weight: bold; color: #fdd835; }
</style>

<div class="main-container">
    <%
        String view = (String) request.getAttribute("view");
        String todayBtnClass = "view-btn" + ("today".equals(view) ? " active" : "");
        String weeklyBtnClass = "view-btn" + ("weekly".equals(view) ? " active" : "");
        DecimalFormat df = new DecimalFormat("#.#");
    %>
    <h2 style="text-align:center; margin-top: 30px;">Meu Dashboard</h2>
    
    <div class="view-selector">
        <a href="dashboard?view=today"><button class="<%= todayBtnClass %>">Hoje</button></a>
        <a href="dashboard?view=weekly"><button class="<%= weeklyBtnClass %>">Análise Semanal</button></a>
    </div>

    <% if ("weekly".equals(view)) { %>
        <%-- ######### VISÃO SEMANAL (COMPARATIVA) ######### --%>
        <div class="dashboard">
            <div class="card">Nível Fome<br><strong><%= request.getAttribute("fomeHoje") %></strong><hr><small>Mais Comum: <%= request.getAttribute("fomeMedia") %></small></div>
            <div class="card">Energia<br><strong><%= request.getAttribute("energiaHoje") %></strong><hr><small>Mais Comum: <%= request.getAttribute("energiaMedia") %></small></div>
            <div class="card">Qualidade do Sono<br><strong><%= request.getAttribute("sonoHoje") %></strong><hr><small>Mais Comum: <%= request.getAttribute("sonoMedia") %></small></div>
        </div>
        
        <h3 style="text-align:center; margin-top: 50px; color: #4caf50;">Análise Comparativa: Hoje vs. Média da Semana</h3>
        <div class="graficos-lado-lado">
            <div style="width: 100%; max-width: 450px;"><canvas id="refeicoesComparativo"></canvas></div>
            <div style="width: 100%; max-width: 450px;"><canvas id="caloriasGauge"></canvas></div>
        </div>
        
        <h3 style="text-align:center; margin-top: 50px; color: #4caf50;">Consistência Semanal (Últimos 7 dias)</h3>
        <div class="graficos-lado-lado">
            <div class="consistencia-card">
                <h4>Meta de Água</h4>
                <p>Você atingiu sua meta em</p>
                <span class="consistencia-dias"><%= request.getAttribute("diasMetaAgua") != null ? request.getAttribute("diasMetaAgua") : 0 %></span> de 7 dias
            </div>
            <div class="consistencia-card">
                <h4>Meta de Calorias</h4>
                <p>Você atingiu sua meta em</p>
                <span class="consistencia-dias"><%= request.getAttribute("diasMetaCalorias") != null ? request.getAttribute("diasMetaCalorias") : 0 %></span> de 7 dias
            </div>
        </div>

    <% } else { %>
        <%-- ######### VISÃO DIÁRIA (PADRÃO) ######### --%>
        <div class="dashboard">
            <div class="card">Nível Fome: <strong><%= request.getAttribute("fomeHoje") %></strong></div>
            <div class="card">Energia: <strong><%= request.getAttribute("energiaHoje") %></strong></div>
            <div class="card">Qualidade do Sono: <strong><%= request.getAttribute("sonoHoje") %></strong></div>
        </div>

        <h3 style="text-align:center; margin-top: 50px; color: #4caf50;">Resumo de Hoje</h3>
        <div class="graficos-lado-lado">
            <div style="width: 100%; max-width: 340px;">
                <canvas id="pizzaRefeicoesHoje"></canvas>
            </div>
            
            <div class="metas-container">
                <%
                    // Lógica para a barra de ÁGUA
                    double aguaHoje = (Double) (request.getAttribute("aguaHoje") != null ? request.getAttribute("aguaHoje") : 0.0);
                    int metaAgua = (Integer) (request.getAttribute("metaAgua") != null ? request.getAttribute("metaAgua") : 0);
                    int porcentagemAgua = metaAgua > 0 ? (int)((aguaHoje / metaAgua) * 100) : 0;
                    String corAgua = porcentagemAgua > 110 ? "#e53935" : "#2196f3";
                    int larguraBarraAgua = Math.min(100, porcentagemAgua);

                    // Lógica para a barra de CALORIAS
                    int caloriasHoje = (Integer) (request.getAttribute("caloriasHoje") != null ? request.getAttribute("caloriasHoje") : 0);
                    int metaCalorias = (Integer) (request.getAttribute("metaCalorias") != null ? request.getAttribute("metaCalorias") : 0);
                    int porcentagemCalorias = metaCalorias > 0 ? (int)(((double)caloriasHoje / metaCalorias) * 100) : 0;
                    String corCalorias = "#4caf50"; // Verde por padrão
                    if (porcentagemCalorias > 105) {
                        corCalorias = "#e53935"; // Vermelho se passar de 105%
                    } else if (porcentagemCalorias >= 90) {
                        corCalorias = "#fdd835"; // Amarelo se estiver perto (entre 90% e 105%)
                    }
                    int larguraBarraCalorias = Math.min(100, porcentagemCalorias);
                %>
                <div class="progress-bar-container">
                    <div class="progress-bar-title">Água</div>
                    <div class="progress-bar-bg">
                        <div class="progress-bar-fill" style="width: <%=larguraBarraAgua%>%; background-color: <%=corAgua%>;">
                            <%= df.format(aguaHoje) %> L / <%= metaAgua %> L (<%=porcentagemAgua%>%)
                        </div>
                    </div>
                </div>
                <div class="progress-bar-container">
                    <div class="progress-bar-title">Calorias</div>
                    <div class="progress-bar-bg">
                        <div class="progress-bar-fill" style="width: <%=larguraBarraCalorias%>%; background-color: <%=corCalorias%>; color: <%= (porcentagemCalorias >= 90 && porcentagemCalorias <= 105) ? "#181c1f" : "#fff" %>;">
                            <%=caloriasHoje%> kcal / <%=metaCalorias%> kcal (<%=porcentagemCalorias%>%)
                        </div>
                    </div>
                </div>
            </div>
        </div>
    <% } %>
</div>

<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
<script>
    // ESTE SCRIPT CONTÉM APENAS A LÓGICA DOS GRÁFICOS DAS DUAS VISÕES
    const view = "<%= view %>";

    if (view === "weekly") {
        const cafeHoje = <%= request.getAttribute("cafeHoje") != null ? request.getAttribute("cafeHoje") : 0 %>;
        const almocoHoje = <%= request.getAttribute("almocoHoje") != null ? request.getAttribute("almocoHoje") : 0 %>;
        const jantarHoje = <%= request.getAttribute("jantarHoje") != null ? request.getAttribute("jantarHoje") : 0 %>;
        const lanchesHoje = <%= request.getAttribute("lanchesHoje") != null ? request.getAttribute("lanchesHoje") : 0 %>;

        const cafeMedia = <%= request.getAttribute("cafeMedia") != null ? Math.round((Double)request.getAttribute("cafeMedia")) : 0 %>;
        const almocoMedia = <%= request.getAttribute("almocoMedia") != null ? Math.round((Double)request.getAttribute("almocoMedia")) : 0 %>;
        const jantarMedia = <%= request.getAttribute("jantarMedia") != null ? Math.round((Double)request.getAttribute("jantarMedia")) : 0 %>;
        const lanchesMedia = <%= request.getAttribute("lanchesMedia") != null ? Math.round((Double)request.getAttribute("lanchesMedia")) : 0 %>;
        
        new Chart(document.getElementById('refeicoesComparativo'), {
            type: 'bar',
            data: {
                labels: ['Café', 'Almoço', 'Jantar', 'Lanches'],
                datasets: [
                    { label: 'Hoje', data: [cafeHoje, almocoHoje, jantarHoje, lanchesHoje], backgroundColor: '#4caf50' },
                    { label: 'Média da Semana', data: [cafeMedia, almocoMedia, jantarMedia, lanchesMedia], backgroundColor: '#777' }
                ]
            },
            options: { plugins: { title: { display: true, text: 'Calorias por Refeição: Hoje vs. Média' } }, scales: { y: { beginAtZero: true } } }
        });

        const caloriasHoje = <%= request.getAttribute("caloriasHoje") != null ? (Integer)request.getAttribute("caloriasHoje") : 0 %>;
        const caloriasMedia = <%= request.getAttribute("caloriasMedia") != null ? Math.round((Double)request.getAttribute("caloriasMedia")) : 0 %>;

        new Chart(document.getElementById('caloriasGauge'), {
            type: 'bar',
            data: {
                labels: ['Calorias Totais'],
                datasets: [
                    { label: 'Hoje', data: [caloriasHoje], backgroundColor: caloriasHoje > caloriasMedia ? '#e53935' : '#4caf50', barPercentage: 0.5 },
                    { label: 'Média da Semana (referência)', data: [caloriasMedia], backgroundColor: 'transparent', borderColor: '#fdd835', borderWidth: 3, barPercentage: 0.5 }
                ]
            },
            options: { indexAxis: 'y', plugins: { title: { display: true, text: 'Performance de Calorias: Hoje vs. Média' } }, scales: { x: { beginAtZero: true } } }
        });

    } else {
        const cafeHoje = <%= request.getAttribute("cafeHoje") != null ? request.getAttribute("cafeHoje") : 0 %>;
        const almocoHoje = <%= request.getAttribute("almocoHoje") != null ? request.getAttribute("almocoHoje") : 0 %>;
        const jantarHoje = <%= request.getAttribute("jantarHoje") != null ? request.getAttribute("jantarHoje") : 0 %>;
        const lanchesHoje = <%= request.getAttribute("lanchesHoje") != null ? request.getAttribute("lanchesHoje") : 0 %>;

        new Chart(document.getElementById('pizzaRefeicoesHoje'), {
            type: 'pie',
            data: {
                labels: ['Café', 'Almoço', 'Jantar', 'Lanches'],
                datasets: [{
                    data: [cafeHoje, almocoHoje, jantarHoje, lanchesHoje],
                    backgroundColor: ['#ffa07a', '#f08080', '#20b2aa', '#87cefa']
                }]
            },
            options: { plugins: { title: { display: true, text: 'Distribuição de Calorias de Hoje' } } }
        });
    }
</script>
<%@ include file="WEB-INF/jspf/footer.jspf" %>