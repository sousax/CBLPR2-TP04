import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;

public class TP04JAVA extends JFrame {
    private JTextField txtNomePesquisa;
    private JLabel lblNome, lblSalario, lblCargo;
    private JTextField txtNome, txtSalario, txtCargo;
    private JButton btnPesquisar, btnAnterior, btnProximo;
    private Connection conn;
    private PreparedStatement ps;
    private ResultSet resultSet;


    private ArrayList<String> historicoPesquisas = new ArrayList<>();
    private int posicaoHistorico = -1;

    public TP04JAVA() {
        setTitle("TRABALHO PRATICO 04");
        setLayout(null);
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel lblPesquisa = new JLabel("Nome:");
        lblPesquisa.setBounds(20, 20, 50, 20);
        add(lblPesquisa);

        txtNomePesquisa = new JTextField();
        txtNomePesquisa.setBounds(80, 20, 200, 20);
        add(txtNomePesquisa);

        btnPesquisar = new JButton("Pesquisar");
        btnPesquisar.setBounds(290, 20, 100, 20);
        add(btnPesquisar);

        lblNome = new JLabel("Nome:");
        lblNome.setBounds(20, 60, 50, 20);
        add(lblNome);

        txtNome = new JTextField();
        txtNome.setBounds(80, 60, 200, 20);
        txtNome.setEditable(false);
        add(txtNome);

        lblSalario = new JLabel("Salário:");
        lblSalario.setBounds(20, 100, 50, 20);
        add(lblSalario);

        txtSalario = new JTextField();
        txtSalario.setBounds(80, 100, 200, 20);
        txtSalario.setEditable(false);
        add(txtSalario);

        lblCargo = new JLabel("Cargo:");
        lblCargo.setBounds(20, 140, 50, 20);
        add(lblCargo);

        txtCargo = new JTextField();
        txtCargo.setBounds(80, 140, 200, 20);
        txtCargo.setEditable(false);
        add(txtCargo);

        btnAnterior = new JButton("Anterior");
        btnAnterior.setBounds(50, 200, 100, 30);
        add(btnAnterior);

        btnProximo = new JButton("Próximo");
        btnProximo.setBounds(200, 200, 100, 30);
        add(btnProximo);

        btnPesquisar.addActionListener(e -> pesquisarFuncionario());
        btnAnterior.addActionListener(e -> navegarHistorico(false));
        btnProximo.addActionListener(e -> navegarHistorico(true));

        setVisible(true);
    }

    private void pesquisarFuncionario() {
        String nome = txtNomePesquisa.getText();

        try {
            // Fechar conexão e ResultSet anteriores, se existirem
            if (resultSet != null) resultSet.close();
            if (ps != null) ps.close();
            if (conn != null) conn.close();

            // Criar nova conexão
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/TrabalhoPratico04", "root", "*Consagrado712");
            ps = conn.prepareStatement(
                "SELECT f.nome_func, f.sal_func, c.ds_cargo " +
                "FROM tbfuncs f " +
                "JOIN tbcargos c ON f.cod_cargo = c.cd_cargo " +
                "WHERE f.nome_func LIKE ?",
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY
            );
            ps.setString(1, "%" + nome + "%");
            resultSet = ps.executeQuery();

            if (resultSet.next()) {

                if (historicoPesquisas.isEmpty() || !historicoPesquisas.get(historicoPesquisas.size() - 1).equals(nome)) {
                    historicoPesquisas.add(nome);
                    posicaoHistorico = historicoPesquisas.size() - 1;
                }
                exibirDados();
            } else {
                JOptionPane.showMessageDialog(this, "Nenhum registro encontrado.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao pesquisar: " + ex.getMessage());
        }
    }

    private void navegarHistorico(boolean proximo) {
        if (historicoPesquisas.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nenhuma pesquisa realizada.");
            return;
        }

        if (proximo) {
            if (posicaoHistorico < historicoPesquisas.size() - 1) {
                posicaoHistorico++;
                executarPesquisaDoHistorico();
            } else {
                JOptionPane.showMessageDialog(this, "Não há mais registros nessa direção.");
            }
        } else {
            if (posicaoHistorico > 0) {
                posicaoHistorico--;
                executarPesquisaDoHistorico();
            } else {
                JOptionPane.showMessageDialog(this, "Não há mais registros nessa direção.");
            }
        }
    }

    private void executarPesquisaDoHistorico() {
        String nome = historicoPesquisas.get(posicaoHistorico);

        try {
            if (resultSet != null) resultSet.close();
            if (ps != null) ps.close();

            ps = conn.prepareStatement(
                "SELECT f.nome_func, f.sal_func, c.ds_cargo " +
                "FROM tbfuncs f " +
                "JOIN tbcargos c ON f.cod_cargo = c.cd_cargo " +
                "WHERE f.nome_func LIKE ?",
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY
            );
            ps.setString(1, "%" + nome + "%");
            resultSet = ps.executeQuery();

            if (resultSet.next()) {
                exibirDados();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao executar histórico: " + ex.getMessage());
        }
    }

    private void exibirDados() throws SQLException {
        txtNome.setText(resultSet.getString("nome_func"));
        txtSalario.setText(String.valueOf(resultSet.getBigDecimal("sal_func")));
        txtCargo.setText(resultSet.getString("ds_cargo"));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TP04JAVA::new);
    }
}
