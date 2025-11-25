package com.liveteam.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class Tabelas {

    public static void main(String[] args) {
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {

            // Tabela de usuários
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS usuario (" +
                "id SERIAL PRIMARY KEY, " +
                "nome VARCHAR(50) NOT NULL, " +
                "email VARCHAR(100) NOT NULL UNIQUE, " +
                "senha VARCHAR(255) NOT NULL, " +
                "role VARCHAR(20) NOT NULL DEFAULT 'usuario', " +
                "idade INTEGER, " +
                "altura_cm NUMERIC(5,2), " +
                "peso_kg NUMERIC(5,2))"
            );
            System.out.println("Tabela 'usuario' criada com sucesso!");

            // Tabela de dados diários
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS dados_diarios (" +
                    "id SERIAL PRIMARY KEY, " +
                    "id_usuario INT NOT NULL, " +
                    "dia INT NOT NULL, " +
                    "mes INT NOT NULL, " +
                    "ano INT NOT NULL, " +
                    "cafe_da_manha TEXT, " +
                    "cafe_da_manha_calorias INT, " +
                    "almoco TEXT, " +
                    "almoco_calorias INT, " +
                    "jantar TEXT, " +
                    "jantar_calorias INT, " +
                    "lanches TEXT, " +
                    "lanches_calorias INT, " +
                    "observacoes_alimentacao TEXT, " +
                    "agua VARCHAR(255), " +
                    "outros_liquidos VARCHAR(255), " +
                    "observacoes_liquidos TEXT, " +
                    "tipo_treino VARCHAR(255), " +
                    "duracao_treino VARCHAR(255), " +
                    "intensidade_treino VARCHAR(255), " +
                    "detalhes_exercicio TEXT, " +
                    "observacoes_exercicio TEXT, " +
                    "nivel_fome VARCHAR(255), " +
                    "nivel_energia VARCHAR(255), " +
                    "qualidade_sono VARCHAR(255), " +
                    "observacoes_avaliacao TEXT, " +
                    "FOREIGN KEY (id_usuario) REFERENCES usuario(id) ON DELETE CASCADE" +
                    ")");


            System.out.println("Tabela 'dados_diarios' criada com sucesso!");

            // Tabela plano
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS plano (" +
                    "id SERIAL PRIMARY KEY, " +
                    "id_usuario INT NOT NULL, " +
                    "data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (id_usuario) REFERENCES usuario(id) ON DELETE CASCADE)");

            // Tabela dieta
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS dieta (" +
                        "id SERIAL PRIMARY KEY, " +
                        "plano_id INT NOT NULL, " +
                        "objetivo TEXT NOT NULL, " +
                        "calorias_totais INT, " +
                        "observacoes TEXT, " +
                        "meta_agua INT, " + // <-- campo adicionado
                        "FOREIGN KEY (plano_id) REFERENCES plano(id) ON DELETE CASCADE)");

            // Tabela macronutrientes
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS macronutrientes (" +
                    "id SERIAL PRIMARY KEY, " +
                    "dieta_id INT NOT NULL, " +
                    "proteinas INT, " +
                    "carboidratos INT, " +
                    "gorduras INT, " +
                    "FOREIGN KEY (dieta_id) REFERENCES dieta(id) ON DELETE CASCADE)");

            // Tabela refeicoes
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS refeicoes (" +
                    "id SERIAL PRIMARY KEY, " +
                    "dieta_id INT NOT NULL, " +
                    "cafe_da_manha TEXT, " +
                    "almoco TEXT, " +
                    "lanche_tarde TEXT, " +
                    "jantar TEXT, " +
                    "FOREIGN KEY (dieta_id) REFERENCES dieta(id) ON DELETE CASCADE)");

            // Tabela treino
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS treino (" +
                    "id SERIAL PRIMARY KEY, " +
                    "plano_id INT NOT NULL, " +
                    "divisao TEXT, " +
                    "justificativa_divisao TEXT, " +
                    "observacoes TEXT, " +
                    "FOREIGN KEY (plano_id) REFERENCES plano(id) ON DELETE CASCADE)");

            // Tabela subtreino
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS subtreino (" +
                    "id SERIAL PRIMARY KEY, " +
                    "treino_id INT NOT NULL, " +
                    "nome VARCHAR(10), " +
                    "foco TEXT, " +
                    "FOREIGN KEY (treino_id) REFERENCES treino(id) ON DELETE CASCADE)");

            // Tabela exercicio
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS exercicio (" +
                    "id SERIAL PRIMARY KEY, " +
                    "subtreino_id INT NOT NULL, " +
                    "nome TEXT, " +
                    "series VARCHAR(30), " +
                    "repeticoes VARCHAR(30), " +
                    "FOREIGN KEY (subtreino_id) REFERENCES subtreino(id) ON DELETE CASCADE)");

            System.out.println("Tabelas de plano, dieta e treino criadas com sucesso!");

            // Tabela de tokens de redefinição de senha
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS password_reset_tokens (" +
                    "user_id INT REFERENCES usuario(id) ON DELETE CASCADE, " +
                    "token VARCHAR(128) PRIMARY KEY, " +
                    "expires_at TIMESTAMP NOT NULL)");
            System.out.println("Tabela 'password_reset_tokens' criada com sucesso!");

        } catch (SQLException e) {
            System.err.println("Erro ao executar comandos SQL: " + e.getMessage());
        } finally {
            DatabaseConnection.closeConnection();
        }
    }
}
