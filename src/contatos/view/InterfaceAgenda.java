package contatos.view;

import contatos.model.Contatos;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

public class InterfaceAgenda extends JFrame {
    private final List<Contatos> listaContatos = new ArrayList<>();
    private JTable tabela;
    private DefaultTableModel modelo;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField txtBusca;
    private JComboBox<String> comboCat;
    private JButton btnFavoritos, btnOrdem, btnAniversariantes;
    private boolean apenasFavoritos = false;
    private boolean ordemAZ = true;
    private boolean mostrarAniversariantes = false;

    public InterfaceAgenda() {
        setTitle("Agenda de Contatos");
        setSize(1200, 760);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        criarBarraSuperior();
        criarTabela();
        criarBarraInferior();

        atualizarTabela();
    }

    private void criarBarraSuperior() {
        JToolBar barra = new JToolBar();
        barra.setFloatable(false);
        barra.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        // botao para criar novo contato (verde pra ficar mais intuitivo)

        JButton btnNovo = new JButton(" Novo Contato");
        btnNovo.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnNovo.setForeground(Color.WHITE);
        btnNovo.setBackground(new Color(0, 170, 0));
        btnNovo.setFocusPainted(false);
        btnNovo.setBorderPainted(false);
        btnNovo.setOpaque(true);
        btnNovo.setBorder(BorderFactory.createEmptyBorder(12, 28, 12, 28));
        btnNovo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnNovo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btnNovo.setBackground(new Color(0, 140, 0)); }
            public void mouseExited(java.awt.event.MouseEvent e) { btnNovo.setBackground(new Color(0, 170, 0)); }
        });
        btnNovo.addActionListener(e -> abrirCadastro(null));

        JPanel painelEsquerda = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 12));
        painelEsquerda.setOpaque(false);
        painelEsquerda.add(btnNovo);

        JPanel painelDireita = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        painelDireita.setOpaque(false);

        painelDireita.add(new JLabel("Buscar:"));
        txtBusca = new JTextField(28);
        txtBusca.setPreferredSize(new Dimension(300, 38));
        painelDireita.add(txtBusca);

        painelDireita.add(Box.createHorizontalStrut(25));
        painelDireita.add(new JLabel("Categoria:"));
        comboCat = new JComboBox<>(new String[]{"Todas"});
        comboCat.setPreferredSize(new Dimension(210, 38));
        painelDireita.add(comboCat);

        barra.add(painelEsquerda);
        barra.add(Box.createHorizontalGlue());
        barra.add(painelDireita);

        txtBusca.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { aplicarFiltros(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { aplicarFiltros(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { aplicarFiltros(); }
        });
        comboCat.addActionListener(e -> aplicarFiltros());

        add(barra, BorderLayout.NORTH);
    }

    private void criarTabela() {
        modelo = new DefaultTableModel(new String[]{"Favoritar", "Nome", "Telefone", "E-mail", "Categoria", "Nascimento"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 0; }
            @Override public Class<?> getColumnClass(int c) { return c == 0 ? Boolean.class : String.class; }
        };

        tabela = new JTable(modelo);
        tabela.setRowHeight(42);
        tabela.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        tabela.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        tabela.getColumnModel().getColumn(0).setPreferredWidth(100);
        tabela.getColumnModel().getColumn(0).setMaxWidth(100);
        tabela.getColumnModel().getColumn(0).setCellRenderer((t, v, s, f, r, c) -> {
            boolean fav = Boolean.TRUE.equals(v);
            JLabel l = new JLabel(fav ? "★" : "☆", SwingConstants.CENTER);
            l.setFont(new Font("Segoe UI Emoji", Font.BOLD, 32));
            l.setForeground(fav ? new Color(255, 193, 7) : Color.GRAY);
            l.setOpaque(true);
            l.setBackground(s ? t.getSelectionBackground() : t.getBackground());
            return l;
        });

        modelo.addTableModelListener(e -> {
            if (e.getColumn() == 0 && e.getType() == TableModelEvent.UPDATE) {
                int viewRow = e.getFirstRow();
                int modelRow = tabela.convertRowIndexToModel(viewRow);
                boolean favorito = (boolean) modelo.getValueAt(modelRow, 0);
                int idx = obterIndiceReal(viewRow);
                if (idx != -1) listaContatos.get(idx).setFavorito(favorito);
            }
        });

        tabela.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && tabela.columnAtPoint(e.getPoint()) != 0) {
                    abrirCadastro(obterContatoSelecionado());
                }
            }
        });

        sorter = new TableRowSorter<>(modelo);
        tabela.setRowSorter(sorter);
        add(new JScrollPane(tabela), BorderLayout.CENTER);
    }

    private void criarBarraInferior() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        barra.setBackground(new Color(248, 249, 250));

        // detalhe bobinho pra preencher mais a tela

        JLabel status = new JLabel("Total: 0 | Favoritos: 0 | Exibindo: 0");
        status.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 0));

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 18, 8));
        botoes.setOpaque(false);

        JButton btnEditar = new JButton("Editar Contato");
        JButton btnExcluir = new JButton("Excluir Contato");

        // botao para filtrar contatos favoritados

        btnFavoritos = new JButton("Apenas Favoritos");
        btnFavoritos.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnFavoritos.setForeground(Color.WHITE);
        btnFavoritos.setBackground(new Color(0, 120, 0));
        btnFavoritos.setFocusPainted(false);
        btnFavoritos.setBorderPainted(false);
        btnFavoritos.setOpaque(true);
        btnFavoritos.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnFavoritos.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnFavoritos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btnFavoritos.setBackground(new Color(0, 90, 0)); }
            public void mouseExited(java.awt.event.MouseEvent e) { btnFavoritos.setBackground(new Color(0, 120, 0)); }
        });
        btnFavoritos.addActionListener(e -> {
            apenasFavoritos = !apenasFavoritos;
            btnFavoritos.setText(apenasFavoritos ? "Todos os Contatos" : "Apenas Favoritos");
            aplicarFiltros();
        });

        // botao para filtrar anivesariantes

        btnAniversariantes = new JButton("Aniversariantes do Mês");
        btnAniversariantes.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAniversariantes.setForeground(Color.WHITE);
        btnAniversariantes.setBackground(new Color(139, 0, 139));
        btnAniversariantes.setFocusPainted(false);
        btnAniversariantes.setBorderPainted(false);
        btnAniversariantes.setOpaque(true);
        btnAniversariantes.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnAniversariantes.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAniversariantes.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btnAniversariantes.setBackground(new Color(100, 0, 100)); }
            public void mouseExited(java.awt.event.MouseEvent e) { btnAniversariantes.setBackground(new Color(139, 0, 139)); }
        });
        btnAniversariantes.addActionListener(e -> {
            mostrarAniversariantes = !mostrarAniversariantes;
            btnAniversariantes.setText(mostrarAniversariantes ? "Todos os Contatos" : "Aniversariantes do Mês");
            aplicarFiltros();
        });

        btnOrdem = new JButton("A a Z");
        btnOrdem.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnOrdem.addActionListener(e -> {
            ordemAZ = !ordemAZ;
            btnOrdem.setText(ordemAZ ? "A a Z" : "Z a A");
            aplicarFiltros();
        });

        Font f = new Font("Segoe UI", Font.BOLD, 14);
        btnEditar.setFont(f); btnExcluir.setFont(f); btnOrdem.setFont(f);

        tabela.getSelectionModel().addListSelectionListener(e -> {
            boolean tem = tabela.getSelectedRow() != -1;
            btnEditar.setEnabled(tem);
            btnExcluir.setEnabled(tem);
        });

        btnEditar.addActionListener(e -> abrirCadastro(obterContatoSelecionado()));
        btnExcluir.addActionListener(e -> excluirContato());

        botoes.add(btnEditar);
        botoes.add(btnExcluir);
        botoes.add(Box.createHorizontalStrut(40));
        botoes.add(btnFavoritos);
        botoes.add(btnAniversariantes);
        botoes.add(Box.createHorizontalStrut(20));
        botoes.add(btnOrdem);

        barra.add(status, BorderLayout.WEST);
        barra.add(botoes, BorderLayout.EAST);

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override public void run() {
                SwingUtilities.invokeLater(() -> {
                    int t = listaContatos.size();
                    int fav = (int) listaContatos.stream().filter(Contatos::isFavorito).count();
                    int ex = modelo.getRowCount();
                    status.setText(String.format(" Total: %d | Favoritos: %d | Exibindo: %d ", t, fav, ex));
                });
            }
        }, 0, 500);

        add(barra, BorderLayout.SOUTH);
    }

    private void abrirCadastro(Contatos c) {
        CadastroDeContatos dialog = new CadastroDeContatos(this, c);
        dialog.setVisible(true);

        if (dialog.foiConfirmado()) {
            if (c == null) {
                listaContatos.add(dialog.getContato());
            }

            atualizarTabela();
            comboCat.setSelectedItem("Todas");
        }
    }

    private void excluirContato() {
        int row = tabela.getSelectedRow();
        if (row == -1) return;
        int idx = obterIndiceReal(row);
        if (idx == -1) return;
        if (JOptionPane.showConfirmDialog(this, "Excluir Contato Permanentemente?", "Confirmação",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            listaContatos.remove(idx);
            atualizarTabela();
        }
    }

    private Contatos obterContatoSelecionado() {
        int row = tabela.getSelectedRow();
        if (row == -1) return null;
        int idx = obterIndiceReal(row);
        return idx != -1 ? listaContatos.get(idx) : null;
    }

    private int obterIndiceReal(int viewRow) {
        if (viewRow < 0) return -1;
        int modelRow = tabela.convertRowIndexToModel(viewRow);
        String nome = (String) modelo.getValueAt(modelRow, 1);
        return listaContatos.stream()
                .filter(c -> c.getNome().equals(nome))
                .findFirst()
                .map(listaContatos::indexOf)
                .orElse(-1);
    }

    private void aplicarFiltros() {
        List<RowFilter<Object, Object>> filtros = new ArrayList<>();

        String busca = txtBusca.getText().trim();
        if (!busca.isEmpty()) {
            filtros.add(RowFilter.regexFilter("(?i).*" + Pattern.quote(busca) + ".*", 1));
        }

        String cat = (String) comboCat.getSelectedItem();
        if (cat != null && !"Todas".equals(cat)) {
            filtros.add(RowFilter.regexFilter("^" + Pattern.quote(cat) + "$", 4));
        }

        if (apenasFavoritos) {
            filtros.add(new RowFilter<Object, Object>() {
                @Override public boolean include(Entry<? extends Object, ? extends Object> entry) {
                    return Boolean.TRUE.equals(entry.getValue(0));
                }
            });
        }

        if (mostrarAniversariantes) {
            int mesAtual = LocalDate.now().getMonthValue();
            filtros.add(new RowFilter<Object, Object>() {
                @Override public boolean include(Entry<? extends Object, ? extends Object> entry) {
                    String nomeContato = (String) entry.getValue(1);
                    return listaContatos.stream()
                            .filter(c -> c.getNome().equals(nomeContato))
                            .anyMatch(c -> c.getDataNascimento() != null &&
                                    c.getDataNascimento().getMonthValue() == mesAtual);
                }
            });
        }

        sorter.setRowFilter(filtros.isEmpty() ? null : RowFilter.andFilter(filtros));
        sorter.setSortKeys(List.of(new RowSorter.SortKey(1, ordemAZ ? SortOrder.ASCENDING : SortOrder.DESCENDING)));
    }

    private void atualizarTabela() {
        modelo.setRowCount(0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (Contatos c : listaContatos) {
            String nasc = c.getDataNascimento() != null ? c.getDataNascimento().format(fmt) : "";
            modelo.addRow(new Object[]{c.isFavorito(), c.getNome(), c.getTelefone(), c.getEmail(), c.getCategoria(), nasc});
        }
        Set<String> cats = new TreeSet<>();
        cats.add("Todas");
        listaContatos.forEach(c -> cats.add(c.getCategoria()));
        comboCat.setModel(new DefaultComboBoxModel<>(cats.toArray(new String[0])));
        aplicarFiltros();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
            new InterfaceAgenda().setVisible(true);
        });
    }
}