package com.noticiapopular.autenticacao.application.usecases;

import com.noticiapopular.autenticacao.application.dtos.RefreshTokenRequest;
import com.noticiapopular.autenticacao.application.dtos.TokenResponse;
import com.noticiapopular.autenticacao.application.ports.out.UsuarioRepositoryPort;
import com.noticiapopular.autenticacao.domain.entities.Usuario;
import com.noticiapopular.kernel.domain.exceptions.AcessoNegadoException;
import com.noticiapopular.kernel.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefrescarTokenUseCase {

    private final JwtTokenProvider jwtTokenProvider;
    private final UsuarioRepositoryPort usuarioRepository;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    public TokenResponse executar(RefreshTokenRequest request) {
        String refreshToken = request.refreshToken();

        if (!jwtTokenProvider.validarToken(refreshToken)) {
            throw new AcessoNegadoException("Refresh token inválido ou expirado");
        }

        if (!jwtTokenProvider.ehRefreshToken(refreshToken)) {
            throw new AcessoNegadoException("Token fornecido não é um refresh token");
        }

        String usuarioId = jwtTokenProvider.obterUsuarioIdDoToken(refreshToken);

        Usuario usuario = usuarioRepository.buscarPorId(usuarioId)
                .orElseThrow(() -> new AcessoNegadoException("Usuário não encontrado"));

        if (!usuario.isAtivo()) {
            throw new AcessoNegadoException("Usuário desativado");
        }

        String novoAccessToken = jwtTokenProvider.gerarAccessToken(usuario.getId(), usuario.getPapel().name());
        String novoRefreshToken = jwtTokenProvider.gerarRefreshToken(usuario.getId());

        log.debug("Token renovado para usuário: {}", usuarioId);
        return new TokenResponse(novoAccessToken, novoRefreshToken, usuario.ehAdmin(), jwtExpirationMs / 1000);
    }
}
