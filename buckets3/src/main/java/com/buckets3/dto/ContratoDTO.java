package com.buckets3.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
@Builder
public class ContratoDTO {

	private Long Id;
	private String codReferenciaExterna;
	private Date dataInicio;
	private String tipoEfeitoContrato;
	private String cnpjRegistrador;
	private String cnpjCredor;
	private String documentoContratante;

}

