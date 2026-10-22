package br.com.techfix.model;

import java.time.LocalDateTime;

public class HistoricoDeSituacao {

    private long id;
    private String situacao;
    private LocalDateTime dataHora;
    private OrdemDeServico ordemDeServico;
    private Usuario usuario;

    public HistoricoDeSituacao(String situacao, LocalDateTime dataHora) {
        this.situacao = situacao;
        this.dataHora = dataHora;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSituacao() {
        return situacao;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public OrdemDeServico getOrdemDeServico() {
        return ordemDeServico;
    }

    public void setOrdemDeServico(OrdemDeServico ordemDeServico) {
        this.ordemDeServico = ordemDeServico;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}
