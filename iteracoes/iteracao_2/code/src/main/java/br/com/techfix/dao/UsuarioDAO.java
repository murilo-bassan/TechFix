package br.com.techfix.dao;

import br.com.techfix.model.Atendente;
import br.com.techfix.model.Gerente;
import br.com.techfix.model.Tecnico;
import br.com.techfix.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UsuarioDAO {

    public void inserir(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO Usuario (nome, login, senha, perfilAcesso, situacao) VALUES (?, ?, ?, ?, ?)";
        try (Connection conexao = Conexao.abrir();
             PreparedStatement ps = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, usuario.getNome());
            ps.setString(2, usuario.getLogin());
            ps.setString(3, usuario.getSenha());
            ps.setString(4, usuario.getPerfilAcesso());
            ps.setInt(5, usuario.isSituacao() ? 1 : 0);
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                chaves.next();
                usuario.setId(chaves.getLong(1));
            }
        }
    }

    public Usuario buscarPorLogin(String login) throws SQLException {
        return buscar("SELECT * FROM Usuario WHERE login = ?", login);
    }

    public Usuario buscarPorId(long id) throws SQLException {
        return buscar("SELECT * FROM Usuario WHERE id = ?", id);
    }

    private Usuario buscar(String sql, Object parametro) throws SQLException {
        try (Connection conexao = Conexao.abrir();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setObject(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return montarUsuario(rs);
            }
        }
    }

    private Usuario montarUsuario(ResultSet rs) throws SQLException {
        String perfil = rs.getString("perfilAcesso");
        Usuario usuario;
        switch (perfil) {
            case "Atendente":
                usuario = new Atendente();
                break;
            case "Tecnico":
                usuario = new Tecnico();
                break;
            case "Gerente":
                usuario = new Gerente();
                break;
            default:
                throw new IllegalStateException("Perfil de acesso desconhecido: " + perfil);
        }
        usuario.setId(rs.getLong("id"));
        usuario.setNome(rs.getString("nome"));
        usuario.setLogin(rs.getString("login"));
        usuario.setSenha(rs.getString("senha"));
        usuario.setPerfilAcesso(perfil);
        usuario.setSituacao(rs.getInt("situacao") == 1);
        return usuario;
    }
}
