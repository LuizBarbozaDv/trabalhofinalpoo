// src/contatos/model/Pessoa.java
package contatos.model;

import java.time.LocalDate;

public abstract class Pessoa {
    private String nome;
    private String telefone;
    private String email;
    private LocalDate dataNascimento;

    public Pessoa(String nome, String telefone, String email, LocalDate dataNascimento) {
        this.nome = nome;
        this.telefone = telefone;
        this.email = email;
        this.dataNascimento = dataNascimento;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    // metodo abstrato para pegar a categoria

    public abstract String getCategoria();
}