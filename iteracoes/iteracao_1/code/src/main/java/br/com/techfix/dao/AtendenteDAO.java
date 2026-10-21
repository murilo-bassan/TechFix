package br.com.techfix.dao;

import br.com.techfix.model.Atendente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class AtendenteDAO {

    public void inserir(Atendente atendente) throws SQLException {
        String sql = "INSERT INTO Atendente (nome, login, senha, situacao) VALUES (?, ?, ?, ?)";
        try (Connection conexao = Conexao.abrir();
             PreparedStatement ps = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, atendente.getNome());
            ps.setString(2, atendente.getLogin());
            ps.setString(3, atendente.getSenha());
            ps.setInt(4, atendente.isSituacao() ? 1 : 0);
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                chaves.next();
                atendente.setId(chaves.getLong(1));
            }
        }
    }

    public Atendente buscarPorLogin(String login) throws SQLException {
        String sql = "SELECT * FROM Atendente WHERE login = ?";
        try (Connection conexao = Conexao.abrir();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, login);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Atendente atendente = new Atendente();
                atendente.setId(rs.getLong("id"));
                atendente.setNome(rs.getString("nome"));
                atendente.setLogin(rs.getString("login"));
                atendente.setSenha(rs.getString("senha"));
                atendente.setPerfilAcesso("Atendente");
                atendente.setSituacao(rs.getInt("situacao") == 1);
                return atendente;
            }
        }
    }
}
