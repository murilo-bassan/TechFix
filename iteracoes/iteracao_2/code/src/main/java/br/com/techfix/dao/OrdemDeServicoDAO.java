package br.com.techfix.dao;

import br.com.techfix.model.Atendente;
import br.com.techfix.model.Equipamento;
import br.com.techfix.model.HistoricoDeSituacao;
import br.com.techfix.model.OrdemDeServico;
import br.com.techfix.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

public class OrdemDeServicoDAO {

    private final EquipamentoDAO equipamentoDAO = new EquipamentoDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public void inserir(OrdemDeServico os) throws SQLException {
        try (Connection conexao = Conexao.abrir()) {
            conexao.setAutoCommit(false);
            try {
                inserirOrdem(conexao, os);
                inserirHistoricosNovos(conexao, os);
                conexao.commit();
            } catch (SQLException e) {
                conexao.rollback();
                throw e;
            }
        }
    }

    public OrdemDeServico buscarPorNumero(long numero) throws SQLException {
        String sql = "SELECT * FROM OrdemDeServico WHERE numero = ?";
        try (Connection conexao = Conexao.abrir();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, numero);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return montarOrdem(rs);
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

    // Participa da transação de quem chama; não faz commit nem rollback.
    public void atualizarSituacao(Connection conexao, OrdemDeServico os) throws SQLException {
        String sql = "UPDATE OrdemDeServico SET situacao = ? WHERE numero = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, os.getSituacao());
            ps.setLong(2, os.getNumero());
            ps.executeUpdate();
        }
        inserirHistoricosNovos(conexao, os);
    }

    private OrdemDeServico montarOrdem(ResultSet rs) throws SQLException {
        Equipamento equipamento = equipamentoDAO.buscarPorNumeroSerie(rs.getString("numeroSerieEquipamento"));
        Usuario usuario = usuarioDAO.buscarPorId(rs.getLong("idAtendente"));
        if (!(usuario instanceof Atendente)) {
            throw new IllegalStateException("A Ordem de Serviço " + rs.getLong("numero")
                + " não foi aberta por um Atendente");
        }
        return new OrdemDeServico(
            rs.getLong("numero"),
            LocalDateTime.parse(rs.getString("dataHoraEntrada")),
            equipamento,
            (Atendente) usuario,
            rs.getString("defeitoRelatado"),
            rs.getString("checklistAcessorios"),
            rs.getString("situacao"),
            rs.getInt("cienciaTaxaDiagnostico") == 1);
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

    private void inserirHistoricosNovos(Connection conexao, OrdemDeServico os) throws SQLException {
        for (HistoricoDeSituacao historico : os.getHistoricos()) {
            if (historico.getId() == 0) {
                inserirHistorico(conexao, historico);
            }
        }
    }

    private void inserirHistorico(Connection conexao, HistoricoDeSituacao historico) throws SQLException {
        String sql = "INSERT INTO HistoricoDeSituacao (situacao, dataHora, numeroOrdemDeServico, idUsuario) "
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
