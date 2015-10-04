package com.domotica.raspimanager.record;

import com.domotica.raspimanager.shared.Optional;

public class GpioRecord {
	public String nome;
	public String indirizzo;
	public boolean stato;//false 0 , true 1
	public boolean inout;//in true , out false
	public Optional optional;
	public boolean optionalIsShow ;
	
	public GpioRecord(String nome, String indirizzo,boolean stato,boolean inout) {
	this.nome = nome;
	this.indirizzo = indirizzo;
	this.stato = stato;
	this.inout = inout;
	this.optionalIsShow = false;
	}
	public GpioRecord(String nome, String indirizzo,boolean stato,boolean inout,Optional opt) {
		this.nome = nome;
		this.indirizzo = indirizzo;
		this.stato = stato;
		this.inout = inout;
		this.optional = opt;
		this.optionalIsShow = false;
		
		}
	public void setOptionalIsShow(boolean isShow){
		this.optionalIsShow = isShow;
	}
	
	public boolean getOptionalIsShow( ){
		return this.optionalIsShow;
	}
	public void setOptional(Optional opt){
		this.optional = opt;
	}
	public Optional getOptional(){
		return optional;
	}
	public boolean isInout() {
		return inout;
	}
	public void setInout(boolean inout) {
		this.inout = inout;
	}
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public String getIndirizzo() {
		return indirizzo;
	}
	public void setIndirizzo(String indirizzo) {
		this.indirizzo = indirizzo;
	}
	public boolean isStato() {
		return stato;
	}
	public void setStato(boolean stato) {
		this.stato = stato;
	}


}