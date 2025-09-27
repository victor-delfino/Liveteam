<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page session="true" %>
<%@ include file="WEB-INF/jspf/header.jspf" %>
<%@ include file="WEB-INF/jspf/html-head.jspf" %>
<style>
    body {
        background: #181c1f;
        color: #f7f7f7;
        font-family: 'Open Sans', 'Roboto', Arial, sans-serif;
        margin: 0;
        padding: 0;
        min-height: 100vh;
    }
    .main-container {
        max-width: 1200px;
        margin: 0 auto;
        padding: 40px 16px 24px 16px;
        min-height: 80vh;
    }
    .dashboard {
        text-align: center;
        margin-top: 30px;
    }
    .card {
        display: inline-block;
        width: 200px;
        padding: 15px;
        margin: 10px;
        background: #23272b;
        border-radius: 10px;
        box-shadow: 2px 2px 6px #222a;
        text-align: center;
        color: #f7f7f7;
    }
    .progress-bar-bg {
        background: #eee;
        border-radius: 8px;
        height: 28px;
        width: 100%;
        margin-bottom: 10px;
    }
    .progress-bar-fill {
        height: 28px;
        border-radius: 8px;
        text-align: center;
        color: #fff;
        font-weight: bold;
        line-height: 28px;
        white-space: nowrap; /* Não deixa quebrar linha */
        overflow: hidden;
        text-overflow: ellipsis;
        font-size: 16px;
    }
    .progress-bar-fill.red {
        background: #e53935 !important;
    }
    .alerta {
        background: #ffe0e0;
        color: #b71c1c;
        border: 1px solid #b71c1c;
        border-radius: 8px;
        padding: 10px;
        margin: 20px auto;
        width: 400px;
        text-align: center;
        font-weight: bold;
    }
    .consistencia-grid {
        display: flex;
        justify-content: center;
        gap: 30px;
        margin: 40px 0 20px 0;
    }
    .consistencia-col {
        text-align: center;
    }
    .consistencia-dot {
        display: inline-block;
        width: 22px;
        height: 22px;
        border-radius: 50%;
        margin: 0 4px;
        line-height: 22px;
        font-size: 13px;
        color: #fff;
        font-weight: bold;
    }
    .dot-ok {
        background: #4caf50;
    }
    .dot-nok {
        background: #e53935;
    }
    .consistencia-label {
        margin-top: 8px;
        font-size: 14px;
        font-weight: bold;
    }
    .graficos-lado-lado {
        display: flex;
        justify-content: center;
        gap: 40px;
        margin: 40px 0;
    }
    @media (max-width: 900px) {
        .graficos-lado-lado {
            flex-direction: column;
            align-items: center;
        }
    }
</style>

