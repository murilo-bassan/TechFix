package br.com.techfix.view;

import br.com.techfix.controller.ControladoraAbrirOrdemServico;
import br.com.techfix.model.Cliente;
import br.com.techfix.model.Equipamento;
import br.com.techfix.model.OrdemDeServico;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TelaAbrirOrdemServico {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ControladoraAbrirOrdemServico controladora;

    private final Label situacaoOS = new Label("Situação: —");
    private final Label numeroOS = new Label("Nº OS: —");
    private final Label seloCliente = new Label("encontrado");
    private final Label seloEquipamento = new Label("encontrado");
    private final TextField nomeCliente = new TextField();
    private final TextField telefone = new TextField();
    private final TextField cpfCnpj = new TextField();
    private final TextField numeroSerie = new TextField();
    private final TextField tipo = new TextField();
    private final TextField marca = new TextField();
    private final TextField modelo = new TextField();
    private final TextField valorReferencia = new TextField();
    private final TextArea defeito = new TextArea();
    private final CheckBox carregador = new CheckBox("Carregador");
    private final CheckBox cabo = new CheckBox("Cabo de força");
    private final CheckBox bolsa = new CheckBox("Bolsa/mochila");
    private final CheckBox midias = new CheckBox("Mídias/pendrive");
    private final CheckBox outros = new CheckBox("Outros");
    private final TextField outrosDescricao = new TextField();
    private final CheckBox cienciaTaxa = new CheckBox("Cliente ciente de que, em caso de recusa da cotação, será cobrada a taxa de diagnóstico vigente (RN02).");
    private final Label infoEntrada = new Label();
    private final Label mensagem = new Label();
    private final ScrollPane rolagem = new ScrollPane();

    private Cliente clienteEncontrado;
    private String defeitoInformado;
    private String checklistInformado;
    private boolean cienciaInformada;

    public TelaAbrirOrdemServico(ControladoraAbrirOrdemServico controladora) {
        this.controladora = controladora;
        mensagem.getStyleClass().add("mensagem");
        mensagem.setWrapText(true);
        mensagem.setMaxWidth(Double.MAX_VALUE);
        esconderQuandoVazio(mensagem);
        // A mensagem fica no topo da tela; ao exibi-la, a rolagem volta para o início.
        mensagem.textProperty().addListener((obs, antes, texto) -> {
            if (!texto.isEmpty()) {
                rolagem.setVvalue(0);
            }
        });
    }

    public ScrollPane getRaiz() {
        numeroOS.getStyleClass().add("cabecalho-os");
        situacaoOS.getStyleClass().add("cabecalho-os");
        Label titulo = new Label("TechFix — Abrir Ordem de Serviço");
        titulo.getStyleClass().add("cabecalho-titulo");
        HBox cabecalho = new HBox(16, titulo, espacoEntre(), situacaoOS, numeroOS);
        cabecalho.getStyleClass().add("cabecalho");

        VBox conteudo = new VBox(14,
            cabecalho,
            mensagem,
            card("1. Dados do cliente", montarSecaoCliente()),
            card("2. Dados do equipamento", montarSecaoEquipamento()),
            card("3. Defeito e acessórios", montarSecaoDefeito()),
            montarRodape());
        conteudo.setPadding(new Insets(16));
        conteudo.getStyleClass().add("raiz");
        rolagem.setContent(conteudo);
        rolagem.setFitToWidth(true);
        rolagem.getStyleClass().add("rolagem");
        return rolagem;
    }

    private Node montarSecaoCliente() {
        cpfCnpj.setPromptText("CPF/CNPJ");
        cpfCnpj.textProperty().addListener((obs, antes, texto) -> {
            if (clienteEncontrado == null
                    || !texto.replaceAll("\\D", "").equals(clienteEncontrado.getCpfOuCnpj())) {
                clienteEncontrado = null;
                seloCliente.setVisible(false);
            }
        });
        HBox linhaBusca = linhaComBotao(cpfCnpj, botaoSecundario("Buscar", e -> informarCpfCnpj(cpfCnpj.getText())));

        seloCliente.getStyleClass().add("selo-encontrado");
        seloCliente.setVisible(false);
        seloCliente.managedProperty().bind(seloCliente.visibleProperty());
        HBox rotuloNome = new HBox(8, rotulo("Nome / Razão social"), seloCliente);
        rotuloNome.setAlignment(Pos.CENTER_LEFT);

        GridPane dados = grade(2);
        dados.addRow(0, rotuloNome, rotulo("Telefone"));
        dados.addRow(1, nomeCliente, telefone);

        return new VBox(8,
            rotulo("Buscar cliente (CPF/CNPJ)"),
            linhaBusca,
            dados,
            ajuda("Cliente não encontrado? Preencha nome e telefone e clique em \"Cadastrar Cliente\" (RN03)."),
            botaoSecundario("Cadastrar Cliente", e -> onCadastrarCliente()));
    }

    private Node montarSecaoEquipamento() {
        numeroSerie.setPromptText("Nº de série");
        numeroSerie.textProperty().addListener((obs, antes, texto) -> seloEquipamento.setVisible(false));

        seloEquipamento.getStyleClass().add("selo-encontrado");
        seloEquipamento.setVisible(false);
        seloEquipamento.managedProperty().bind(seloEquipamento.visibleProperty());
        HBox rotuloTipo = new HBox(8, rotulo("Tipo"), seloEquipamento);
        rotuloTipo.setAlignment(Pos.CENTER_LEFT);
        HBox linhaBusca = linhaComBotao(numeroSerie, botaoSecundario("Buscar", e -> informarNumeroSerie(numeroSerie.getText())));

        valorReferencia.setPromptText("Opcional");
        GridPane dados = grade(4);
        dados.addRow(0, rotuloTipo, rotulo("Marca"), rotulo("Modelo"), rotulo("Valor de referência (R$)"));
        dados.addRow(1, tipo, marca, modelo, valorReferencia);

        return new VBox(8,
            rotulo("Buscar equipamento (nº de série)"),
            linhaBusca,
            dados,
            ajuda("Equipamento não encontrado? Preencha os dados e clique em \"Cadastrar Equipamento\"."),
            botaoSecundario("Cadastrar Equipamento", e -> onCadastrarEquipamento()));
    }

    private Node montarSecaoDefeito() {
        defeito.setPrefRowCount(3);
        defeito.setWrapText(true);

        outrosDescricao.setPromptText("Descreva o acessório");
        outrosDescricao.setVisible(false);
        outrosDescricao.managedProperty().bind(outrosDescricao.visibleProperty());
        outros.selectedProperty().addListener((obs, antes, marcado) -> outrosDescricao.setVisible(marcado));
        HBox checkboxes = new HBox(16, carregador, cabo, bolsa, midias, outros);

        Label tituloAviso = new Label("Atenção");
        tituloAviso.getStyleClass().add("aviso-titulo");
        cienciaTaxa.setWrapText(true);
        cienciaTaxa.setMaxWidth(Double.MAX_VALUE);
        VBox avisoTaxa = new VBox(6, tituloAviso, cienciaTaxa);
        avisoTaxa.getStyleClass().add("aviso");

        return new VBox(8,
            rotulo("Defeito relatado pelo cliente"),
            defeito,
            rotulo("Checklist de acessórios entregues"),
            checkboxes,
            outrosDescricao,
            avisoTaxa);
    }

    private Node montarRodape() {
        infoEntrada.getStyleClass().add("ajuda");
        infoEntrada.setWrapText(true);
        atualizarInfoEntrada(LocalDateTime.now());

        Button gerar = new Button("Gerar Ordem de Serviço");
        gerar.getStyleClass().add("botao-primario");
        gerar.setMinWidth(Region.USE_PREF_SIZE);
        gerar.setOnAction(e -> {
            informarDefeitoChecklist(defeito.getText(), montarChecklist());
            informarCienciaTaxaDiagnostico(cienciaTaxa.isSelected());
            confirmarAbertura();
        });

        HBox rodape = new HBox(12, infoEntrada, espacoEntre(), gerar);
        rodape.setAlignment(Pos.CENTER_LEFT);
        return rodape;
    }

    public void informarCpfCnpj(String cpfCnpj) {
        try {
            Cliente cliente = controladora.buscarCliente(cpfCnpj);
            selecionarCliente(cliente);
            if (cliente == null) {
                mostrarErro("Cliente não encontrado.");
            } else {
                exibirMensagem("Cliente encontrado.");
            }
        } catch (IllegalStateException e) {
            mostrarErro(e.getMessage());
        } catch (SQLException e) {
            mostrarErroBanco(e);
        }
    }

    private void selecionarCliente(Cliente cliente) {
        clienteEncontrado = cliente;
        if (cliente == null) {
            mostrarSelo(seloCliente, "não encontrado", false);
            return;
        }
        nomeCliente.setText(cliente.getNomeOuRazaoSocial());
        telefone.setText(cliente.getTelefonePrincipal());
        cpfCnpj.setText(cliente.getCpfOuCnpj());
        mostrarSelo(seloCliente, "encontrado", true);
    }

    private void mostrarSelo(Label selo, String texto, boolean encontrado) {
        selo.setText(texto);
        selo.getStyleClass().remove("selo-nao-encontrado");
        if (!encontrado) {
            selo.getStyleClass().add("selo-nao-encontrado");
        }
        selo.setVisible(true);
    }

    private void onCadastrarCliente() {
        try {
            Cliente cliente = controladora.cadastrarCliente(cpfCnpj.getText(), nomeCliente.getText(), telefone.getText());
            selecionarCliente(cliente);
            mostrarSelo(seloCliente, "cadastrado", true);
            exibirMensagem("Cliente cadastrado.");
        } catch (IllegalStateException e) {
            mostrarErro(e.getMessage());
        } catch (SQLException e) {
            mostrarErroBanco(e);
        }
    }

    public void informarNumeroSerie(String numeroSerie) {
        tipo.clear();
        marca.clear();
        modelo.clear();
        valorReferencia.clear();
        seloEquipamento.setVisible(false);
        try {
            Equipamento equipamento = controladora.buscarEquipamento(numeroSerie);
            if (equipamento == null) {
                mostrarSelo(seloEquipamento, "não encontrado", false);
                mostrarErro("Equipamento não encontrado.");
                return;
            }
            tipo.setText(equipamento.getTipo());
            marca.setText(equipamento.getMarca());
            modelo.setText(equipamento.getModelo());
            if (equipamento.getValorReferencia() > 0) {
                valorReferencia.setText(String.format("%.2f", equipamento.getValorReferencia()));
            }
            mostrarSelo(seloEquipamento, "encontrado", true);

            Cliente dono = equipamento.getCliente();
            boolean trocouCliente = clienteEncontrado != null
                && !clienteEncontrado.getCpfOuCnpj().equals(dono.getCpfOuCnpj());
            selecionarCliente(dono);
            if (trocouCliente) {
                mostrarErro("Este equipamento pertence a " + dono.getNomeOuRazaoSocial()
                    + ". Os dados do cliente foram trocados para o proprietário.");
            } else {
                exibirMensagem("Equipamento encontrado. Cliente preenchido com o proprietário.");
            }
        } catch (IllegalStateException e) {
            mostrarErro(e.getMessage());
        } catch (SQLException e) {
            mostrarErroBanco(e);
        }
    }

    private void onCadastrarEquipamento() {
        try {
            controladora.cadastrarEquipamento(
                numeroSerie.getText(),
                tipo.getText(),
                marca.getText(),
                modelo.getText(),
                valorReferencia.getText(),
                clienteEncontrado == null ? "" : clienteEncontrado.getCpfOuCnpj());
            mostrarSelo(seloEquipamento, "cadastrado", true);
            exibirMensagem("Equipamento cadastrado.");
        } catch (IllegalStateException e) {
            mostrarErro(e.getMessage());
        } catch (SQLException e) {
            mostrarErroBanco(e);
        }
    }

    public void informarDefeitoChecklist(String defeito, String checklist) {
        this.defeitoInformado = defeito;
        this.checklistInformado = checklist;
    }

    public void informarCienciaTaxaDiagnostico(boolean ciente) {
        this.cienciaInformada = ciente;
    }

    public void confirmarAbertura() {
        try {
            OrdemDeServico os = controladora.abrirOrdemServico(
                clienteEncontrado == null ? "" : clienteEncontrado.getCpfOuCnpj(),
                numeroSerie.getText(),
                defeitoInformado,
                checklistInformado,
                cienciaInformada);
            numeroOS.setText("Nº OS: " + os.getNumero());
            situacaoOS.setText("Situação: " + os.getSituacao());
            atualizarInfoEntrada(os.getDataHoraEntrada());
            exibirMensagem("Ordem de Serviço nº " + os.getNumero() + " gerada com sucesso. "
                + "Nenhuma Ordem de Serviço em andamento para este equipamento (RN01).");
            exibirComprovante(os);
        } catch (IllegalStateException e) {
            mostrarErro(e.getMessage());
        } catch (SQLException e) {
            mostrarErroBanco(e);
        }
    }

    private void atualizarInfoEntrada(LocalDateTime dataHora) {
        infoEntrada.setText("Data/hora de entrada: " + dataHora.format(FORMATO)
            + "  |  Atendente responsável: " + controladora.buscarAtendenteLogado().getNome()
            + " (preenchido automaticamente)");
    }

    private void exibirComprovante(OrdemDeServico os) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Comprovante de entrada");
        alerta.setHeaderText("TechFix — Comprovante de entrada");
        Label conteudo = new Label(montarComprovante(os));
        conteudo.setWrapText(true);
        conteudo.setMaxWidth(420);
        alerta.getDialogPane().setContent(conteudo);
        alerta.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alerta.setResizable(true);
        alerta.showAndWait();
    }

    private String montarComprovante(OrdemDeServico os) {
        Equipamento equipamento = os.getEquipamento();
        Cliente cliente = equipamento.getCliente();
        String checklist = os.getChecklistAcessorios().isEmpty() ? "Nenhum" : os.getChecklistAcessorios();
        return "Nº da OS: " + os.getNumero() + "\n"
            + "Situação: " + os.getSituacao() + "\n"
            + "Data/hora de entrada: " + os.getDataHoraEntrada().format(FORMATO) + "\n"
            + "Atendente: " + os.getAtendente().getNome() + "\n\n"
            + "Cliente: " + cliente.getNomeOuRazaoSocial() + " (" + cliente.getCpfOuCnpj() + ")\n"
            + "Telefone: " + cliente.getTelefonePrincipal() + "\n\n"
            + "Equipamento: " + equipamento.getTipo() + " " + equipamento.getMarca() + " " + equipamento.getModelo() + "\n"
            + "Nº de série: " + equipamento.getNumeroSerie() + "\n\n"
            + "Defeito relatado: " + os.getDefeitoRelatado() + "\n"
            + "Checklist de acessórios: " + checklist + "\n\n"
            + "Cliente ciente da taxa de diagnóstico em caso de recusa da cotação.";
    }

    private String montarChecklist() {
        List<String> itens = new ArrayList<>();
        if (carregador.isSelected()) itens.add("Carregador");
        if (cabo.isSelected()) itens.add("Cabo de força");
        if (bolsa.isSelected()) itens.add("Bolsa/mochila");
        if (midias.isSelected()) itens.add("Mídias/pendrive");
        if (outros.isSelected()) {
            String descricao = outrosDescricao.getText().trim();
            itens.add(descricao.isEmpty() ? "Outros" : descricao);
        }
        return String.join(", ", itens);
    }

    private Label rotulo(String texto) {
        Label rotulo = new Label(texto);
        rotulo.getStyleClass().add("rotulo");
        return rotulo;
    }

    private Label ajuda(String texto) {
        Label ajuda = new Label(texto);
        ajuda.getStyleClass().add("ajuda");
        ajuda.setWrapText(true);
        return ajuda;
    }

    private Button botaoSecundario(String texto, javafx.event.EventHandler<javafx.event.ActionEvent> acao) {
        Button botao = new Button(texto);
        botao.setMinWidth(Region.USE_PREF_SIZE);
        botao.getStyleClass().add("botao-secundario");
        botao.setOnAction(acao);
        return botao;
    }

    private HBox linhaComBotao(TextField campo, Button botao) {
        HBox.setHgrow(campo, Priority.ALWAYS);
        HBox linha = new HBox(10, campo, botao);
        linha.setAlignment(Pos.CENTER_LEFT);
        return linha;
    }

    private GridPane grade(int colunas) {
        GridPane grade = new GridPane();
        grade.setHgap(10);
        grade.setVgap(4);
        for (int i = 0; i < colunas; i++) {
            ColumnConstraints coluna = new ColumnConstraints();
            coluna.setHgrow(Priority.ALWAYS);
            coluna.setPercentWidth(100.0 / colunas);
            grade.getColumnConstraints().add(coluna);
        }
        return grade;
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

    private void esconderQuandoVazio(Label label) {
        label.visibleProperty().bind(label.textProperty().isNotEmpty());
        label.managedProperty().bind(label.visibleProperty());
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
