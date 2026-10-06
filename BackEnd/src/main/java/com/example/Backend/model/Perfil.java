package com.example.Backend.model;

/**
 * Nivel de acesso do usuario dentro do painel.
 *
 * O enum existe para o Spring Security decidir AUTORIZACAO (o que a pessoa
 * pode fazer), separado da AUTENTICACAO (quem a pessoa e). Em
 * Usuario.getAuthorities() cada valor vira a role ROLE_USER ou ROLE_ADMIN.
 *
 * Guardado no banco como texto (@Enumerated(STRING)): se fosse numero, mudar
 * a ordem das constantes aqui trocaria o perfil de quem ja esta cadastrado.
 */
public enum Perfil {

    /** Usa o painel: dashboard, carteira, upload. E o padrao de todo cadastro. */
    USER,

    /** Alem do painel, administra as contas (ex.: excluir usuario). */
    ADMIN
}
