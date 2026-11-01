package br.edu.utfpr.netgestor.domain.enums;

/** situacao do equipamento: em estoque, instalado num contrato (comodato) ou com defeito. */
public enum StatusEquipamento {
    ESTOQUE,
    INSTALADO,
    DEFEITO;

    public boolean podeVincular() {
        return this == ESTOQUE;
    }

    public boolean podeDesvincular() {
        return this == INSTALADO;
    }
}
