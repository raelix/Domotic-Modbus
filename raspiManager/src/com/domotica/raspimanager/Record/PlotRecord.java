package com.domotica.raspimanager.record;

import java.util.Date;
import java.util.LinkedList;

public class PlotRecord {

	public String nome;
	public String indirizzo;
	LinkedList<Float> list ;
	LinkedList<Date> listDate ;
	public PlotRecord(String nome, String indirizzo,float value) {
	this.nome = nome;
	this.indirizzo = indirizzo;
	this.list = new LinkedList<Float>();
	list.add(value);
	this.listDate = new LinkedList<Date>();
	listDate.add(new Date());
	}
	public LinkedList<Date> getListDate() {
		return listDate;
	}
	public void setListDate(LinkedList<Date> listDate) {
		this.listDate = listDate;
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
	public LinkedList<Float> getList() {
		return list;
	}
	public void setList(LinkedList<Float> list) {
		this.list = list;
	}

}