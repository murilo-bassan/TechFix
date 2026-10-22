package br.com.techfix.model;

public class ItemCotacaoPeca {

    private int quantidade;
    private double valorUnitario;
    private Peca peca;

    public ItemCotacaoPeca(Peca peca, int quantidade) {
        this.peca = peca;
        this.quantidade = quantidade;
        this.valorUnitario = peca.getPrecoVenda();
    }

    public ItemCotacaoPeca(Peca peca, int quantidade, double valorUnitario) {
        this.peca = peca;
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;
    }

    public double getSubtotal() {
        return quantidade * valorUnitario;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public double getValorUnitario() {
        return valorUnitario;
    }

    public Peca getPeca() {
        return peca;
    }
}
