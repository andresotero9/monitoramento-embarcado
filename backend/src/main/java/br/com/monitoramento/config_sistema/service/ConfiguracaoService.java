package br.com.monitoramento.config_sistema.service;

import br.com.monitoramento.config_sistema.dto.ConfiguracaoDTO;
import br.com.monitoramento.config_sistema.entity.Configuracao;
import br.com.monitoramento.config_sistema.repository.ConfiguracaoRepository;
import br.com.monitoramento.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Centraliza a leitura e atualização das configurações do sistema.
 *
 * <p>Os métodos {@code getInt}, {@code getDecimal} e {@code getString} são utilizados
 * pelos serviços de monitoramento (Internet, Disco, Câmeras) para ler seus parâmetros
 * sem precisar conhecer o modelo de persistência chave/valor.</p>
 */
@Service
public class ConfiguracaoService {

    private final ConfiguracaoRepository configuracaoRepository;

    public ConfiguracaoService(ConfiguracaoRepository configuracaoRepository) {
        this.configuracaoRepository = configuracaoRepository;
    }

    public List<ConfiguracaoDTO> listarTodas() {
        return configuracaoRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * Atualiza em lote os valores de configuração recebidos. Cada item é localizado
     * pela chave; chaves inexistentes geram {@link ResourceNotFoundException}.
     */
    public List<ConfiguracaoDTO> atualizar(List<ConfiguracaoDTO> configuracoes) {
        return configuracoes.stream()
                .map(dto -> {
                    Configuracao configuracao = buscarPorChaveOuFalhar(dto.getChave());
                    configuracao.setValor(dto.getValor());
                    return toDTO(configuracaoRepository.save(configuracao));
                })
                .toList();
    }

    public String getString(String chave, String valorPadrao) {
        return configuracaoRepository.findByChave(chave)
                .map(Configuracao::getValor)
                .orElse(valorPadrao);
    }

    public int getInt(String chave, int valorPadrao) {
        return configuracaoRepository.findByChave(chave)
                .map(c -> Integer.parseInt(c.getValor()))
                .orElse(valorPadrao);
    }

    public BigDecimal getDecimal(String chave, BigDecimal valorPadrao) {
        return configuracaoRepository.findByChave(chave)
                .map(c -> new BigDecimal(c.getValor()))
                .orElse(valorPadrao);
    }

    private Configuracao buscarPorChaveOuFalhar(String chave) {
        return configuracaoRepository.findByChave(chave)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Configuração não encontrada para a chave: " + chave));
    }

    private ConfiguracaoDTO toDTO(Configuracao configuracao) {
        return new ConfiguracaoDTO(
                configuracao.getId(),
                configuracao.getChave(),
                configuracao.getValor(),
                configuracao.getTipo(),
                configuracao.getDescricao(),
                configuracao.getDataAtualizacao()
        );
    }
}
