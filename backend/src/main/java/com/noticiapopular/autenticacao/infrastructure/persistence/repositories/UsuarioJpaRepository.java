package com.noticiapopular.autenticacao.infrastructure.persistence.repositories;

import com.noticiapopular.autenticacao.infrastructure.persistence.entities.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, String> {

    Optional<UsuarioEntity> findByEmail(String email);

    Optional<UsuarioEntity> findByProviderAndProviderId(String provider, String providerId);
}
