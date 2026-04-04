package com.noticiapopular.admin.application.dtos;

public record AdminDashboardDTO(
        long totalUsuarios,
        long totalNoticias,
        long noticiasPublicadas,
        long noticiasRascunho,
        long totalCategorias
) {}
