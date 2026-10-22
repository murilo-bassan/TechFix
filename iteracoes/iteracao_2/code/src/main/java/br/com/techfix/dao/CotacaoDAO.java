package br.com.techfix.dao;

import br.com.techfix.model.Cotacao;
import br.com.techfix.model.ItemCotacaoPeca;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CotacaoDAO {

    private final OrdemDeServicoDAO ordemDeServicoDAO = new OrdemDeServicoDAO();

    // Grava a cotação, seus itens e a mudança de situação da OS na mesma transação.
    public void registrar(Cotacao cotacao) throws SQLException {
        try (Connection conexao = Conexao.abrir()) {
            conexao.setAutoCommit(false);
            try {
                inserirCotacao(conexao, cotacao);
                for (ItemCotacaoPeca item : cotacao.getItens()) {
                    inserirItem(conexao, cotacao, item);
                }
                ordemDeServicoDAO.atualizarSituacao(conexao, cotacao.getOrdemDeServico());
                conexao.commit();
            } catch (SQLException e) {
                conexao.rollback();
                throw e;
            }
        }
    }

    public boolean existeVigente(long numeroOrdemDeServico) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Cotacao WHERE numeroOrdemDeServico = ? AND situacao IN (?, ?)";
        try (Connection conexao = Conexao.abrir();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, numeroOrdemDeServico);
            ps.setString(2, Cotacao.PENDENTE);
            ps.setString(3, Cotacao.APROVADA);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    // RN05: cotação pendente cujo prazo de validade passou assume a situação Expirada.
    public void expirarVencidas() throws SQLException {
        LocalDateTime agora = LocalDateTime.now();
        List<Long> vencidas = new ArrayList<>();
        try (Connection conexao = Conexao.abrir()) {
            String consulta = "SELECT id, prazoValidade FROM Cotacao WHERE situacao = ?";
            try (PreparedStatement ps = conexao.prepareStatement(consulta)) {
                ps.setString(1, Cotacao.PENDENTE);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        if (agora.isAfter(LocalDateTime.parse(rs.getString("prazoValidade")))) {
                            vencidas.add(rs.getLong("id"));
                        }
                    }
                }
            }
            String atualizacao = "UPDATE Cotacao SET situacao = ? WHERE id = ?";
            try (PreparedStatement ps = conexao.prepareStatement(atualizacao)) {
                for (long id : vencidas) {
                    ps.setString(1, Cotacao.EXPIRADA);
                    ps.setLong(2, id);
                    ps.executeUpdate();
                }
            }
        }
    }

    private void inserirCotacao(Connection conexao, Cotacao cotacao) throws SQLException {
        String sql = "INSERT INTO Cotacao (diagnosticoTecnico, valorMaoDeObra, prazoExecucaoDias, valorTotal, "
            + "dataRegistro, prazoValidade, situacao, reparoAntieconomico, cienciaAntieconomico, "
            + "numeroOrdemDeServico, idTecnico) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cotacao.getDiagnosticoTecnico());
            ps.setDouble(2, cotacao.getValorMaoDeObra());
            ps.setInt(3, cotacao.getPrazoExecucaoDias());
            ps.setDouble(4, cotacao.getValorTotal());
            ps.setString(5, cotacao.getDataRegistro().toString());
            ps.setString(6, cotacao.getPrazoValidade().toString());
            ps.setString(7, cotacao.getSituacao());
            ps.setInt(8, cotacao.isReparoAntieconomico() ? 1 : 0);
            ps.setInt(9, cotacao.isCienciaAntieconomico() ? 1 : 0);
            ps.setLong(10, cotacao.getOrdemDeServico().getNumero());
            ps.setLong(11, cotacao.getTecnico().getId());
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                chaves.next();
                cotacao.setId(chaves.getLong(1));
            }
        }
    }

    private void inserirItem(Connection conexao, Cotacao cotacao, ItemCotacaoPeca item) throws SQLException {
        String sql = "INSERT INTO ItemCotacaoPeca (quantidade, valorUnitario, idCotacao, codigoPeca) "
            + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setInt(1, item.getQuantidade());
            ps.setDouble(2, item.getValorUnitario());
            ps.setLong(3, cotacao.getId());
            ps.setString(4, item.getPeca().getCodigo());
            ps.executeUpdate();
        }
    }
}
