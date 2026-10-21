package br.com.techfix;

import br.com.techfix.controller.ControladoraAbrirOrdemServico;
import br.com.techfix.dao.ClienteDAO;
import br.com.techfix.dao.Conexao;
import br.com.techfix.dao.EquipamentoDAO;
import br.com.techfix.dao.OrdemDeServicoDAO;
import br.com.techfix.dao.AtendenteDAO;
import br.com.techfix.model.Atendente;
import br.com.techfix.view.TelaAbrirOrdemServico;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.SQLException;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws SQLException {
        Conexao.criarTabelas();

        Atendente atendente = obterAtendentePadrao(new AtendenteDAO());

        ControladoraAbrirOrdemServico controladora = new ControladoraAbrirOrdemServico(
            atendente,
            new ClienteDAO(),
            new EquipamentoDAO(),
            new OrdemDeServicoDAO());

        TelaAbrirOrdemServico tela = new TelaAbrirOrdemServico(controladora);

        stage.setTitle("TechFix");
        Scene cena = new Scene(tela.getRaiz(), 860, 760);
        cena.getStylesheets().add(getClass().getResource("/css/estilo.css").toExternalForm());
        stage.setScene(cena);
        stage.show();
    }

    private Atendente obterAtendentePadrao(AtendenteDAO atendenteDAO) throws SQLException {
        Atendente atendente = atendenteDAO.buscarPorLogin("camila");
        if (atendente == null) {
            atendente = new Atendente();
            atendente.setNome("Camila Souza");
            atendente.setLogin("camila");
            atendente.setSenha("1234");
            atendente.setPerfilAcesso("Atendente");
            atendente.setSituacao(true);
            atendenteDAO.inserir(atendente);
        }
        return atendente;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
