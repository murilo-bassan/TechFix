package br.com.techfix.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrdemDeServico {

    private long numero;
    private LocalDateTime dataHoraEntrada;
    private String checklistAcessorios;
    private String defeitoRelatado;
    private String situacao;
    private int tempoDespendido;
    private String laudoTecnico;
    private LocalDateTime dataConclusao;
    private LocalDateTime dataHoraRetirada;
    private String formaPagamento;
    private double valorPago;
    private String responsavelRetirada;
    private LocalDateTime dataLimiteGarantia;
    private boolean cienciaTaxaDiagnostico;
    private Equipamento equipamento;
    private Atendente atendente;
    private final List<HistoricoDeSituacao> historicos = new ArrayList<>();

    public OrdemDeServico(Equipamento equipamento, Atendente atendente, String defeito, String checklist, boolean cienciaTaxa) {
        this.equipamento = equipamento;
        this.atendente = atendente;
        this.defeitoRelatado = defeito;
        this.checklistAcessorios = checklist;
        this.cienciaTaxaDiagnostico = cienciaTaxa;
        this.dataHoraEntrada = LocalDateTime.now();
    }

    public OrdemDeServico(long numero, LocalDateTime dataHoraEntrada, Equipamento equipamento, Atendente atendente,
                          String defeito, String checklist, String situacao, boolean cienciaTaxa) {
        this.numero = numero;
        this.dataHoraEntrada = dataHoraEntrada;
        this.equipamento = equipamento;
        this.atendente = atendente;
        this.defeitoRelatado = defeito;
        this.checklistAcessorios = checklist;
        this.situacao = situacao;
        this.cienciaTaxaDiagnostico = cienciaTaxa;
    }

    public void registrarSituacao(String situacao, Usuario usuario) {
        this.situacao = situacao;
        HistoricoDeSituacao historico = new HistoricoDeSituacao(situacao, LocalDateTime.now());
        historico.setOrdemDeServico(this);
        historico.setUsuario(usuario);
        historicos.add(historico);
    }

    public List<HistoricoDeSituacao> getHistoricos() {
        return Collections.unmodifiableList(historicos);
    }

    public long getNumero() {
        return numero;
    }

    public void setNumero(long numero) {
        this.numero = numero;
    }

    public LocalDateTime getDataHoraEntrada() {
        return dataHoraEntrada;
    }

    public String getChecklistAcessorios() {
        return checklistAcessorios;
    }

    public String getDefeitoRelatado() {
        return defeitoRelatado;
    }

    public void setDefeitoRelatado(String defeitoRelatado) {
        this.defeitoRelatado = defeitoRelatado;
    }

    public String getSituacao() {
        return situacao;
    }

    public int getTempoDespendido() {
        return tempoDespendido;
    }

    public void setTempoDespendido(int tempoDespendido) {
        this.tempoDespendido = tempoDespendido;
    }

    public String getLaudoTecnico() {
        return laudoTecnico;
    }

    public void setLaudoTecnico(String laudoTecnico) {
        this.laudoTecnico = laudoTecnico;
    }

    public LocalDateTime getDataConclusao() {
        return dataConclusao;
    }

    public void setDataConclusao(LocalDateTime dataConclusao) {
        this.dataConclusao = dataConclusao;
    }

    public LocalDateTime getDataHoraRetirada() {
        return dataHoraRetirada;
    }

    public void setDataHoraRetirada(LocalDateTime dataHoraRetirada) {
        this.dataHoraRetirada = dataHoraRetirada;
    }

    public String getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(String formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public double getValorPago() {
        return valorPago;
    }

    public void setValorPago(double valorPago) {
        this.valorPago = valorPago;
    }

    public String getResponsavelRetirada() {
        return responsavelRetirada;
    }

    public void setResponsavelRetirada(String responsavelRetirada) {
        this.responsavelRetirada = responsavelRetirada;
    }

    public LocalDateTime getDataLimiteGarantia() {
        return dataLimiteGarantia;
    }

    public void setDataLimiteGarantia(LocalDateTime dataLimiteGarantia) {
        this.dataLimiteGarantia = dataLimiteGarantia;
    }

    public boolean isCienciaTaxaDiagnostico() {
        return cienciaTaxaDiagnostico;
    }

    public Equipamento getEquipamento() {
        return equipamento;
    }

    public Atendente getAtendente() {
        return atendente;
    }
}
