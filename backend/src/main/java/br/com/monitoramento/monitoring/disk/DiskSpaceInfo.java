package br.com.monitoramento.monitoring.disk;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Snapshot de uso de disco de um filesystem em um dado instante.
 */
public record DiskSpaceInfo(long espacoTotal, long espacoUtilizado, long espacoLivre) {

    public BigDecimal percentualUtilizado() {
        if (espacoTotal <= 0) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(espacoUtilizado)
                // Calcula o percentual de espaço utilizado com precisão de 4 casas decimais, arredondando para cima
                .divide(BigDecimal.valueOf(espacoTotal), 4, RoundingMode.HALF_UP)
                // Multiplica por 100 para obter o percentual
                .multiply(BigDecimal.valueOf(100))
                // Arredonda o resultado final para 2 casas decimais, arredondando para cima
                .setScale(2, RoundingMode.HALF_UP);
    }
}
