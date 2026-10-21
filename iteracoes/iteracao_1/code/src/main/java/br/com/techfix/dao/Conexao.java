package br.com.techfix.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Conexao {

    private static final String URL = "jdbc:sqlite:techfix.db";

    private static final String[] TABELAS = {
        "CREATE TABLE IF NOT EXISTS Atendente ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "nome TEXT NOT NULL, "
            + "login TEXT NOT NULL UNIQUE, "
            + "senha TEXT NOT NULL, "
            + "situacao INTEGER NOT NULL)",

        "CREATE TABLE IF NOT EXISTS Cliente ("
            + "cpfOuCnpj TEXT PRIMARY KEY, "
            + "tipoPessoa TEXT, "
            + "nomeOuRazaoSocial TEXT NOT NULL, "
            + "telefonePrincipal TEXT NOT NULL, "
            + "telefoneAlternativo TEXT, "
            + "enderecoEmail TEXT, "
            + "endereco TEXT, "
            + "clienteFaturado INTEGER NOT NULL, "
            + "dataCadastro TEXT NOT NULL)",

        "CREATE TABLE IF NOT EXISTS Equipamento ("
            + "numeroSerie TEXT PRIMARY KEY, "
            + "tipo TEXT, "
            + "marca TEXT, "
            + "modelo TEXT, "
            + "valorReferencia REAL, "
            + "observacoes TEXT, "
            + "cpfOuCnpjCliente TEXT NOT NULL REFERENCES Cliente(cpfOuCnpj))",

        "CREATE TABLE IF NOT EXISTS OrdemDeServico ("
            + "numero INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "dataHoraEntrada TEXT NOT NULL, "
            + "defeitoRelatado TEXT, "
            + "checklistAcessorios TEXT, "
            + "situacao TEXT NOT NULL, "
            + "cienciaTaxaDiagnostico INTEGER NOT NULL, "
            + "numeroSerieEquipamento TEXT NOT NULL REFERENCES Equipamento(numeroSerie), "
            + "idAtendente INTEGER NOT NULL REFERENCES Atendente(id))",

        "CREATE TABLE IF NOT EXISTS HistoricoDeSituacao ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "situacao TEXT NOT NULL, "
            + "dataHora TEXT NOT NULL, "
            + "numeroOrdemDeServico INTEGER NOT NULL REFERENCES OrdemDeServico(numero), "
            + "idAtendente INTEGER NOT NULL REFERENCES Atendente(id))"
    };

    public static Connection abrir() throws SQLException {
        Connection conexao = DriverManager.getConnection(URL);
        try (Statement st = conexao.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
        }
        return conexao;
    }

    public static void criarTabelas() throws SQLException {
        try (Connection conexao = abrir(); Statement st = conexao.createStatement()) {
            for (String sql : TABELAS) {
                st.execute(sql);
            }
        }
    }
}
