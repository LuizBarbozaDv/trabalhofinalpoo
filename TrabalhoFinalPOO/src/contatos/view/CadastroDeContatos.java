package contatos.view;

import contatos.model.Contatos;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CadastroDeContatos extends JDialog {
    private final JTextField txtNome = new JTextField(20);
    private final JTextField txtTelefone = new JTextField(20);
    private final JTextField txtEmail = new JTextField(20);
    private final JTextField txtCategoria = new JTextField(20);
    private final JFormattedTextField txtDataNascimento;

    private Contatos contato;
    private boolean confirmado = false;

    public CadastroDeContatos(Frame owner, Contatos contato) {
        super(owner, contato == null ? "Novo Contato" : "Editar Contato", true);
        this.contato = contato;

        try {
            txtDataNascimento = new JFormattedTextField(new javax.swing.text.MaskFormatter("##/##/####"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        txtDataNascimento.setColumns(10);

        setSize(540, 500);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 20, 12, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // campos do fomulario

        adicionarCampo(form, gbc, "Nome *:", txtNome, 0);
        adicionarCampo(form, gbc, "Telefone *:", txtTelefone, 1);
        adicionarCampo(form, gbc, "E-mail *:", txtEmail, 2);
        adicionarCampo(form, gbc, "Categoria *:", txtCategoria, 3);
        adicionarCampo(form, gbc, "Aniversario (opcional):", txtDataNascimento, 4);
        txtDataNascimento.setToolTipText("DD/MM/AAAA - deixe em branco se não quiser");

        if (contato != null) {
            txtNome.setText(contato.getNome());
            txtTelefone.setText(contato.getTelefone());
            txtEmail.setText(contato.getEmail());

            if (contato.getDataNascimento() != null) {
                txtDataNascimento.setText(contato.getDataNascimento()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
        }

        // botoes salvar e cancelar (en casos de edicao)

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 15));
        JButton btnSalvar = new JButton("Salvar");
        JButton btnCancelar = new JButton("Cancelar");
        btnSalvar.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnCancelar.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        btnSalvar.addActionListener(e -> {
            if (validarCampos()) {
                LocalDate dataNasc = null;
                String textoData = txtDataNascimento.getText().replace(" ", "").replace("_", "");
                if (textoData.length() == 10) {
                    try {
                        dataNasc = LocalDate.parse(textoData, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    } catch (Exception ignored) {}
                }

                // Cria ou atualiza o contato
                if (this.contato == null) {
                    this.contato = new Contatos("", "", "", "");
                }
                this.contato.setNome(txtNome.getText().trim());
                this.contato.setTelefone(txtTelefone.getText().trim());
                this.contato.setEmail(txtEmail.getText().trim());
                this.contato.setCategoria(txtCategoria.getText().trim().isEmpty() ? "Todas" : txtCategoria.getText().trim());
                this.contato.setDataNascimento(dataNasc);

                confirmado = true;
                dispose();
            }
        });

        btnCancelar.addActionListener(e -> dispose());

        botoes.add(btnSalvar);
        botoes.add(btnCancelar);

        add(form, BorderLayout.CENTER);
        add(botoes, BorderLayout.SOUTH);
    }

    private void adicionarCampo(JPanel p, GridBagConstraints gbc, String label, JComponent campo, int y) {
        gbc.gridx = 0; gbc.gridy = y; gbc.anchor = GridBagConstraints.EAST;
        p.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        p.add(campo, gbc);
        gbc.weightx = 0;
    }

    private boolean validarCampos() {
        if (txtNome.getText().trim().isEmpty() ||
                txtTelefone.getText().trim().isEmpty() ||
                txtEmail.getText().trim().isEmpty() ||
                txtCategoria.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Os campos com * são obrigatórios!", "Atenção", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    public Contatos getContato() { return contato; }
    public boolean foiConfirmado() { return confirmado; }
}