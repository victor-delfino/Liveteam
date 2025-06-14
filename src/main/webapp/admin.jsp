<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.liveteam.database.DatabaseConnection" %>
<%@ page import="java.sql.SQLException" %>
<%
    int paginaAtual = 1;
    if (request.getParameter("page") != null) {
        try {
            paginaAtual = Integer.parseInt(request.getParameter("page"));
        } catch (NumberFormatException e) {
            paginaAtual = 1;
        }
    }
    int usuariosPorPagina = 5;
    int offset = (paginaAtual - 1) * usuariosPorPagina;
    List<DatabaseConnection.User> usuarios = null;
    int totalUsuarios = 0;
    int totalPaginas = 0;
    String mensagemErro = (String) request.getAttribute("errorMsg");

    // Captura os parâmetros de ordenação
    String sortBy = request.getParameter("sortBy");
    String order = request.getParameter("order");

    try {
        // Chama o método getAllUsers passando os parâmetros de ordenação
        usuarios = DatabaseConnection.getAllUsers(usuariosPorPagina, offset, sortBy, order);
        totalUsuarios = DatabaseConnection.getTotalUserCount();
        totalPaginas = (int) Math.ceil((double) totalUsuarios / usuariosPorPagina);
    } catch (SQLException e) {
        e.printStackTrace();
        mensagemErro = "Erro ao carregar usuários.";
    }
%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <title>Admin</title>
    <link rel="stylesheet" href="assets/css/pages/admin.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.7.2/css/all.min.css" integrity="sha512-Evv84Mr4kqVGRNSgIGL/F/aIDqQb7xQ2vcrdIwxfjThSH8CSR7PBEakCr51Ck+w+/U6swU2Im1vVX0SVk9ABhg==" crossorigin="anonymous" referrerpolicy="no-referrer" />
</head>
<body>
<div class="container">
    <button onclick="voltar()" class="button"><i class="fas fa-chevron-left"></i></button>
    <h1 class="main-heading">Gerenciamento de Usuários</h1>

    <div class="nav">
        <button onclick="showSection('userManagement', event)" class="button active"><i class="fas fa-users"></i></button>
        <button onclick="showSection('addUserForm', event)" class="button"><i class="fas fa-user-plus"></i></button>
    </div>
    <div class="ordering-buttons">
        <button onclick="ordenarPorId('asc')" class="button">ID <i class="fas fa-sort-numeric-down"></i></button>
        <button onclick="ordenarPorId('desc')" class="button">ID <i class="fas fa-sort-numeric-up"></i></button>
        <button onclick="ordenarPorNome('asc')" class="button">Nome <i class="fas fa-sort-alpha-down"></i></button>
        <button onclick="ordenarPorNome('desc')" class="button">Nome <i class="fas fa-sort-alpha-up"></i></button>
    </div>

    <div id="addUserForm" class="section">
        <h2>Adicionar Novo Usuário</h2>
        <form action="${pageContext.request.contextPath}/admin" method="post" class="add-user-form">
            <input type="hidden" name="action" value="addUser">
            <div class="form-group">
                <label for="nome">Nome:</label>
                <input type="text" name="nome" id="nome" required>
            </div>
            <div class="form-group">
                <label for="email">Email:</label>
                <input type="email" name="email" id="email" required>
            </div>
            <div class="form-group">
                <label for="senha">Senha:</label>
                <input type="password" name="senha" id="senha" required>
            </div>
            <div class="form-group">
                <label for="role">Role:</label>
                <select name="role" id="role" required>
                    <option value="usuario">Usuário</option>
                    <option value="administrador">Administrador</option>
                </select>
            </div>
            <button type="submit" class="button">Adicionar</button>
        </form>
    </div>

    <div id="userManagement" class="section active-section">
        <% if (mensagemErro != null) { %>
            <p class="error-message"><%= mensagemErro %></p>
        <% } %>

        <table>
            <thead>
            <tr>
                <th>ID</th>
                <th>Nome</th>
                <th>Role</th>
                <th>Ações</th>
            </tr>
            </thead>
            <tbody>
            <% if (usuarios != null && !usuarios.isEmpty()) {
                for (DatabaseConnection.User usuario : usuarios) { %>
                    <tr>
                        <td><%= usuario.id %></td>
                        <td><%= usuario.nome %></td>
                        <td><%= usuario.role %></td>
                        <td>
                            <button
                                    class="button edit-button"
                                    data-id="<%=usuario.id%>"
                                    data-nome="<%=usuario.nome%>"
                                    data-email="<%=usuario.email%>"
                                    data-role="<%=usuario.role%>">
                                <i class="fas fa-user-edit"></i>
                            </button>

                            <form action="${pageContext.request.contextPath}/admin" method="post">
                                <input type="hidden" name="action" value="deleteUser">
                                <input type="hidden" name="userId" value="<%= usuario.id %>">
                                <button type="submit" class="button"><i class="fas fa-user-times"></i></button>
                            </form>
                        </td>
                    </tr>
                <% }
            } else { %>
                <tr><td colspan="5"><%= usuarios == null ? "Erro ao carregar usuários." : "Nenhum usuário encontrado." %></td></tr>
            <% } %>
            </tbody>
        </table>

