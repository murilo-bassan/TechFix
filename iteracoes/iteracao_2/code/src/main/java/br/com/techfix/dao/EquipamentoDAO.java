package br.com.techfix.dao;

import br.com.techfix.model.Equipamento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EquipamentoDAO {

    private final ClienteDAO clienteDAO = new ClienteDAO();

    public void inserir(Equipamento equipamento) throws SQLException {
        String sql = "INSERT INTO Equipamento (numeroSerie, tipo, marca, modelo, valorReferencia, "
            + "observacoes, cpfOuCnpjCliente) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conexao = Conexao.abrir();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, equipamento.getNumeroSerie());
            ps.setString(2, equipamento.getTipo());
            ps.setString(3, equipamento.getMarca());
            ps.setString(4, equipamento.getModelo());
            ps.setDouble(5, equipamento.getValorReferencia());
            ps.setString(6, equipamento.getObservacoes());
            ps.setString(7, equipamento.getCliente().getCpfOuCnpj());
            ps.executeUpdate();
        }
    }

    public Equipamento buscarPorNumeroSerie(String numeroSerie) throws SQLException {
        String sql = "SELECT * FROM Equipamento WHERE numeroSerie = ?";
        try (Connection conexao = Conexao.abrir();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, numeroSerie);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Equipamento equipamento = new Equipamento();
                equipamento.setNumeroSerie(rs.getString("numeroSerie"));
                equipamento.setTipo(rs.getString("tipo"));
                equipamento.setMarca(rs.getString("marca"));
                equipamento.setModelo(rs.getString("modelo"));
                equipamento.setValorReferencia(rs.getDouble("valorReferencia"));
                equipamento.setObservacoes(rs.getString("observacoes"));
                equipamento.setCliente(clienteDAO.buscarPorCpfCnpj(rs.getString("cpfOuCnpjCliente")));
                return equipamento;
            }
        }
    }
}
