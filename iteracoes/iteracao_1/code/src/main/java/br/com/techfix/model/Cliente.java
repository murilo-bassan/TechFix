package br.com.techfix.model;

import java.time.LocalDateTime;

public class Cliente {

    private String tipoPessoa;
    private String nomeOuRazaoSocial;
    private String cpfOuCnpj;
    private String telefonePrincipal;
    private String telefoneAlternativo;
    private String enderecoEmail;
    private String endereco;
    private boolean clienteFaturado;
    private LocalDateTime dataCadastro;

    public String getTipoPessoa() {
        return tipoPessoa;
    }

    public void setTipoPessoa(String tipoPessoa) {
        this.tipoPessoa = tipoPessoa;
    }

    public String getNomeOuRazaoSocial() {
        return nomeOuRazaoSocial;
    }

    public void setNomeOuRazaoSocial(String nomeOuRazaoSocial) {
        this.nomeOuRazaoSocial = nomeOuRazaoSocial;
    }

    public String getCpfOuCnpj() {
        return cpfOuCnpj;
    }

    public void setCpfOuCnpj(String cpfOuCnpj) {
        this.cpfOuCnpj = cpfOuCnpj;
    }

    public String getTelefonePrincipal() {
        return telefonePrincipal;
    }

    public void setTelefonePrincipal(String telefonePrincipal) {
        this.telefonePrincipal = telefonePrincipal;
    }

    public String getTelefoneAlternativo() {
        return telefoneAlternativo;
    }

    public void setTelefoneAlternativo(String telefoneAlternativo) {
        this.telefoneAlternativo = telefoneAlternativo;
    }

    public String getEnderecoEmail() {
        return enderecoEmail;
    }

    public void setEnderecoEmail(String enderecoEmail) {
        this.enderecoEmail = enderecoEmail;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public boolean isClienteFaturado() {
        return clienteFaturado;
    }

    public void setClienteFaturado(boolean clienteFaturado) {
        this.clienteFaturado = clienteFaturado;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }
}
