package br.com.techfix.controller;

import br.com.techfix.dao.CotacaoDAO;
import br.com.techfix.dao.OrdemDeServicoDAO;
import br.com.techfix.dao.PecaDAO;
import br.com.techfix.model.Cotacao;
import br.com.techfix.model.Gerente;
import br.com.techfix.model.ItemCotacaoPeca;
import br.com.techfix.model.OrdemDeServico;
import br.com.techfix.model.Peca;
import br.com.techfix.model.Tecnico;
import br.com.techfix.model.Usuario;

import java.sql.SQLException;
import java.util.List;

public class ControladoraRegistrarCotacao {

    private final Usuario usuarioLogado;
    private final OrdemDeServicoDAO ordemDeServicoDAO;
    private final CotacaoDAO cotacaoDAO;
    private final PecaDAO pecaDAO;

    public ControladoraRegistrarCotacao(Usuario usuarioLogado, OrdemDeServicoDAO ordemDeServicoDAO,
                                        CotacaoDAO cotacaoDAO, PecaDAO pecaDAO) {
        this.usuarioLogado = usuarioLogado;
        this.ordemDeServicoDAO = ordemDeServicoDAO;
        this.cotacaoDAO = cotacaoDAO;
        this.pecaDAO = pecaDAO;
    }

    public Usuario buscarUsuarioLogado() {
        return usuarioLogado;
    }

    public OrdemDeServico buscarOrdemServico(long numero) throws SQLException {
        exigirPermissao();
        cotacaoDAO.expirarVencidas();
        return ordemDeServicoDAO.buscarPorNumero(numero);
    }

    // Retorna o número da cotação pendente da OS, ou 0 se não houver.
    public long buscarCotacaoPendente(long numeroOrdemServico) throws SQLException {
        return cotacaoDAO.buscarIdPendente(numeroOrdemServico);
    }

    public List<Peca> listarPecas() throws SQLException {
        return pecaDAO.listar();
    }

    public boolean isReparoAntieconomico(OrdemDeServico os, double valorTotal) {
        return Cotacao.excedeLimiteAntieconomico(valorTotal, os.getEquipamento().getValorReferencia());
    }

    public Cotacao registrarCotacao(long numeroOrdemServico, String diagnostico, List<ItemCotacaoPeca> itens,
                                    double valorMaoDeObra, int prazoExecucaoDias,
                                    boolean substituirPendente) throws SQLException {
        exigirPermissao();
        cotacaoDAO.expirarVencidas();

        OrdemDeServico os = ordemDeServicoDAO.buscarPorNumero(numeroOrdemServico);
        if (os == null) {
            throw new IllegalStateException("Ordem de Serviço não encontrada");
        }
        exigirOrdemApta(os);

        // A nova cotação substitui a pendente; a anterior é mantida com a situação Substituída.
        long idPendente = cotacaoDAO.buscarIdPendente(os.getNumero());
        if (idPendente != 0 && !substituirPendente) {
            throw new IllegalStateException("A OS já possui a cotação nº " + idPendente
                + " pendente. Confirme a substituição para registrar a nova cotação");
        }

        exigirPreenchido(diagnostico, "Informe o diagnóstico técnico");
        if (valorMaoDeObra < 0) {
            throw new IllegalStateException("O valor da mão de obra não pode ser negativo");
        }
        if (prazoExecucaoDias <= 0) {
            throw new IllegalStateException("Informe o prazo estimado de execução (em dias)");
        }

        Cotacao cotacao = new Cotacao(os, usuarioLogado, diagnostico.trim(), valorMaoDeObra, prazoExecucaoDias);
        for (ItemCotacaoPeca solicitado : itens) {
            cotacao.adicionarItem(montarItem(solicitado));
        }
        if (cotacao.getValorTotal() <= 0) {
            throw new IllegalStateException("O valor total da cotação deve ser maior que zero");
        }

        // RN06: o UC08 apenas sinaliza o reparo antieconômico; a ciência do cliente é registrada no UC09.
        cotacao.setReparoAntieconomico(isReparoAntieconomico(os, cotacao.getValorTotal()));

        if ("Aberta".equals(os.getSituacao())) {
            os.registrarSituacao("Em Diagnóstico", usuarioLogado);
        }
        // Na substituição a OS já está Aguardando Aprovação e não muda de situação.
        if (!"Aguardando Aprovação".equals(os.getSituacao())) {
            os.registrarSituacao("Aguardando Aprovação", usuarioLogado);
        }

        cotacaoDAO.registrar(cotacao, idPendente);
        return cotacao;
    }

    // RN04: somente Técnico ou Gerente registram cotação.
    private void exigirPermissao() {
        if (!(usuarioLogado instanceof Tecnico) && !(usuarioLogado instanceof Gerente)) {
            throw new IllegalStateException("Somente Técnico ou Gerente podem registrar cotação (RN04)");
        }
    }

    // RN07: OS com cotação expirada aceita nova cotação.
    private void exigirOrdemApta(OrdemDeServico os) {
        String situacao = os.getSituacao();
        boolean situacaoValida = "Aberta".equals(situacao)
            || "Em Diagnóstico".equals(situacao)
            || "Aguardando Aprovação".equals(situacao)
            || "Expirada".equals(situacao);
        if (!situacaoValida) {
            throw new IllegalStateException("Não é possível registrar cotação para uma OS com situação \""
                + situacao + "\"");
        }
    }

    private ItemCotacaoPeca montarItem(ItemCotacaoPeca solicitado) throws SQLException {
        if (solicitado.getQuantidade() <= 0) {
            throw new IllegalStateException("A quantidade de cada peça deve ser maior que zero");
        }
        Peca peca = pecaDAO.buscarPorCodigo(solicitado.getPeca().getCodigo());
        if (peca == null) {
            throw new IllegalStateException("Peça não cadastrada: " + solicitado.getPeca().getDescricao());
        }
        return new ItemCotacaoPeca(peca, solicitado.getQuantidade());
    }

    private void exigirPreenchido(String valor, String mensagem) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException(mensagem);
        }
    }
}