<% if (totalPaginas > 1) { %>
            <div class="paginacao">
                <%
                    int botoesVisiveis = 4;
                    int metadeBotoes = botoesVisiveis / 2;
                    int inicio = Math.max(1, paginaAtual - metadeBotoes);
                    int fim = Math.min(totalPaginas, inicio + botoesVisiveis - 1);
                    boolean mostrarInicio = inicio > 1;
                    boolean mostrarFim = fim < totalPaginas;

                    // Ajuste para garantir sempre 'botoesVisiveis' se possível
                    if (totalPaginas <= botoesVisiveis) {
                        inicio = 1;
                        fim = totalPaginas;
                    } else {
                        if (paginaAtual <= metadeBotoes + 1) {
                            fim = botoesVisiveis;
                        } else if (paginaAtual >= totalPaginas - metadeBotoes) {
                            inicio = totalPaginas - botoesVisiveis + 1;
                        }
                    }
                %>

                <% if (mostrarInicio) { %>
                    <button onclick="window.location.href='<%= request.getContextPath() %>/admin?page=1<%= (sortBy != null ? "&sortBy=" + sortBy : "") %><%= (order != null ? "&order=" + order : "") %>'">
                        1
                    </button>
                    <% if (inicio > 2) { %>
                        <span>...</span>
                    <% } %>
                <% } %>

                <% for (int i = inicio; i <= fim; i++) { %>
                    <button class="<%= (i == paginaAtual) ? "ativo" : "" %>"
                            onclick="window.location.href='<%= request.getContextPath() %>/admin?page=<%= i %><%= (sortBy != null ? "&sortBy=" + sortBy : "") %><%= (order != null ? "&order=" + order : "") %>'">
                        <%= i %>
                    </button>
                <% } %>

                <% if (mostrarFim) { %>
                    <% if (fim < totalPaginas - 1) { %>
                        <span>...</span>
                    <% } %>
                    <button onclick="window.location.href='<%= request.getContextPath() %>/admin?page=<%= totalPaginas %><%= (sortBy != null ? "&sortBy=" + sortBy : "") %><%= (order != null ? "&order=" + order : "") %>'">
                        <%= totalPaginas %>
                    </button>
                <% } %>
            </div>
        <% } %>
    </div>
</div>

<div id="editModal" class="modal">
    <div class="modal-content">
        <span class="close">&times;</span>
        <h2>Editar Usuário</h2>
        <form id="editUserForm"
              action="${pageContext.request.contextPath}/admin"
              method="post"
              class="add-user-form edit-user-form">
            <input type="hidden" name="action" value="updateUser">
            <input type="hidden" name="userId" id="editUserId">

            <div class="form-group">
                <label for="editNome">Nome:</label>
                <input type="text" name="nome" id="editNome" required>
            </div>
            <div class="form-group">
                <label for="editEmail">Email:</label>
                <input type="email" name="email" id="editEmail" required>
            </div>
            <div class="form-group">
                <label for="editSenha">Senha:</label>
                <input type="password" name="senha" id="editSenha" placeholder="Deixe em branco para manter">
            </div>
            <div class="form-group">
                <label for="editRole">Role:</label>
                <select name="role" id="editRole" required>
                    <option value="usuario">Usuário</option>
                    <option value="administrador">Administrador</option>
                </select>
            </div>
            <button type="submit" class="button">Atualizar Usuário</button>
        </form>
    </div>
</div>

<script>
    function showSection(sectionId, event) {
        document.querySelectorAll('.section')
            .forEach(s => s.classList.remove('active-section'));
        document.getElementById(sectionId)
            .classList.add('active-section');
        // Alterna o botão ativo
        document.querySelectorAll('.nav .button')
            .forEach(btn => btn.classList.remove('active'));
        if(event && event.currentTarget) {
            event.currentTarget.classList.add('active');
        }

        // Mostra ou esconde os botões de ordenação
        if (sectionId === 'addUserForm') {
            document.querySelector('.ordering-buttons').style.display = 'none';
        } else {
            document.querySelector('.ordering-buttons').style.display = 'flex';
        }
    }
    function voltar(){
        window.location.href = '${pageContext.request.contextPath}/perfil.jsp';
    }

    // Lógica de confirmação para exclusão
    document.querySelectorAll('form[action$="/admin"]').forEach(form => {
        if (form.querySelector('input[name="action"][value="deleteUser"]')) {
            form.addEventListener('submit', function(event) {
                if (!confirm('Tem certeza que deseja excluir este usuário?')) {
                    event.preventDefault(); // Impede o envio do formulário se o usuário cancelar
                }
            });
        }
    });
    // Lógica do Modal (inalterada)
    const editModal = document.getElementById('editModal');
    const closeBtn  = editModal.querySelector('.close');

    document.querySelectorAll('.edit-button').forEach(btn => {
        btn.addEventListener('click', () => {
            document.getElementById('editUserId').value = btn.dataset.id;
            document.getElementById('editNome').value   = btn.dataset.nome;
            document.getElementById('editEmail').value  = btn.dataset.email;
            document.getElementById('editSenha').value  = '';
            document.getElementById('editRole').value   = btn.dataset.role;
            editModal.style.display = 'flex';
        });
    });

    closeBtn.addEventListener('click', () => {
        editModal.style.display = 'none';
    });
    window.addEventListener('click', e => {
        if (e.target === editModal) editModal.style.display = 'none';
    });
    function ordenarPorId(ordem) {
        window.location.href = '<%= request.getContextPath() %>/admin?sortBy=id&order=' + ordem + '&page=<%= paginaAtual %>';
    }

    function ordenarPorNome(ordem) {
        window.location.href = '<%= request.getContextPath() %>/admin?sortBy=nome&order=' + ordem + '&page=<%= paginaAtual %>';
    }

    // Esconde ordering-buttons se a tela inicia no modo de adicionar usuário
    window.addEventListener('DOMContentLoaded', function () {
        if (document.getElementById('addUserForm').classList.contains('active-section')) {
            document.querySelector('.ordering-buttons').style.display = 'none';
        }
    });
</script>
</body>
</html>