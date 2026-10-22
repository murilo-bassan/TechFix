package br.com.techfix.dao;

import br.com.techfix.model.Peca;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PecaDAO {

    public void inserir(Peca peca) throws SQLException {
        String sql = "INSERT INTO Peca (codigo, descricao, categoria, precoCusto, precoVenda, "
            + "quantidadeEstoque, quantidadeMinimaEstoque) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conexao = Conexao.abrir();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, peca.getCodigo());
            ps.setString(2, peca.getDescricao());
            ps.setString(3, peca.getCategoria());
            ps.setDouble(4, peca.getPrecoCusto());
            ps.setDouble(5, peca.getPrecoVenda());
            ps.setInt(6, peca.getQuantidadeEstoque());
            ps.setInt(7, peca.getQuantidadeMinimaEstoque());
            ps.executeUpdate();
        }
    }

    public boolean existeAlguma() throws SQLException {
        try (Connection conexao = Conexao.abrir();
             Statement st = conexao.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM Peca")) {
            rs.next();
            return rs.getInt(1) > 0;
        }
    }

    public Peca buscarPorCodigo(String codigo) throws SQLException {
        String sql = "SELECT * FROM Peca WHERE codigo = ?";
        try (Connection conexao = Conexao.abrir();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? montarPeca(rs) : null;
            }
        }
    }

    public List<Peca> listar() throws SQLException {
        List<Peca> pecas = new ArrayList<>();
        try (Connection conexao = Conexao.abrir();
             Statement st = conexao.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM Peca ORDER BY descricao")) {
            while (rs.next()) {
                pecas.add(montarPeca(rs));
            }
        }
        return pecas;
    }

    private Peca montarPeca(ResultSet rs) throws SQLException {
        Peca peca = new Peca();
        peca.setCodigo(rs.getString("codigo"));
        peca.setDescricao(rs.getString("descricao"));
        peca.setCategoria(rs.getString("categoria"));
        peca.setPrecoCusto(rs.getDouble("precoCusto"));
        peca.setPrecoVenda(rs.getDouble("precoVenda"));
        peca.setQuantidadeEstoque(rs.getInt("quantidadeEstoque"));
        peca.setQuantidadeMinimaEstoque(rs.getInt("quantidadeMinimaEstoque"));
        return peca;
    }
}
