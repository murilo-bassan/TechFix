package br.com.techfix.view;

import br.com.techfix.controller.ControladoraRegistrarCotacao;
import br.com.techfix.model.Cliente;
import br.com.techfix.model.Cotacao;
import br.com.techfix.model.Equipamento;
import br.com.techfix.model.ItemCotacaoPeca;
import br.com.techfix.model.OrdemDeServico;
import br.com.techfix.model.Peca;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class TelaRegistrarCotacao {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final NumberFormat MOEDA = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    private final ControladoraRegistrarCotacao controladora;

    private final Label situacaoOS = new Label("Situação: —");
    private final Label numeroOSCabecalho = new Label("Nº OS: —");
    private final TextField numeroOS = new TextField();
    private final Label resumoCliente = new Label("—");
    private final Label resumoEquipamento = new Label("—");
    private final Label resumoDefeito = new Label("—");
    private final Label resumoValorReferencia = new Label("—");
    private final Label resumoCotacaoPendente = new Label("—");
    private final TextArea diagnostico = new TextArea();
    private final ComboBox<Peca> pecas = new ComboBox<>();
    private final TextField quantidade = new TextField("1");
    private final ObservableList<ItemCotacaoPeca> itens = FXCollections.observableArrayList();
    private final TableView<ItemCotacaoPeca> tabelaItens = new TableView<>(itens);
    private final TextField maoDeObra = new TextField();
    private final TextField prazoExecucao = new TextField();
    private final Label totalCotacao = new Label();
    private final Label validadeCotacao = new Label();
    private final VBox avisoAntieconomico = new VBox(6);
    private final Label mensagem = new Label();
    private final ScrollPane rolagem = new ScrollPane();

    private OrdemDeServico ordemCarregada;

    public TelaRegistrarCotacao(ControladoraRegistrarCotacao controladora) {
        this.controladora = controladora;
        mensagem.getStyleClass().add("mensagem");
        mensagem.setWrapText(true);
        mensagem.setMaxWidth(Double.MAX_VALUE);
        mensagem.visibleProperty().bind(mensagem.textProperty().isNotEmpty());
        mensagem.managedProperty().bind(mensagem.visibleProperty());
        // A mensagem fica no topo da tela; ao exibi-la, a rolagem volta para o início.
        mensagem.textProperty().addListener((obs, antes, texto) -> {
            if (!texto.isEmpty()) {
                rolagem.setVvalue(0);
            }
        });
    }

    public ScrollPane getRaiz() {
        numeroOSCabecalho.getStyleClass().add("cabecalho-os");
        situacaoOS.getStyleClass().add("cabecalho-os");
        Label titulo = new Label("TechFix — Registrar Cotação");
        titulo.getStyleClass().add("cabecalho-titulo");
        HBox cabecalho = new HBox(16, titulo, espacoEntre(), situacaoOS, numeroOSCabecalho);
        cabecalho.getStyleClass().add("cabecalho");

        VBox conteudo = new VBox(14,
            cabecalho,
            mensagem,
            card("1. Ordem de Serviço", montarSecaoOrdem()),
            card("2. Diagnóstico técnico", montarSecaoDiagnostico()),
            card("3. Peças necessárias", montarSecaoPecas()),
            card("4. Mão de obra e prazo", montarSecaoValores()),
            montarRodape());
        conteudo.setPadding(new Insets(16));
        conteudo.getStyleClass().add("raiz");
        atualizarTotais();
        rolagem.setContent(conteudo);
        rolagem.setFitToWidth(true);
        rolagem.getStyleClass().add("rolagem");
        return rolagem;
    }

    private Node montarSecaoOrdem() {
        numeroOS.setPromptText("Nº da OS");
        HBox.setHgrow(numeroOS, Priority.ALWAYS);
        Button buscar = botaoSecundario("Buscar", e -> informarNumeroOS(numeroOS.getText()));
        HBox linhaBusca = new HBox(10, numeroOS, buscar);
        linhaBusca.setAlignment(Pos.CENTER_LEFT);

        GridPane resumo = new GridPane();
        resumo.setHgap(12);
        resumo.setVgap(4);
        resumo.addRow(0, rotulo("Cliente"), resumoCliente);
        resumo.addRow(1, rotulo("Equipamento"), resumoEquipamento);
        resumo.addRow(2, rotulo("Valor de referência"), resumoValorReferencia);
        resumo.addRow(3, rotulo("Defeito relatado"), resumoDefeito);
        resumo.addRow(4, rotulo("Cotação pendente"), resumoCotacaoPendente);
        resumoDefeito.setWrapText(true);

        return new VBox(8, rotulo("Buscar Ordem de Serviço (número)"), linhaBusca, resumo);
    }

    private Node montarSecaoDiagnostico() {
        diagnostico.setPrefRowCount(3);
        diagnostico.setWrapText(true);
        diagnostico.setPromptText("Descreva o defeito encontrado e o serviço necessário");
        return new VBox(8, rotulo("Diagnóstico técnico"), diagnostico);
    }

    private Node montarSecaoPecas() {
        pecas.setConverter(new StringConverter<Peca>() {
            @Override
            public String toString(Peca peca) {
                if (peca == null) {
                    return "";
                }
                return peca.getDescricao() + " — " + MOEDA.format(peca.getPrecoVenda())
                    + " (estoque: " + peca.getQuantidadeEstoque() + ")";
            }

            @Override
            public Peca fromString(String texto) {
                return null;
            }
        });
        pecas.setPromptText("Selecione a peça");
        pecas.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(pecas, Priority.ALWAYS);
        carregarPecas();

        quantidade.setPrefWidth(70);
        Button adicionar = botaoSecundario("Adicionar", e -> adicionarPeca());
        HBox linhaAdicionar = new HBox(10, pecas, rotulo("Qtd."), quantidade, adicionar);
        linhaAdicionar.setAlignment(Pos.CENTER_LEFT);

        tabelaItens.setPlaceholder(new Label("Nenhuma peça adicionada (cotação apenas de mão de obra)"));
        tabelaItens.setPrefHeight(150);
        tabelaItens.getColumns().add(coluna("Peça", 300, item -> item.getPeca().getDescricao()));
        tabelaItens.getColumns().add(coluna("Qtd.", 60, item -> String.valueOf(item.getQuantidade())));
        tabelaItens.getColumns().add(coluna("Valor unitário", 110, item -> MOEDA.format(item.getValorUnitario())));
        tabelaItens.getColumns().add(coluna("Subtotal", 110, item -> MOEDA.format(item.getSubtotal())));
        itens.addListener((javafx.collections.ListChangeListener<ItemCotacaoPeca>) c -> atualizarTotais());

        Button remover = botaoSecundario("Remover selecionada", e -> removerPecaSelecionada());
        return new VBox(8, linhaAdicionar, tabelaItens, remover);
    }

    private Node montarSecaoValores() {
        maoDeObra.setPromptText("0,00");
        maoDeObra.textProperty().addListener((obs, antes, texto) -> atualizarTotais());
        prazoExecucao.setPromptText("Dias");

        GridPane grade = new GridPane();
        grade.setHgap(12);
        grade.setVgap(4);
        grade.addRow(0, rotulo("Valor da mão de obra (R$)"), rotulo("Prazo estimado de execução (dias)"));
        grade.addRow(1, maoDeObra, prazoExecucao);
        return grade;
    }

    private Node montarRodape() {
        Label tituloAviso = new Label("Reparo antieconômico");
        tituloAviso.getStyleClass().add("aviso-titulo");
        Label explicacao = new Label("O valor total da cotação ultrapassa 70% do valor de referência do equipamento "
            + "(RN06). A ciência do cliente será registrada na aprovação da cotação.");
        explicacao.setWrapText(true);
        avisoAntieconomico.getChildren().addAll(tituloAviso, explicacao);
        avisoAntieconomico.getStyleClass().add("aviso");
        avisoAntieconomico.setVisible(false);
        avisoAntieconomico.managedProperty().bind(avisoAntieconomico.visibleProperty());

        totalCotacao.getStyleClass().add("total-cotacao");
        validadeCotacao.getStyleClass().add("ajuda");

        Button registrar = new Button("Registrar Cotação");
        registrar.getStyleClass().add("botao-primario");
        registrar.setMinWidth(Region.USE_PREF_SIZE);
        registrar.setOnAction(e -> confirmarRegistro());

        VBox totais = new VBox(2, totalCotacao, validadeCotacao);
        HBox linha = new HBox(12, totais, espacoEntre(), registrar);
        linha.setAlignment(Pos.CENTER_LEFT);
        return new VBox(10, avisoAntieconomico, linha);
    }

    public void informarNumeroOS(String texto) {
        long numero;
        try {
            numero = Long.parseLong(texto == null ? "" : texto.trim());
        } catch (NumberFormatException e) {
            mostrarErro("Informe o número da Ordem de Serviço");
            return;
        }
        try {
            OrdemDeServico os = controladora.buscarOrdemServico(numero);
            if (os == null) {
                ordemCarregada = null;
                limparResumo();
                mostrarErro("Ordem de Serviço não encontrada.");
                return;
            }
            exibirOrdem(os);
            exibirMensagem("Ordem de Serviço encontrada.");
        } catch (IllegalStateException e) {
            mostrarErro(e.getMessage());
        } catch (SQLException e) {
            mostrarErroBanco(e);
        }
    }

    private void exibirOrdem(OrdemDeServico os) throws SQLException {
        ordemCarregada = os;
        Equipamento equipamento = os.getEquipamento();
        Cliente cliente = equipamento.getCliente();
        numeroOSCabecalho.setText("Nº OS: " + os.getNumero());
        situacaoOS.setText("Situação: " + os.getSituacao());
        resumoCliente.setText(cliente.getNomeOuRazaoSocial() + " (" + cliente.getCpfOuCnpj() + ")");
        resumoEquipamento.setText(equipamento.getTipo() + " " + equipamento.getMarca() + " "
            + equipamento.getModelo() + " — série " + equipamento.getNumeroSerie());
        resumoValorReferencia.setText(equipamento.getValorReferencia() > 0
            ? MOEDA.format(equipamento.getValorReferencia())
            : "não informado (verificação de reparo antieconômico não será aplicada)");
        resumoDefeito.setText(os.getDefeitoRelatado());
        long pendente = controladora.buscarCotacaoPendente(os.getNumero());
        resumoCotacaoPendente.setText(pendente == 0
            ? "nenhuma"
            : "nº " + pendente + " (será substituída se uma nova cotação for registrada)");
        atualizarTotais();
    }

    private void limparResumo() {
        numeroOSCabecalho.setText("Nº OS: —");
        situacaoOS.setText("Situação: —");
        resumoCliente.setText("—");
        resumoEquipamento.setText("—");
        resumoValorReferencia.setText("—");
        resumoDefeito.setText("—");
        resumoCotacaoPendente.setText("—");
        atualizarTotais();
    }

    private void carregarPecas() {
        try {
            pecas.getItems().setAll(controladora.listarPecas());
        } catch (SQLException e) {
            mostrarErroBanco(e);
        }
    }

    private void adicionarPeca() {
        Peca peca = pecas.getValue();
        int qtd;
        try {
            qtd = Integer.parseInt(quantidade.getText().trim());
        } catch (NumberFormatException e) {
            qtd = 0;
        }
        if (peca == null || qtd <= 0) {
            mostrarErro("Selecione a peça e informe uma quantidade maior que zero");
            return;
        }
        for (int i = 0; i < itens.size(); i++) {
            ItemCotacaoPeca existente = itens.get(i);
            if (existente.getPeca().getCodigo().equals(peca.getCodigo())) {
                itens.set(i, new ItemCotacaoPeca(peca, existente.getQuantidade() + qtd));
                finalizarAdicao();
                return;
            }
        }
        itens.add(new ItemCotacaoPeca(peca, qtd));
        finalizarAdicao();
    }

    private void finalizarAdicao() {
        quantidade.setText("1");
        pecas.setValue(null);
        mensagem.setText("");
    }

    private void removerPecaSelecionada() {
        ItemCotacaoPeca selecionado = tabelaItens.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            mostrarErro("Selecione uma peça na tabela para remover");
            return;
        }
        itens.remove(selecionado);
    }

    private void atualizarTotais() {
        double total = Cotacao.calcularTotal(new ArrayList<>(itens), lerMaoDeObra());
        totalCotacao.setText("Valor total: " + MOEDA.format(total));
        validadeCotacao.setText("Validade da cotação: " + Cotacao.VALIDADE_DIAS
            + " dias a partir do registro (RN05)");

        boolean antieconomico = ordemCarregada != null && controladora.isReparoAntieconomico(ordemCarregada, total);
        avisoAntieconomico.setVisible(antieconomico);
    }

    private double lerMaoDeObra() {
        try {
            return Double.parseDouble(maoDeObra.getText().trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public void confirmarRegistro() {
        if (ordemCarregada == null) {
            mostrarErro("Busque a Ordem de Serviço antes de registrar a cotação");
            return;
        }
        double valorMaoDeObra;
        try {
            String texto = maoDeObra.getText().trim().replace(',', '.');
            valorMaoDeObra = texto.isEmpty() ? 0.0 : Double.parseDouble(texto);
        } catch (NumberFormatException e) {
            mostrarErro("Valor da mão de obra inválido");
            return;
        }
        int prazo;
        try {
            prazo = Integer.parseInt(prazoExecucao.getText().trim());
        } catch (NumberFormatException e) {
            mostrarErro("Informe o prazo estimado de execução em dias");
            return;
        }

        try {
            long pendente = controladora.buscarCotacaoPendente(ordemCarregada.getNumero());
            if (pendente != 0 && !confirmarSubstituicao(pendente)) {
                exibirOrdem(controladora.buscarOrdemServico(ordemCarregada.getNumero()));
                exibirMensagem("Substituição cancelada. A cotação nº " + pendente
                    + " continua pendente e a nova cotação não foi registrada.");
                return;
            }
            List<ItemCotacaoPeca> solicitados = new ArrayList<>(itens);
            Cotacao cotacao = controladora.registrarCotacao(ordemCarregada.getNumero(), diagnostico.getText(),
                solicitados, valorMaoDeObra, prazo, pendente != 0);
            exibirMensagem("Cotação nº " + cotacao.getId() + " registrada para a OS "
                + ordemCarregada.getNumero() + ". A OS aguarda a aprovação do cliente.");
            exibirResumo(cotacao);
            limparFormulario();
            exibirOrdem(controladora.buscarOrdemServico(ordemCarregada.getNumero()));
        } catch (IllegalStateException e) {
            mostrarErro(e.getMessage());
        } catch (SQLException e) {
            mostrarErroBanco(e);
        }
    }

    private boolean confirmarSubstituicao(long numeroCotacaoPendente) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Substituir cotação");
        alerta.setHeaderText("A OS já possui a cotação nº " + numeroCotacaoPendente + " pendente");
        alerta.setContentText("Deseja substituí-la pela nova cotação? A cotação anterior será mantida "
            + "com a situação \"" + Cotacao.SUBSTITUIDA + "\".");
        Optional<ButtonType> resposta = alerta.showAndWait();
        return resposta.isPresent() && resposta.get() == ButtonType.OK;
    }

    private void limparFormulario() {
        diagnostico.clear();
        itens.clear();
        maoDeObra.clear();
        prazoExecucao.clear();
    }

    private void exibirResumo(Cotacao cotacao) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Cotação registrada");
        alerta.setHeaderText("TechFix — Cotação nº " + cotacao.getId());
        Label conteudo = new Label(montarResumo(cotacao));
        conteudo.setWrapText(true);
        conteudo.setMaxWidth(440);
        alerta.getDialogPane().setContent(conteudo);
        alerta.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alerta.setResizable(true);
        alerta.showAndWait();
    }

    private String montarResumo(Cotacao cotacao) {
        StringBuilder texto = new StringBuilder();
        texto.append("Ordem de Serviço: ").append(cotacao.getOrdemDeServico().getNumero()).append("\n");
        texto.append("Técnico: ").append(cotacao.getTecnico().getNome()).append("\n");
        texto.append("Registrada em: ").append(cotacao.getDataRegistro().format(FORMATO)).append("\n");
        texto.append("Válida até: ").append(cotacao.getPrazoValidade().format(FORMATO)).append("\n\n");
        texto.append("Diagnóstico: ").append(cotacao.getDiagnosticoTecnico()).append("\n\n");
        if (cotacao.getItens().isEmpty()) {
            texto.append("Peças: nenhuma\n");
        } else {
            texto.append("Peças:\n");
            for (ItemCotacaoPeca item : cotacao.getItens()) {
                texto.append("  ").append(item.getQuantidade()).append("x ")
                    .append(item.getPeca().getDescricao()).append(" — ")
                    .append(MOEDA.format(item.getSubtotal())).append("\n");
            }
        }
        texto.append("Mão de obra: ").append(MOEDA.format(cotacao.getValorMaoDeObra())).append("\n");
        texto.append("Prazo estimado de execução: ").append(cotacao.getPrazoExecucaoDias()).append(" dia(s)\n");
        texto.append("Valor total: ").append(MOEDA.format(cotacao.getValorTotal()));
        if (cotacao.isReparoAntieconomico()) {
            texto.append("\n\nReparo antieconômico — a ciência do cliente será registrada na aprovação.");
        }
        return texto.toString();
    }

    private TableColumn<ItemCotacaoPeca, String> coluna(String titulo, double largura,
                                                       java.util.function.Function<ItemCotacaoPeca, String> valor) {
        TableColumn<ItemCotacaoPeca, String> coluna = new TableColumn<>(titulo);
        coluna.setPrefWidth(largura);
        coluna.setSortable(false);
        coluna.setCellValueFactory(c -> new ReadOnlyStringWrapper(valor.apply(c.getValue())));
        return coluna;
    }

    private Label rotulo(String texto) {
        Label rotulo = new Label(texto);
        rotulo.getStyleClass().add("rotulo");
        return rotulo;
    }

    private Button botaoSecundario(String texto, javafx.event.EventHandler<javafx.event.ActionEvent> acao) {
        Button botao = new Button(texto);
        botao.setMinWidth(Region.USE_PREF_SIZE);
        botao.getStyleClass().add("botao-secundario");
        botao.setOnAction(acao);
        return botao;
    }

    private VBox card(String titulo, Node conteudo) {
        Label rotulo = new Label(titulo);
        rotulo.getStyleClass().add("titulo-secao");
        VBox card = new VBox(10, rotulo, conteudo);
        card.getStyleClass().add("card");
        return card;
    }

    private Region espacoEntre() {
        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);
        return espaco;
    }

    public void exibirMensagem(String texto) {
        mensagem.setText(texto);
        mensagem.getStyleClass().remove("mensagem-erro");
    }

    private void mostrarErro(String texto) {
        mensagem.setText(texto);
        if (!mensagem.getStyleClass().contains("mensagem-erro")) {
            mensagem.getStyleClass().add("mensagem-erro");
        }
    }

    private void mostrarErroBanco(SQLException e) {
        mostrarErro("Erro ao acessar o banco: " + e.getMessage());
    }
}
