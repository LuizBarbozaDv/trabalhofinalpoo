package contatos.model;

import java.time.LocalDate;

public class Contatos extends Pessoa implements Favoritavel {
    private String categoria;
    private Boolean favorito;

    public Contatos(String nome, String telefone, String email, String categoria) {
        this(nome, telefone, email, categoria, null);
    }

    public Contatos(String nome, String telefone, String email, String categoria, LocalDate dataNascimento) {
        super(nome, telefone, email, dataNascimento);
        this.categoria = categoria;
        this.favorito = false;
    }

    @Override
    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    @Override
    public Boolean isFavorito() {
        return favorito;
    }

    @Override
    public void setFavorito(Boolean favorito) {
        this.favorito = favorito;
    }

    // metodo responsavel por fazer o filtro de aniversariante do mes funcionar

    public boolean isAniversarianteEsteMes() {
        if (getDataNascimento() == null) return false;
        return getDataNascimento().getMonth() == LocalDate.now().getMonth();
    }
}