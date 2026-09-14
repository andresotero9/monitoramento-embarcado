package br.com.monitoramento.config_sistema.repository;

import br.com.monitoramento.config_sistema.entity.Configuracao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfiguracaoRepository extends JpaRepository<Configuracao, Long> {

    Optional<Configuracao> findByChave(String chave);
}
