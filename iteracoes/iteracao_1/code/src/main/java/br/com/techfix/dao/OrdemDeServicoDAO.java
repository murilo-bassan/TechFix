package br.com.techfix.dao;

import br.com.techfix.model.HistoricoDeSituacao;
import br.com.techfix.model.OrdemDeServico;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class OrdemDeServicoDAO {

    public void inserir(OrdemDeServico os) throws SQLException {
        try (Connection conexao = Conexao.abrir()) {
            conexao.setAutoCommit(false);
            try {
                inserirOrdem(conexao, os);
                for (HistoricoDeSituacao historico : os.getHistoricos()) {
                    inserirHistorico(conexao, historico);
                }
                conexao.commit();
            } catch (SQLException e) {
                conexao.rollback();
                throw e;
            }
        }
    }

    public boolean existeEmAndamento(String numeroSerie) throws SQLException {
        String sql = "SELECT COUNT(*) FROM OrdemDeServico "
            + "WHERE numeroSerieEquipamento = ? AND situacao NOT IN ('Entregue', 'Cancelada')";
        try (Connection conexao = Conexao.abrir();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, numeroSerie);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    private void inserirOrdem(Connection conexao, OrdemDeServico os) throws SQLException {
        String sql = "INSERT INTO OrdemDeServico (dataHoraEntrada, defeitoRelatado, checklistAcessorios, "
            + "situacao, cienciaTaxaDiagnostico, numeroSerieEquipamento, idAtendente) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, os.getDataHoraEntrada().toString());
            ps.setString(2, os.getDefeitoRelatado());
            ps.setString(3, os.getChecklistAcessorios());
            ps.setString(4, os.getSituacao());
            ps.setInt(5, os.isCienciaTaxaDiagnostico() ? 1 : 0);
            ps.setString(6, os.getEquipamento().getNumeroSerie());
            ps.setLong(7, os.getAtendente().getId());
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                chaves.next();
                os.setNumero(chaves.getLong(1));
            }
        }
    }

    private void inserirHistorico(Connection conexao, HistoricoDeSituacao historico) throws SQLException {
        String sql = "INSERT INTO HistoricoDeSituacao (situacao, dataHora, numeroOrdemDeServico, idAtendente) "
            + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, historico.getSituacao());
            ps.setString(2, historico.getDataHora().toString());
            ps.setLong(3, historico.getOrdemDeServico().getNumero());
            ps.setLong(4, historico.getUsuario().getId());
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                chaves.next();
                historico.setId(chaves.getLong(1));
            }
        }
    }
}
