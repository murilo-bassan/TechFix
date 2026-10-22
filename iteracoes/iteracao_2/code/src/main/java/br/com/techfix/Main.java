package br.com.techfix;

import br.com.techfix.controller.ControladoraAbrirOrdemServico;
import br.com.techfix.controller.ControladoraRegistrarCotacao;
import br.com.techfix.dao.ClienteDAO;
import br.com.techfix.dao.Conexao;
import br.com.techfix.dao.CotacaoDAO;
import br.com.techfix.dao.EquipamentoDAO;
import br.com.techfix.dao.OrdemDeServicoDAO;
import br.com.techfix.dao.PecaDAO;
import br.com.techfix.dao.UsuarioDAO;
import br.com.techfix.model.Atendente;
import br.com.techfix.model.Peca;
import br.com.techfix.model.Tecnico;
import br.com.techfix.model.Usuario;
import br.com.techfix.view.TelaAbrirOrdemServico;
import br.com.techfix.view.TelaRegistrarCotacao;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

import java.sql.SQLException;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws SQLException {
        Conexao.criarTabelas();

        UsuarioDAO usuarioDAO = new UsuarioDAO();
        Atendente atendente = (Atendente) obterUsuarioPadrao(usuarioDAO, new Atendente(),
            "Camila Souza", "camila", "Atendente");
        Tecnico tecnico = (Tecnico) obterUsuarioPadrao(usuarioDAO, new Tecnico(),
            "Rafael Lima", "rafael", "Tecnico");

        PecaDAO pecaDAO = new PecaDAO();
        cadastrarPecasIniciais(pecaDAO);

        OrdemDeServicoDAO ordemDeServicoDAO = new OrdemDeServicoDAO();

        ControladoraAbrirOrdemServico controladoraAbrir = new ControladoraAbrirOrdemServico(
            atendente,
            new ClienteDAO(),
            new EquipamentoDAO(),
            ordemDeServicoDAO);
        TelaAbrirOrdemServico telaAbrir = new TelaAbrirOrdemServico(controladoraAbrir);

        ControladoraRegistrarCotacao controladoraCotacao = new ControladoraRegistrarCotacao(
            tecnico,
            ordemDeServicoDAO,
            new CotacaoDAO(),
            pecaDAO);
        TelaRegistrarCotacao telaCotacao = new TelaRegistrarCotacao(controladoraCotacao);

        TabPane abas = new TabPane(
            aba("Abrir OS (Atendente)", telaAbrir.getRaiz()),
            aba("Registrar Cotação (Técnico)", telaCotacao.getRaiz()));

        stage.setTitle("TechFix");
        Scene cena = new Scene(abas, 900, 800);
        cena.getStylesheets().add(getClass().getResource("/css/estilo.css").toExternalForm());
        stage.setScene(cena);
        stage.show();
    }

    private Tab aba(String titulo, javafx.scene.Node conteudo) {
        Tab aba = new Tab(titulo, conteudo);
        aba.setClosable(false);
        return aba;
    }

    // Usuários fixos até o login e o UC05 - Gerenciar Usuário serem implementados.
    private Usuario obterUsuarioPadrao(UsuarioDAO usuarioDAO, Usuario novo, String nome, String login,
                                       String perfil) throws SQLException {
        Usuario usuario = usuarioDAO.buscarPorLogin(login);
        if (usuario == null) {
            novo.setNome(nome);
            novo.setLogin(login);
            novo.setSenha("1234");
            novo.setPerfilAcesso(perfil);
            novo.setSituacao(true);
            usuarioDAO.inserir(novo);
            usuario = novo;
        }
        return usuario;
    }

    // Peças de exemplo até o UC03 - Gerenciar Peça ser implementado.
    private void cadastrarPecasIniciais(PecaDAO pecaDAO) throws SQLException {
        if (pecaDAO.existeAlguma()) {
            return;
        }
        pecaDAO.inserir(peca("MEM-8GB", "Memória RAM 8GB DDR4", "Memória", 110.00, 189.90, 12, 3));
        pecaDAO.inserir(peca("SSD-240", "SSD 240GB SATA", "Armazenamento", 140.00, 229.90, 8, 3));
        pecaDAO.inserir(peca("FONTE-500", "Fonte ATX 500W", "Energia", 120.00, 199.90, 5, 2));
        pecaDAO.inserir(peca("TEC-NB", "Teclado para notebook", "Periférico", 45.00, 89.90, 6, 2));
        pecaDAO.inserir(peca("FLAT-TELA", "Cabo flat de tela", "Cabo", 18.00, 49.90, 10, 3));
    }

    private Peca peca(String codigo, String descricao, String categoria, double custo, double venda,
                      int estoque, int minimo) {
        Peca peca = new Peca();
        peca.setCodigo(codigo);
        peca.setDescricao(descricao);
        peca.setCategoria(categoria);
        peca.setPrecoCusto(custo);
        peca.setPrecoVenda(venda);
        peca.setQuantidadeEstoque(estoque);
        peca.setQuantidadeMinimaEstoque(minimo);
        return peca;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
