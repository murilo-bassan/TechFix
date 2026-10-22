package br.com.techfix.controller;

import br.com.techfix.dao.ClienteDAO;
import br.com.techfix.dao.EquipamentoDAO;
import br.com.techfix.dao.OrdemDeServicoDAO;
import br.com.techfix.model.Atendente;
import br.com.techfix.model.Cliente;
import br.com.techfix.model.Equipamento;
import br.com.techfix.model.OrdemDeServico;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class ControladoraAbrirOrdemServico {

    private final Atendente atendenteLogado;
    private final ClienteDAO clienteDAO;
    private final EquipamentoDAO equipamentoDAO;
    private final OrdemDeServicoDAO ordemDeServicoDAO;

    public ControladoraAbrirOrdemServico(Atendente atendenteLogado, ClienteDAO clienteDAO,
                                         EquipamentoDAO equipamentoDAO, OrdemDeServicoDAO ordemDeServicoDAO) {
        this.atendenteLogado = atendenteLogado;
        this.clienteDAO = clienteDAO;
        this.equipamentoDAO = equipamentoDAO;
        this.ordemDeServicoDAO = ordemDeServicoDAO;
    }

    public Atendente buscarAtendenteLogado() {
        return atendenteLogado;
    }

    public Cliente buscarCliente(String cpfCnpj) throws SQLException {
        exigirPreenchido(cpfCnpj, "Informe o CPF/CNPJ para buscar o cliente");
        return clienteDAO.buscarPorCpfCnpj(somenteDigitos(cpfCnpj));
    }

    public Equipamento buscarEquipamento(String numeroSerie) throws SQLException {
        exigirPreenchido(numeroSerie, "Informe o nº de série para buscar o equipamento");
        return equipamentoDAO.buscarPorNumeroSerie(numeroSerie.trim());
    }

    public Cliente cadastrarCliente(String cpfCnpj, String nome, String telefone) throws SQLException {
        exigirPreenchido(cpfCnpj, "Informe o CPF/CNPJ do cliente");
        exigirPreenchido(nome, "Informe o nome ou razão social do cliente");
        exigirPreenchido(telefone, "Informe o telefone do cliente");

        String documento = somenteDigitos(cpfCnpj);
        if (documento.length() != 11 && documento.length() != 14) {
            throw new IllegalStateException("CPF/CNPJ inválido. Informe 11 dígitos (CPF) ou 14 (CNPJ)");
        }
        if (somenteDigitos(telefone).length() < 10) {
            throw new IllegalStateException("Telefone inválido. Informe DDD e número");
        }

        if (clienteDAO.buscarPorCpfCnpj(documento) != null) {
            throw new IllegalStateException("Já existe cliente cadastrado com este CPF/CNPJ");
        }

        Cliente cliente = new Cliente();
        cliente.setCpfOuCnpj(documento);
        cliente.setTipoPessoa(documento.length() == 11 ? "Física" : "Jurídica");
        cliente.setNomeOuRazaoSocial(nome.trim());
        cliente.setTelefonePrincipal(somenteDigitos(telefone));
        cliente.setClienteFaturado(false);
        cliente.setDataCadastro(LocalDateTime.now());
        clienteDAO.inserir(cliente);
        return cliente;
    }

    public Equipamento cadastrarEquipamento(String numeroSerie, String tipo, String marca, String modelo,
                                            String valorReferencia, String cpfCnpjDono) throws SQLException {
        if (cpfCnpjDono == null || cpfCnpjDono.isBlank()) {
            throw new IllegalStateException("Busque ou cadastre o cliente antes do equipamento");
        }
        Cliente dono = clienteDAO.buscarPorCpfCnpj(somenteDigitos(cpfCnpjDono));
        if (dono == null) {
            throw new IllegalStateException("Busque ou cadastre o cliente antes do equipamento");
        }

        exigirPreenchido(numeroSerie, "Informe o nº de série do equipamento");
        exigirPreenchido(tipo, "Informe o tipo do equipamento");
        exigirPreenchido(marca, "Informe a marca do equipamento");
        exigirPreenchido(modelo, "Informe o modelo do equipamento");

        if (equipamentoDAO.buscarPorNumeroSerie(numeroSerie.trim()) != null) {
            throw new IllegalStateException("Já existe equipamento cadastrado com este nº de série");
        }

        Equipamento equipamento = new Equipamento();
        equipamento.setNumeroSerie(numeroSerie.trim());
        equipamento.setTipo(tipo.trim());
        equipamento.setMarca(marca.trim());
        equipamento.setModelo(modelo.trim());
        equipamento.setValorReferencia(lerValorReferencia(valorReferencia));
        equipamento.setCliente(dono);
        equipamentoDAO.inserir(equipamento);
        return equipamento;
    }

    public boolean possuiOrdemServicoEmAndamento(String numeroSerie) throws SQLException {
        return ordemDeServicoDAO.existeEmAndamento(numeroSerie);
    }

    public OrdemDeServico abrirOrdemServico(String cpfCnpj, String numeroSerie, String defeito,
                                            String checklist, boolean cienciaTaxa) throws SQLException {
        exigirPreenchido(defeito, "Informe o defeito relatado pelo cliente");

        Cliente cliente = clienteDAO.buscarPorCpfCnpj(somenteDigitos(cpfCnpj));
        if (cliente == null) {
            throw new IllegalStateException("Cliente não cadastrado");
        }

        Equipamento equipamento = equipamentoDAO.buscarPorNumeroSerie(numeroSerie.trim());
        if (equipamento == null) {
            throw new IllegalStateException("Equipamento não cadastrado");
        }

        if (equipamento.getCliente() == null
                || !cliente.getCpfOuCnpj().equals(equipamento.getCliente().getCpfOuCnpj())) {
            throw new IllegalStateException("O equipamento informado pertence a outro cliente");
        }

        if (!cienciaTaxa) {
            throw new IllegalStateException("É necessário registrar a ciência da taxa de diagnóstico (RN02)");
        }

        if (ordemDeServicoDAO.existeEmAndamento(equipamento.getNumeroSerie())) {
            throw new IllegalStateException("Equipamento já possui Ordem de Serviço em andamento (RN01)");
        }

        OrdemDeServico os = new OrdemDeServico(equipamento, atendenteLogado, defeito, checklist, cienciaTaxa);
        os.registrarSituacao("Aberta", atendenteLogado);
        ordemDeServicoDAO.inserir(os);

        return os;
    }

    // Campo opcional: vazio vale 0 (não informado). Usado depois na verificação de reparo antieconômico (RN06).
    private double lerValorReferencia(String texto) {
        if (texto == null || texto.isBlank()) {
            return 0.0;
        }
        try {
            double valor = Double.parseDouble(texto.trim().replace(',', '.'));
            if (valor < 0) {
                throw new IllegalStateException("O valor de referência não pode ser negativo");
            }
            return valor;
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Valor de referência inválido");
        }
    }

    private String somenteDigitos(String texto) {
        return texto.replaceAll("\\D", "");
    }

    private void exigirPreenchido(String valor, String mensagem) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException(mensagem);
        }
    }
}