<div class="main-container">
    <h2 style="text-align:center; margin-top: 30px;">Meu Dashboard</h2>

    <%
        int totalCalorias = request.getAttribute("totalCalorias") != null ? (Integer) request.getAttribute("totalCalorias") : 0;
        int metaCalorias = request.getAttribute("metaCalorias") != null ? (Integer) request.getAttribute("metaCalorias") : 0;
        String aguaStr = request.getAttribute("agua") != null ? request.getAttribute("agua").toString() : "0";
        int metaAgua = request.getAttribute("metaAgua") != null ? (Integer) request.getAttribute("metaAgua") : 0;
        double aguaConsumida = 0;
        try { aguaConsumida = Double.parseDouble(aguaStr.replaceAll("[^\\d.,]", "").replace(",", ".")); } catch(Exception e) { aguaConsumida = 0; } 
    %>

    <div class="dashboard">
        <div class="card">Nivel Fome: <strong><%= request.getAttribute("nivel_fome") %></strong></div>
        <div class="card">Energia: <strong><%= request.getAttribute("energia") %></strong></div>
        <div class="card">Qualidade do Sono: <strong><%= request.getAttribute("sono") %></strong></div>
    </div>

    <!-- Consistência semanal em formato de bolinhas -->
    <div class="consistencia-grid">
        <div class="consistencia-col">
            <div class="consistencia-label">Meta Calorias</div>
            <div>
                <% 
                String[] dias = request.getAttribute("diasSemana") != null ? ((String)request.getAttribute("diasSemana")).replaceAll("[\\[\\]\"]", "").split(",") : new String[]{"Dom","Seg","Ter","Qua","Qui","Sex","Sáb"};
                int[] bateuMetaCaloriasSemana = request.getAttribute("bateuMetaCaloriasSemana") != null ?
                    java.util.Arrays.stream(((String)request.getAttribute("bateuMetaCaloriasSemana")).replaceAll("[\\[\\]]", "").split(",")).mapToInt(s -> s.trim().isEmpty() ? 0 : Integer.parseInt(s.trim())).toArray()
                    : new int[]{0,0,0,0,0,0,0};
                for(int i=0;i<dias.length;i++) {
                    boolean ok = bateuMetaCaloriasSemana.length > i && bateuMetaCaloriasSemana[i] == 1;
                %>
                    <span class="consistencia-dot <%= ok ? "dot-ok" : "dot-nok" %>" title="<%= dias[i] %>"><%= dias[i].trim().substring(0,1) %></span>
                <% } %>
            </div>
        </div>
        <div class="consistencia-col">
            <div class="consistencia-label">Meta Água</div>
            <div>
                <%
                int[] bateuMetaAguaSemana = request.getAttribute("bateuMetaAguaSemana") != null ?
                    java.util.Arrays.stream(((String)request.getAttribute("bateuMetaAguaSemana")).replaceAll("[\\[\\]]", "").split(",")).mapToInt(s -> s.trim().isEmpty() ? 0 : Integer.parseInt(s.trim())).toArray()
                    : new int[]{0,0,0,0,0,0,0};
                for(int i=0;i<dias.length;i++) {
                    boolean ok = bateuMetaAguaSemana.length > i && bateuMetaAguaSemana[i] == 1;
                %>
                    <span class="consistencia-dot <%= ok ? "dot-ok" : "dot-nok" %>" title="<%= dias[i] %>"><%= dias[i].trim().substring(0,1) %></span>
                <% } %>
            </div>
        </div>
    </div>
    <!-- Legenda das cores de consistência -->
    <div style="text-align:center; margin-bottom: 10px;">
        <span class="consistencia-dot dot-ok" style="margin-right:5px;"></span>
        <span style="margin-right:20px;">Cumpriu a meta</span>
        <span class="consistencia-dot dot-nok" style="margin-right:5px;"></span>
        <span>Não cumpriu a meta</span>
    </div>

    <!-- Barras de progresso -->
        <div style="width: 400px; margin: 30px auto 0 auto;">
            <h4>Consumo de Calorias</h4>
            <div class="progress-bar-bg">
                <div id="caloriasBar" class="progress-bar-fill" style="background:#4caf50; width:0%">0%</div>
            </div>
            <div id="caloriasBarInfo" style="text-align:left; font-size:14px; color:#ccc; margin-bottom:8px;"></div>
            <div id="caloriasAviso"></div>
            <h4 style="margin-top:20px;">Consumo de Água</h4>
            <div class="progress-bar-bg">
                <div id="aguaBar" class="progress-bar-fill" style="background:#2196f3; width:0%">0%</div>
            </div>
            <div id="aguaBarInfo" style="text-align:left; font-size:14px; color:#ccc; margin-bottom:8px;"></div>
            <div id="aguaAviso"></div>
        </div>

    <!-- Gráficos lado a lado -->
    <div class="graficos-lado-lado">
        <!-- Gráfico de pizza das calorias por refeição -->
        <div style="width: 340px;">
            <canvas id="pizzaRefeicoes"></canvas>
        </div>
        <!-- Gráfico de ranking de refeições mais calóricas -->
        <div style="width: 340px;">
            <canvas id="rankingRefeicoes"></canvas>
        </div>
    </div>

    <!-- Centraliza o gráfico de evolução semanal -->
    <div style="width: 400px; margin: 0 auto 40px auto; display: flex; justify-content: center;">
        <canvas id="evolucaoCalorias"></canvas>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
