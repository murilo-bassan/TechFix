package br.com.techfix.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Cotacao {

    public static final String PENDENTE = "Pendente";
    public static final String APROVADA = "Aprovada";
    public static final String RECUSADA = "Recusada";
    public static final String EXPIRADA = "Expirada";
    public static final String SUBSTITUIDA = "Substituída";

    public static final int VALIDADE_DIAS = 7;
    public static final double PERCENTUAL_LIMITE_ANTIECONOMICO = 0.70;

    private long id;
    private String diagnosticoTecnico;
    private double valorMaoDeObra;
    private int prazoExecucaoDias;
    private double valorTotal;
    private LocalDateTime dataRegistro;
    private LocalDateTime prazoValidade;
    private String situacao;
    private boolean reparoAntieconomico;
    private OrdemDeServico ordemDeServico;
    private Usuario tecnico;
    private final List<ItemCotacaoPeca> itens = new ArrayList<>();

    public Cotacao(OrdemDeServico ordemDeServico, Usuario tecnico, String diagnosticoTecnico,
                   double valorMaoDeObra, int prazoExecucaoDias) {
        this.ordemDeServico = ordemDeServico;
        this.tecnico = tecnico;
        this.diagnosticoTecnico = diagnosticoTecnico;
        this.valorMaoDeObra = valorMaoDeObra;
        this.prazoExecucaoDias = prazoExecucaoDias;
        this.dataRegistro = LocalDateTime.now();
        this.prazoValidade = dataRegistro.plusDays(VALIDADE_DIAS);
        this.situacao = PENDENTE;
        this.valorTotal = valorMaoDeObra;
    }

    public void adicionarItem(ItemCotacaoPeca item) {
        itens.add(item);
        valorTotal = calcularTotal(itens, valorMaoDeObra);
    }

    public static double calcularTotal(List<ItemCotacaoPeca> itens, double valorMaoDeObra) {
        double total = valorMaoDeObra;
        for (ItemCotacaoPeca item : itens) {
            total += item.getSubtotal();
        }
        return total;
    }

    public static boolean excedeLimiteAntieconomico(double valorTotal, double valorReferencia) {
        return valorReferencia > 0 && valorTotal > valorReferencia * PERCENTUAL_LIMITE_ANTIECONOMICO;
    }

    public boolean estaVencida(LocalDateTime agora) {
        return agora.isAfter(prazoValidade);
    }

    public List<ItemCotacaoPeca> getItens() {
        return Collections.unmodifiableList(itens);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDiagnosticoTecnico() {
        return diagnosticoTecnico;
    }

    public double getValorMaoDeObra() {
        return valorMaoDeObra;
    }

    public int getPrazoExecucaoDias() {
        return prazoExecucaoDias;
    }

    public double getValorTotal() {
        return valorTotal;
    }

    public LocalDateTime getDataRegistro() {
        return dataRegistro;
    }

    public LocalDateTime getPrazoValidade() {
        return prazoValidade;
    }

    public String getSituacao() {
        return situacao;
    }

    public boolean isReparoAntieconomico() {
        return reparoAntieconomico;
    }

    public void setReparoAntieconomico(boolean reparoAntieconomico) {
        this.reparoAntieconomico = reparoAntieconomico;
    }

    public OrdemDeServico getOrdemDeServico() {
        return ordemDeServico;
    }

    public Usuario getTecnico() {
        return tecnico;
    }
}
