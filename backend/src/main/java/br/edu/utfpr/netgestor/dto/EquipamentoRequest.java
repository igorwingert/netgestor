package br.edu.utfpr.netgestor.dto;

import br.edu.utfpr.netgestor.domain.enums.TipoEquipamento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * dados tecnicos do equipamento. o status e o contrato (comodato) nao entram aqui:
 * sao controlados pelas acoes vincular/desvincular.
 */
public record EquipamentoRequest(
        @NotNull(message = "O tipo é obrigatório (ONU ou ROTEADOR)")
        TipoEquipamento tipo,

        @NotBlank(message = "O fabricante é obrigatório")
        @Size(max = 60, message = "O fabricante deve ter no máximo 60 caracteres")
        String fabricante,

        @NotBlank(message = "O modelo é obrigatório")
        @Size(max = 60, message = "O modelo deve ter no máximo 60 caracteres")
        String modelo,

        @NotBlank(message = "O número de série é obrigatório")
        @Size(max = 40, message = "O número de série deve ter no máximo 40 caracteres")
        String numeroSerie,

        @NotBlank(message = "O MAC é obrigatório")
        @Pattern(regexp = "^([0-9A-F]{2}:){5}[0-9A-F]{2}$", message = "MAC inválido (formato AA:BB:CC:DD:EE:FF)")
        String mac) {

    /** aceita MAC em minusculas ou separado por hifen: aa-bb-cc-dd-ee-ff vira AA:BB:CC:DD:EE:FF. */
    public EquipamentoRequest {
        if (mac != null) {
            mac = mac.trim().toUpperCase().replace('-', ':');
        }
        if (numeroSerie != null) {
            numeroSerie = numeroSerie.trim().toUpperCase();
        }
    }
}