<script>
    // Arrays semanais (sempre parse antes de usar!)
    const caloriasSemana = JSON.parse('<%= request.getAttribute("caloriasSemana") != null ? request.getAttribute("caloriasSemana") : "[0,0,0,0,0,0,0]" %>');
    const metaCaloriasSemana = JSON.parse('<%= request.getAttribute("metaCaloriasSemana") != null ? request.getAttribute("metaCaloriasSemana") : "[0,0,0,0,0,0,0]" %>');
    const diasSemana = JSON.parse('<%= request.getAttribute("diasSemana") != null ? request.getAttribute("diasSemana") : "[\"Dom\",\"Seg\",\"Ter\",\"Qua\",\"Qui\",\"Sex\",\"Sáb\"]" %>');

    // Gráfico de pizza das calorias por refeição
    const cafe = <%= request.getAttribute("cafe") != null ? request.getAttribute("cafe") : 0 %>;
    const almoco = <%= request.getAttribute("almoco") != null ? request.getAttribute("almoco") : 0 %>;
    const jantar = <%= request.getAttribute("jantar") != null ? request.getAttribute("jantar") : 0 %>;
    const lanches = <%= request.getAttribute("lanches") != null ? request.getAttribute("lanches") : 0 %>;

    new Chart(document.getElementById('pizzaRefeicoes'), {
        type: 'pie',
        data: {
            labels: ['Café', 'Almoço', 'Jantar', 'Lanches'],
            datasets: [{
                data: [cafe, almoco, jantar, lanches],
                backgroundColor: ['#ffa07a', '#f08080', '#20b2aa', '#87cefa']
            }]
        },
        options: {
            plugins: {
                title: {
                    display: true,
                    text: 'Distribuição de Calorias por Refeição'
                }
            }
        }
    });

    // Gráfico de ranking de refeições mais calóricas (ordem decrescente)
    let nomes = ['Café da manhã', 'Almoço', 'Jantar', 'Lanches'];
    let valores = [cafe, almoco, jantar, lanches];
    // Ordenar para ranking
    for(let i=0;i<valores.length-1;i++) {
        for(let j=i+1;j<valores.length;j++) {
            if(valores[j] > valores[i]) {
                let tmp = valores[i]; valores[i] = valores[j]; valores[j] = tmp;
                let tmpN = nomes[i]; nomes[i] = nomes[j]; nomes[j] = tmpN;
            }
        }
    }
    new Chart(document.getElementById('rankingRefeicoes'), {
        type: 'bar',
        data: {
            labels: nomes,
            datasets: [{
                label: 'Ranking kcal',
                data: valores,
                backgroundColor: ['#f08080', '#ffa07a', '#20b2aa', '#87cefa']
            }]
        },
        options: {
            indexAxis: 'y',
            plugins: {
                title: {
                    display: true,
                    text: 'Ranking de Refeições Mais Calóricas do Dia'
                },
                legend: { display: false }
            },
            scales: {
                x: { beginAtZero: true }
            }
        }
    });

    // 1. Gráfico de evolução semanal de calorias
    new Chart(document.getElementById('evolucaoCalorias'), {
        type: 'line',
        data: {
            labels: diasSemana,
            datasets: [
                {
                    label: 'Calorias Consumidas',
                    data: caloriasSemana,
                    borderColor: '#4caf50',
                    backgroundColor: 'rgba(76,175,80,0.2)',
                    tension: 0.3
                },
                {
                    label: 'Meta Calorias',
                    data: metaCaloriasSemana,
                    borderColor: '#bdbdbd',
                    backgroundColor: 'rgba(189,189,189,0.2)',
                    borderDash: [5,5],
                    tension: 0.3
                }
            ]
        },
        options: {
            plugins: {
                title: {
                    display: true,
                    text: 'Evolução Semanal de Calorias'
                }
            }
        }
    });

    // Barras de progresso
    const totalCalorias = <%= totalCalorias %>;
    const metaCalorias = <%= metaCalorias %>;
    const aguaStr = '<%= aguaStr %>';
    const metaAgua = <%= metaAgua %>;
    let aguaConsumida = 0;
    try {
        aguaConsumida = parseFloat(aguaStr.replace(/[^0-9.,]/g, '').replace(',', '.')) || 0;
    } catch(e) { aguaConsumida = 0; }

    let porcentagemCalorias = metaCalorias > 0 ? (totalCalorias / metaCalorias) * 100 : 0;
    let porcentagemAgua = metaAgua > 0 ? (aguaConsumida / metaAgua) * 100 : 0;
    porcentagemCalorias = Math.round(porcentagemCalorias);
    porcentagemAgua = Math.round(porcentagemAgua);

    const caloriasBar = document.getElementById('caloriasBar');
    const caloriasAviso = document.getElementById('caloriasAviso');
    if (porcentagemCalorias > 100) {
        caloriasBar.style.background = "#e53935";
        caloriasBar.classList.add("red");
        caloriasAviso.innerHTML = "<span style='color:#e53935;font-weight:bold;'>Você ultrapassou sua meta diária de calorias!</span>";
    } else {
        caloriasBar.style.background = "#4caf50";
        caloriasBar.classList.remove("red");
        caloriasAviso.innerHTML = "";
    }
    caloriasBar.style.width = Math.min(100, porcentagemCalorias) + "%";
    caloriasBar.textContent = porcentagemCalorias + "%";
    document.getElementById('caloriasBarInfo').textContent = totalCalorias + " / " + metaCalorias + " kcal";

    const aguaBar = document.getElementById('aguaBar');
    const aguaAviso = document.getElementById('aguaAviso');
    aguaBar.style.width = Math.min(100, porcentagemAgua) + "%";
    aguaBar.textContent = porcentagemAgua + "%";
    document.getElementById('aguaBarInfo').textContent = aguaConsumida + " / " + metaAgua + " L";

    if (porcentagemAgua > 100) {
        aguaBar.style.background = "#e53935";
        aguaBar.classList.add("red");
        aguaAviso.innerHTML = "<span style='color:#e53935;font-weight:bold;'>Você ultrapassou sua meta diária de água!</span>";
    } else if (porcentagemAgua < 100 && metaAgua > 0 && aguaConsumida < metaAgua) {
        aguaBar.style.background = "#2196f3";
        aguaBar.classList.remove("red");
        aguaAviso.innerHTML = "<span style='color:#e53935;font-weight:bold;'>Você ainda não atingiu sua meta de água!</span>";
    } else {
        aguaBar.style.background = "#2196f3";
        aguaBar.classList.remove("red");
        aguaAviso.innerHTML = "";
    }
    aguaBar.style.width = Math.min(100, porcentagemAgua) + "%";
    aguaBar.textContent = porcentagemAgua + "% (" + aguaConsumida + " / " + metaAgua + " L)";
</script>
<%@ include file="WEB-INF/jspf/footer.jspf" %>