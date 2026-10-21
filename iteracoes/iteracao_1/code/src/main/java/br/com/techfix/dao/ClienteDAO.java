package br.com.techfix.dao;

import br.com.techfix.model.Cliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class ClienteDAO {

    public void inserir(Cliente cliente) throws SQLException {
        String sql = "INSERT INTO Cliente (cpfOuCnpj, tipoPessoa, nomeOuRazaoSocial, telefonePrincipal, "
            + "telefoneAlternativo, enderecoEmail, endereco, clienteFaturado, dataCadastro) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conexao = Conexao.abrir();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, cliente.getCpfOuCnpj());
            ps.setString(2, cliente.getTipoPessoa());
            ps.setString(3, cliente.getNomeOuRazaoSocial());
            ps.setString(4, cliente.getTelefonePrincipal());
            ps.setString(5, cliente.getTelefoneAlternativo());
            ps.setString(6, cliente.getEnderecoEmail());
            ps.setString(7, cliente.getEndereco());
            ps.setInt(8, cliente.isClienteFaturado() ? 1 : 0);
            ps.setString(9, cliente.getDataCadastro().toString());
            ps.executeUpdate();
        }
    }

    public Cliente buscarPorCpfCnpj(String cpfCnpj) throws SQLException {
        String sql = "SELECT * FROM Cliente WHERE cpfOuCnpj = ?";
        try (Connection conexao = Conexao.abrir();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, cpfCnpj);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return montarCliente(rs);
            }
        }
    }

    private Cliente montarCliente(ResultSet rs) throws SQLException {
        Cliente cliente = new Cliente();
        cliente.setTipoPessoa(rs.getString("tipoPessoa"));
        cliente.setNomeOuRazaoSocial(rs.getString("nomeOuRazaoSocial"));
        cliente.setCpfOuCnpj(rs.getString("cpfOuCnpj"));
        cliente.setTelefonePrincipal(rs.getString("telefonePrincipal"));
        cliente.setTelefoneAlternativo(rs.getString("telefoneAlternativo"));
        cliente.setEnderecoEmail(rs.getString("enderecoEmail"));
        cliente.setEndereco(rs.getString("endereco"));
        cliente.setClienteFaturado(rs.getInt("clienteFaturado") == 1);
        cliente.setDataCadastro(LocalDateTime.parse(rs.getString("dataCadastro")));
        return cliente;
    }
}
