package br.com.techfix.dao;

import br.com.techfix.model.Cotacao;
import br.com.techfix.model.ItemCotacaoPeca;
import br.com.techfix.model.OrdemDeServico;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class CotacaoDAO {

    private final OrdemDeServicoDAO ordemDeServicoDAO = new OrdemDeServicoDAO();

    // Grava a cotação, seus itens, a substituição da cotação pendente (se houver)
    // e a mudança de situação da OS na mesma transação.
    public void registrar(Cotacao cotacao, long idCotacaoSubstituida) throws SQLException {
        try (Connection conexao = Conexao.abrir()) {
            conexao.setAutoCommit(false);
            try {
                if (idCotacaoSubstituida != 0) {
                    atualizarSituacao(conexao, idCotacaoSubstituida, Cotacao.SUBSTITUIDA);
                }
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

    // Retorna o id da cotação pendente da OS, ou 0 se não houver.
    public long buscarIdPendente(long numeroOrdemDeServico) throws SQLException {
        String sql = "SELECT id FROM Cotacao WHERE numeroOrdemDeServico = ? AND situacao = ?";
        try (Connection conexao = Conexao.abrir();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, numeroOrdemDeServico);
            ps.setString(2, Cotacao.PENDENTE);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong("id") : 0;
            }
        }
    }

    // RN05: cotação pendente cujo prazo de validade passou assume a situação Expirada,
    // e a OS que aguardava a aprovação dela passa para Expirada. A mudança é feita pelo
    // sistema, por isso o histórico fica sem usuário.
    public void expirarVencidas() throws SQLException {
        LocalDateTime agora = LocalDateTime.now();
        Map<Long, Long> vencidas = new LinkedHashMap<>();
        String consulta = "SELECT id, prazoValidade, numeroOrdemDeServico FROM Cotacao WHERE situacao = ?";
        try (Connection conexao = Conexao.abrir();
             PreparedStatement ps = conexao.prepareStatement(consulta)) {
            ps.setString(1, Cotacao.PENDENTE);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (agora.isAfter(LocalDateTime.parse(rs.getString("prazoValidade")))) {
                        vencidas.put(rs.getLong("id"), rs.getLong("numeroOrdemDeServico"));
                    }
                }
            }
        }
        if (vencidas.isEmpty()) {
            return;
        }

        List<OrdemDeServico> ordens = new ArrayList<>();
        for (long numero : new LinkedHashSet<>(vencidas.values())) {
            OrdemDeServico os = ordemDeServicoDAO.buscarPorNumero(numero);
            if (os != null && "Aguardando Aprovação".equals(os.getSituacao())) {
                os.registrarSituacao("Expirada", null);
                ordens.add(os);
            }
        }

        try (Connection conexao = Conexao.abrir()) {
            conexao.setAutoCommit(false);
            try {
                for (long id : vencidas.keySet()) {
                    atualizarSituacao(conexao, id, Cotacao.EXPIRADA);
                }
                for (OrdemDeServico os : ordens) {
                    ordemDeServicoDAO.atualizarSituacao(conexao, os);
                }
                conexao.commit();
            } catch (SQLException e) {
                conexao.rollback();
                throw e;
            }
        }
    }

    private void atualizarSituacao(Connection conexao, long id, String situacao) throws SQLException {
        String sql = "UPDATE Cotacao SET situacao = ? WHERE id = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, situacao);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    private void inserirCotacao(Connection conexao, Cotacao cotacao) throws SQLException {
        String sql = "INSERT INTO Cotacao (diagnosticoTecnico, valorMaoDeObra, prazoExecucaoDias, valorTotal, "
            + "dataRegistro, prazoValidade, situacao, reparoAntieconomico, "
            + "numeroOrdemDeServico, idTecnico) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cotacao.getDiagnosticoTecnico());
            ps.setDouble(2, cotacao.getValorMaoDeObra());
            ps.setInt(3, cotacao.getPrazoExecucaoDias());
            ps.setDouble(4, cotacao.getValorTotal());
            ps.setString(5, cotacao.getDataRegistro().toString());
            ps.setString(6, cotacao.getPrazoValidade().toString());
            ps.setString(7, cotacao.getSituacao());
            ps.setInt(8, cotacao.isReparoAntieconomico() ? 1 : 0);
            ps.setLong(9, cotacao.getOrdemDeServico().getNumero());
            ps.setLong(10, cotacao.getTecnico().getId());
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
